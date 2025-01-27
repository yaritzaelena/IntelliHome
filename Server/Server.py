from cryptography.fernet import Fernet
import os
import json
import socket
import threading
import time
import base64
import serial

class ChatServer:
    def __init__(self, host='0.0.0.0', port=1717):
        self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server_socket.bind((host, port))
        self.server_socket.listen(5)
        self.clients = []
        self.failed_attempts = {}  # Diccionario de intentos fallidos
        self.block_duration = 120  # Tiempo de bloqueo en segundos
        self.arduino=None
        self.serial_port='COM8'
        self.conexion_exitosa=False

        # Generar clave de encriptación si no existe
        if not os.path.exists("secret.key"):
            self.generate_key()
        self.key = self.load_key()
        self.cipher = Fernet(self.key)

        print("Servidor iniciado y esperando conexiones...")
        threading.Thread(target=self.accept_connections).start()

    def conectar_arduino(self):
        try:
            self.arduino=serial.Serial(self.serial_port,9600)
            print(f"Conectando al puerto {self.serial_port}")
            self.conexion_exitosa=True
        except serial.SerialException as e:
            print(f"Error al conectarse al puerto serial {e}")
            self.conexion_exitosa=False

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
        """ Maneja la comunicación con el cliente en un bucle para permitir múltiples solicitudes. """
        try:
            while True:  # Mantener la conexión abierta para múltiples solicitudes
                data_buffer = ""  # Acumulador para el mensaje JSON completo

                while True:
                    chunk = client_socket.recv(8192).decode("utf-8")
                    if not chunk:
                        print("Conexión cerrada por el cliente.")
                        return  # Sale del bucle si el cliente cierra la conexión

                    data_buffer += chunk  # Acumula los fragmentos de JSON

                    try:
                        data = json.loads(data_buffer)
                        break  # Si el JSON es válido, sale del bucle
                    except json.JSONDecodeError:
                        continue  # Si hay error, sigue esperando más datos

                print(f"Mensaje recibido (JSON completo): {data_buffer}")

                action = data.get("action", "")

                if action == "register":
                    response = self.register_user(data)
                elif action == "login":
                    response = self.login_user(data["username"], data["password"])
                elif action == "luces":
                    response= self.controlar_luces(data)
                elif action == "addHouse":
                    response = self.add_house(data)  # Llamar la nueva función
                else:
                    response = {"status": "error", "message": "Acción no válida"}


                response_json = json.dumps(response) + "\n"
                client_socket.send(response_json.encode("utf-8"))
                print(f"Respuesta enviada al cliente: {response_json}")

        except Exception as e:
            print(f"Error al manejar cliente: {e}")

        finally:
            print("Finalizando conexión con el cliente.")
            client_socket.close()  # Cerrar solo cuando el cliente se desconecte.


    def controlar_luces(self,data):
        habitacion = data["habitacion"]
        self.conectar_arduino()
        if self.conexion_exitosa:
            if self.arduino!=None:
                self.arduino.write(habitacion.encode("utf-8"))
                self.conexion_exitosa=False
                self.arduino.close()
        else:
            print("No se pudo establecer conexion con el arduino")

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

            # Guardar foto si está presente
            photo_data = data.get("photo", "")
            if photo_data:
                try:
                    os.makedirs("photos", exist_ok=True)  # Crear carpeta si no existe
                    photo_bytes = base64.b64decode(photo_data)
                    photo_path = f"photos/{username}.png"
                    with open(photo_path, "wb") as photo_file:
                        photo_file.write(photo_bytes)
                    print(f"Foto de {username} guardada en {photo_path}")
                except Exception as e:
                    print(f"Error al guardar la foto: {str(e)}")

            # Guardar datos en database.txt
            with open("database.txt", "a") as db_file:
                db_file.write(f"firstName: {self.encrypt(data['firstName'])}\n")
                db_file.write(f"lastName: {self.encrypt(data['lastName'])}\n")
                db_file.write(f"address: {self.encrypt(data['address'])}\n")
                db_file.write(f"username: {username}\n")  # Username sin cifrar
                db_file.write(f"password: {self.encrypt(data['password'])}\n")
                db_file.write(f"hobby: {self.encrypt(data['hobby'])}\n")
                db_file.write(f"cardnumber: {self.encrypt(data['cardnumber'])}\n")
                db_file.write(f"cardexpiry: {self.encrypt(data['cardexpiry'])}\n")
                db_file.write(f"cardcvv: {self.encrypt(data['cardcvv'])}\n")
                db_file.write(f"cuentaiban: {self.encrypt(data['cuentaiban'])}\n")
                db_file.write(f"houseStyle: {self.encrypt(data['houseStyle'])}\n")
                db_file.write(f"transport: {self.encrypt(data['transport'])}\n")
                db_file.write(f"birthDate: {self.encrypt(data['birthDate'])}\n")
                db_file.write(f"userType: {self.encrypt(data['userType'])}\n")
                db_file.write(f"photo: {photo_path}\n")  # Guardar la ruta de la imagen en lugar de la Base64
                db_file.write(f"{'-' * 20}\n")

            print(f"Usuario registrado: {username}")
            return {"status": "true", "message": "Usuario registrado exitosamente"}

        except Exception as e:
            print(f"Error al guardar en el archivo: {e}")
            return {"status": "false", "message": "Error en el registro"}

    def login_user(self, username, password):
        """ Verifica si un usuario puede iniciar sesión y devuelve si es propietario o no. """
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
                            if key == "password":
                                user_info[key] = self.decrypt(value)
                            elif key == "cuentaiban":
                                user_info[key] = self.decrypt(value).strip()  # Desencriptar y quitar espacios extra
                            else:
                                user_info[key.strip()] = value.strip()

                    if "username" in user_info and "password" in user_info:
                        if user_info["username"] == username and user_info["password"] == password:
                            print(f"Inicio de sesión exitoso para: {username}")
                            self.failed_attempts.pop(username, None)  # Reset de intentos

                            # Verificar si el usuario tiene una cuenta IBAN vacía
                            propietario = "false" if "cuentaiban" not in user_info or user_info["cuentaiban"] == "" else "true"

                            return {"status": "true", "message": "Login exitoso", "propietario": propietario}

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

    def add_house(self, data):
        """ Registra una casa en la base de datos con encriptación. """
        try:
            username = self.encrypt(data.get("username", ""))
            description = self.encrypt(data.get("description", ""))
            rules = self.encrypt(data.get("rules", ""))
            price = self.encrypt(data.get("price", ""))
            capacity = self.encrypt(data.get("capacity", ""))
            provincia = self.encrypt(data.get("provincia", ""))
            canton = self.encrypt(data.get("canton", ""))
            location = self.encrypt(data.get("location", ""))
            photos_list = data.get("housePhotoBase64", [])
            amenities_list = data.get("amenities", [])

            if not isinstance(photos_list, list):
                print("ERROR: `housePhotoBase64` no es una lista")
                return {"status": "error", "message": "Formato incorrecto para imágenes"}
            if not isinstance(amenities_list, list):
                print("ERROR: `amenities` no es una lista")
                return {"status": "error", "message": "Formato incorrecto para amenidades"}

            # Crear el directorio para las imágenes si no existe
            image_directory = "house_images"
            os.makedirs(image_directory, exist_ok=True)

            # Identificador único para la casa
            house_id = int(time.time())

            # Guardar cada imagen
            image_paths = []
            for index, photo_base64 in enumerate(photos_list):
                try:
                    image_data = base64.b64decode(photo_base64)
                    image_path = os.path.join(image_directory, f"house_{house_id}_{index}.jpg")
                    
                    with open(image_path, "wb") as image_file:
                        image_file.write(image_data)

                    image_paths.append(image_path)

                except Exception as e:
                    print(f"Error al guardar la imagen {index}: {e}")
                    return {"status": "error", "message": "Error al guardar las imágenes"}

            # Guardar la información en database.txt con encriptación
            try:
                with open("database_houses.txt", "a", encoding="utf-8") as db_file:
                    db_file.write(f"username: {username}\n")
                    db_file.write(f"description: {description}\n")
                    db_file.write(f"rules: {rules}\n")
                    db_file.write(f"price: {price}\n")
                    db_file.write(f"capacity: {capacity}\n")
                    db_file.write(f"provincia: {provincia}\n")
                    db_file.write(f"canton: {canton}\n")
                    db_file.write(f"location: {location}\n")

                    # Guardar las rutas de las imágenes en database.txt
                    for i, image_path in enumerate(image_paths):
                        db_file.write(f"photo_{i}: {image_path}\n")

                    # Guardar amenidades en formato JSON para facilitar su recuperación
                    db_file.write(f"amenities: {json.dumps(amenities_list, ensure_ascii=False)}\n")

                    db_file.write(f"{'-' * 20}\n")

                    

            except Exception as e:
                print(f"Error al guardar la casa en database.txt: {e}")
                return {"status": "error", "message": "Error al registrar la casa"}

            print(f" Casa guardada correctamente con ID {house_id}")
            return {"status": "true", "message": "Casa añadida correctamente"}

        except Exception as e:
            print(f"Error al registrar casa: {e}")
            return {"status": "error", "message": "Error al registrar la casa"}
if __name__ == "__main__":
    ChatServer()


