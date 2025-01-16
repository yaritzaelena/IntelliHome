from cryptography.fernet import Fernet
import os
import json
import socket
import threading
import time

class ChatServer:
    def __init__(self, host='0.0.0.0', port=1717):
        self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server_socket.bind((host, port))
        self.server_socket.listen(5)
        self.clients = []
        self.failed_attempts = {}  # Diccionario de intentos fallidos
        self.block_duration = 120  # Tiempo de bloqueo en segundos

        # Generar clave de encriptación si no existe
        if not os.path.exists("secret.key"):
            self.generate_key()
        self.key = self.load_key()
        self.cipher = Fernet(self.key)

        print("Servidor iniciado y esperando conexiones...")
        threading.Thread(target=self.accept_connections).start()

    def generate_key(self):
        """ Genera una clave de cifrado y la guarda en un archivo. """
        key = Fernet.generate_key()
        with open("secret.key", "wb") as key_file:
            key_file.write(key)

    def load_key(self):
        """ Carga la clave de cifrado desde el archivo. """
        return open("secret.key", "rb").read()

    def encrypt(self, data):
        """ Encripta los datos con la clave de cifrado. """
        return self.cipher.encrypt(data.encode()).decode()

    def decrypt(self, encrypted_data):
        """ Desencripta los datos cifrados con la clave. """
        try:
            return self.cipher.decrypt(encrypted_data.encode()).decode()
        except:
            return encrypted_data  # Si los datos no están cifrados, los devuelve tal cual

    def accept_connections(self):
        while True:
            client_socket, addr = self.server_socket.accept()
            print(f"Conexión aceptada de {addr}")
            threading.Thread(target=self.handle_client, args=(client_socket,)).start()

    def handle_client(self, client_socket):
        while True:
            try:
                raw_data = client_socket.recv(1024)
                if not raw_data:
                    print("Conexión cerrada por el cliente o mensaje vacío.")
                    break

                message = raw_data.decode('utf-8')
                print(f"Mensaje recibido (JSON crudo): {message}")

                try:
                    data = json.loads(message)
                    action = data.get("action")

                    if action == "register":
                        response = self.register_user(data)
                    elif action == "login":
                        response = self.login_user(data["username"], data["password"])
                    else:
                        response = {"status": "error", "message": "Acción no válida"}

                    response_json = json.dumps(response) + "\n"
                    client_socket.send(response_json.encode('utf-8'))
                    print(f"Respuesta enviada al cliente: {response_json}")

                except json.JSONDecodeError as e:
                    print(f"Error al decodificar JSON: {e}")
                    response = {"status": "error", "message": "Formato JSON inválido"}
                    client_socket.send(json.dumps(response).encode('utf-8'))
                    
            except Exception as e:
                print(f"Error al manejar cliente: {e}")
                break
        client_socket.close()

    def register_user(self, data):
        """ Registra un usuario en la base de datos con encriptación. """
        username = data["username"]
        print(f"Intentando registrar usuario: {username}")

        try:
            if os.path.exists("database.txt"):
                with open("database.txt", "r") as db_file:
                    lines = db_file.read().split("--------------------")
                    for block in lines:
                        if f"username: {username}" in block:  # No ciframos usernames
                            print(f"El usuario {username} ya existe.")
                            return {"status": "false", "message": "El nombre de usuario ya está en uso"}

            with open("database.txt", "a") as db_file:
                db_file.write(f"firstName: {self.encrypt(data['firstName'])}\n")
                db_file.write(f"lastName: {self.encrypt(data['lastName'])}\n")
                db_file.write(f"address: {self.encrypt(data['address'])}\n")
                db_file.write(f"username: {username}\n")  # Username en texto plano
                db_file.write(f"password: {self.encrypt(data['password'])}\n")
                db_file.write(f"hobby: {self.encrypt(data['hobby'])}\n")
                db_file.write(f"cardnumber: {self.encrypt(data['cardnumber'])}\n")
                db_file.write(f"cardexpiry: {self.encrypt(data['cardexpiry'])}\n")
                db_file.write(f"cardcvv: {self.encrypt(data['cardcvv'])}\n")
                db_file.write(f"houseStyle: {self.encrypt(data['houseStyle'])}\n")
                db_file.write(f"transport: {self.encrypt(data['transport'])}\n")
                db_file.write(f"birthDate: {self.encrypt(data['birthDate'])}\n")
                db_file.write(f"{'-' * 20}\n")

            print(f"Usuario registrado: {username}")
            return {"status": "true", "message": "Usuario registrado exitosamente"}

        except Exception as e:
            print(f"Error al guardar en el archivo: {e}")
            return {"status": "error", "message": "Error al guardar los datos"}

    def login_user(self, username, password):
        """ Verifica si un usuario puede iniciar sesión. """
        print(f"Intentando iniciar sesión: {username}")
        current_time = time.time()

        # Verificar si el usuario está bloqueado
        if username in self.failed_attempts:
            attempts, block_time = self.failed_attempts[username]
            if attempts >= 5 and current_time - block_time < self.block_duration:
                remaining_time = self.block_duration - (current_time - block_time)
                print(f"Cuenta bloqueada para {username}. Tiempo restante: {remaining_time:.0f} segundos")
                return {"status": "error", "message": f"Cuenta bloqueada. Intente nuevamente en {remaining_time:.0f} segundos."}

        try:
            with open("database.txt", "r") as db_file:
                lines = db_file.read().split("--------------------")
                for block in lines:
                    user_info = {}
                    for line in block.strip().split("\n"):
                        if ":" in line:
                            key, value = line.split(":", 1)
                            if key.strip() == "password":
                                user_info[key.strip()] = self.decrypt(value.strip())
                            else:
                                user_info[key.strip()] = value.strip()

                    if "username" in user_info and "password" in user_info:
                        if user_info["username"] == username and user_info["password"] == password:
                            print(f"Inicio de sesión exitoso para: {username}")
                            self.failed_attempts.pop(username, None)  # Reset de intentos
                            return {"status": "true", "message": "Login exitoso"}

            print(f"Credenciales incorrectas para: {username}")
            self.record_failed_attempt(username)
            return {"status": "false", "message": "Credenciales incorrectas"}

        except FileNotFoundError:
            print("Base de datos no encontrada.")
            return {"status": "error", "message": "Base de datos no encontrada"}

    def record_failed_attempt(self, username):
        """ Registra intentos fallidos de inicio de sesión. """
        current_time = time.time()
        if username not in self.failed_attempts:
            self.failed_attempts[username] = [1, current_time]
        else:
            attempts, block_time = self.failed_attempts[username]
            if current_time - block_time > self.block_duration:
                self.failed_attempts[username] = [1, current_time]
            else:
                self.failed_attempts[username][0] += 1
                if self.failed_attempts[username][0] >= 5:
                    self.failed_attempts[username][1] = current_time
        print(f"LOG: {username} - Intentos fallidos: {self.failed_attempts[username][0]}")



    def load_users(self):
        users = {}
        try:
            with open("database.txt", "r") as db_file:
                lines = db_file.readlines()
                for i in range(0, len(lines), 3):  # Leer bloques de 3 líneas
                    if lines[i].startswith("username:") and lines[i + 1].startswith("password:"):
                        username = lines[i].split(":", 1)[1].strip()
                        password = lines[i + 1].split(":", 1)[1].strip()
                        users[username] = password
        except FileNotFoundError:
            print("Base de datos no encontrada.")
        except Exception as e:
            print(f"Error al cargar usuarios: {e}")
        return users



if __name__ == "__main__":
    ChatServer()
