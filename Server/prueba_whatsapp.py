from twilio.rest import Client

account_sid = 'ACbb736c2252797ba5690a98bbb1a029f1'
auth_token = 'fa37fae92c4fda8a67a9ca5d8d199521'
client = Client(account_sid, auth_token)

mensajePara='whatsapp:+506'+'89869107'
mensaje="Esto es una prueba"
print("whatsapp para:\n"+mensajePara)

mensaje=client.messages.create(
    body=mensaje,
    from_='whatsapp:+14155238886',
    to=mensajePara
)

print(mensaje.sid)