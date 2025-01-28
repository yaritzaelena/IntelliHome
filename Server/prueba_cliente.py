import socket
import json

def request_reservations(house_id):
    try:
        # Conectar al servidor
        client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client_socket.connect(('192.168.0.152', 1717))  # Cambia la IP y puerto si es necesario

        # Crear la solicitud JSON
        request_data = {
            "action": "getBlockedDates",
            "house_id": house_id
        }

        # Enviar la solicitud al servidor
        client_socket.sendall(json.dumps(request_data).encode('utf-8'))
        print(f"📩 Solicitud enviada: {request_data}")

        # Recibir la respuesta del servidor
        response = client_socket.recv(4096).decode('utf-8')
        response_json = json.loads(response)

        print(f"📩 Respuesta del servidor: {json.dumps(response_json, indent=4, ensure_ascii=False)}")

    except Exception as e:
        print(f"❌ Error: {e}")

    finally:
        client_socket.close()

# Ejecutar la prueba
if __name__ == "__main__":
    request_reservations("1738039141")
