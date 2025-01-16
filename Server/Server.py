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
        self.failed_attempts = {}  # Dictionary to track failed attempts and block time
        self.block_duration = 120  # Block duration in seconds

        print("Servidor iniciado y esperando conexiones...")

        threading.Thread(target=self.accept_connections).start()

    def accept_connections(self):
        while True:
            client_socket, addr = self.server_socket.accept()
            print(f"Conexión aceptada de {addr}")  # Confirmar conexión
            threading.Thread(target=self.handle_client, args=(client_socket,)).start()# Para que sea en hilo separado


    def handle_client(self, client_socket):
        while True:
            try:
                raw_data = client_socket.recv(1024)
                if not raw_data:
                    print("Conexión cerrada por el cliente o mensaje vacío.")
                    break

                message = raw_data.decode('utf-8')
                print(f"Mensaje recibido (JSON crudo): {message}")

                # Intentar procesar como JSON
                try:
                    data = json.loads(message)
                    action = data.get("action")
                    firstName = data.get("firstName")
                    lastName = data.get("lastName")
                    address = data.get("address")
                    username = data.get("username")
                    password = data.get("password")
                    hobby = data.get("hobby")
                    cardnumber = data.get("cardnumber")
                    cardexpiry = data.get("cardexpiry")
                    cardcvv = data.get("cardcvv")
                    houseStyle = data.get("houseStyle")
                    transport = data.get("transport")

                    if action == "register":
                        response = self.register_user(firstName, lastName, address, username, password, hobby, cardnumber, cardexpiry, cardcvv, houseStyle, transport)
                    elif action == "login":
                        response = self.login_user(username, password)
                    else:
                        response = {"status": "error", "message": "Acción no válida"}

                    # Enviar la respuesta al cliente
                    response_json = json.dumps(response) +"\n"
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


    def login_user(self, username, password):
        print(f"Intentando iniciar sesión: {username}")
        current_time = time.time()

        # Verificar bloqueo
        if username in self.failed_attempts:
            attempts, block_time = self.failed_attempts[username]
            if attempts >= 3 and current_time - block_time < self.block_duration:
                remaining_time = self.block_duration - (current_time - block_time)
                print(f"Cuenta bloqueada para {username}. Tiempo restante: {remaining_time:.0f} segundos")
                return {
                    "status": "error",
                    "message": f"Cuenta bloqueada. Intente nuevamente en {remaining_time:.0f} segundos."
                }

        # Validar credenciales
        try:
            with open("database.txt", "r") as db_file:
                user_data = {}
                lines = db_file.read().split("--------------------")  # Dividir por separadores de usuario

                for block in lines:
                    block_lines = block.strip().split("\n")
                    user_info = {}
                    for line in block_lines:
                        if ":" in line:
                            key, value = line.split(":", 1)
                            user_info[key.strip()] = value.strip()

                    if "username" in user_info and "password" in user_info:
                        user_data[user_info["username"]] = user_info["password"]

                if username in user_data and user_data[username] == password:
                    print(f"Inicio de sesión exitoso para: {username}")
                    if username in self.failed_attempts:
                        del self.failed_attempts[username]  # Restablecer intentos fallidos
                    return {"status": "true", "message": "Login exitoso"}
                else:
                    print(f"Credenciales incorrectas para: {username}")
                    self.record_failed_attempt(username)
                    return {"status": "false", "message": "Credenciales incorrectas"}

        except FileNotFoundError:
            print("Base de datos no encontrada.")
            return {"status": "error", "message": "Base de datos no encontrada"}
        except Exception as e:
            print(f"Error al manejar login: {e}")
            return {"status": "error", "message": "Error interno del servidor"}


    

    def register_user(self, firstName, lastName, address, username, password, hobby, cardnumber, cardexpiry, cardcvv, houseStyle, transport):
        print(f"Intentando registrar usuario: {username}")
        try:
            # Verificar si el usuario ya existe
            if os.path.exists("database.txt"):
                with open("database.txt", "r") as db_file:
                    lines = db_file.read().split("--------------------")
                    for block in lines:
                        if f"username: {username}" in block:
                            print(f"El usuario {username} ya existe.")
                            return {"status": "false", "message": "El nombre de usuario ya está en uso"}


            # Si no existe, proceder con el registro
            with open("database.txt", "a") as db_file:
                db_file.write(f"firstName: {firstName}\n")
                db_file.write(f"lastName: {lastName}\n")
                db_file.write(f"address: {address}\n")
                db_file.write(f"username: {username}\n")
                db_file.write(f"password: {password}\n")
                db_file.write(f"hobby: {hobby}\n")
                db_file.write(f"cardnumber: {cardnumber}\n")
                db_file.write(f"cardexpiry: {cardexpiry}\n")
                db_file.write(f"cardcvv: {cardcvv}\n")
                db_file.write(f"houseStyle: {houseStyle}\n")
                db_file.write(f"transport: {transport}\n")
                db_file.write(f"{'-'*20}\n")  # Línea separadora para mayor claridad

            print(f"Usuario registrado: {username}")
            return {"status": "true", "message": "Usuario registrado exitosamente"}

        except FileNotFoundError:
            print("Base de datos no encontrada. Creando una nueva.")
            return {"status": "error", "message": "Base de datos no encontrada"}
        except Exception as e:
            print(f"Error al guardar en el archivo: {e}")
            return {"status": "error", "message": "Error al guardar los datos"}




    def record_failed_attempt(self, username):
        current_time = time.time()
        if username not in self.failed_attempts:
            self.failed_attempts[username] = [1, current_time]
        else:
            attempts, block_time = self.failed_attempts[username]
            if current_time - block_time > self.block_duration:
                # Reinicia el contador si el bloqueo expiró
                self.failed_attempts[username] = [1, current_time]
            else:
                # Incrementa los intentos y bloquea si es necesario
                self.failed_attempts[username][0] += 1
                if self.failed_attempts[username][0] >= 3:
                    self.failed_attempts[username][1] = current_time  # Actualiza el tiempo de bloqueo
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
