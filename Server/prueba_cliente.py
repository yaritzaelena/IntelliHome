import socket
import json
import serial

def send_login_request(username, password):
    try:
        # Connect to the server
        client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client_socket.connect(('192.168.0.152', 1717))  # Update with your server's IP and port

        # Prepare the login JSON
        login_data = {
            "action": "login",
            "username": username,
            "password": password
        }

        # Send the data
        client_socket.sendall(json.dumps(login_data).encode('utf-8'))
        print(f"Sent: {login_data}")

        # Receive the response
        response = client_socket.recv(1024).decode('utf-8')
        print(f"Received: {response}")

    except Exception as e:
        print(f"Error: {e}")

    finally:
        client_socket.close()

if __name__ == "__main__":
    # Test the client with valid and invalid credentials
    send_login_request("Olman2020", "1235")  # Example of invalid credentials
    #send_login_request("Olman2020", "1234")  # Replace with valid credentials in your database
