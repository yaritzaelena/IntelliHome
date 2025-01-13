import socket

client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client.connect(("192.168.0.152", 1717))

message = '{"action":"login","username":"Olman2020","password":"1234"}\n'
client.send(message.encode('utf-8'))

response = client.recv(1024).decode('utf-8')
print("Respuesta del servidor:", response)

client.close()

