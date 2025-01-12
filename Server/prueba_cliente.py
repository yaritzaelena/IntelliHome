import socket

client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client.connect(("192.168.0.152", 1717))
client.send(b"register;johndoe;1234")
response = client.recv(1024)
print(f"Respuesta del servidor: {response.decode('utf-8')}")
