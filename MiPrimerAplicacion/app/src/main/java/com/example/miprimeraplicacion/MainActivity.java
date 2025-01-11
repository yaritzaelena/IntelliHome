package com.example.miprimeraplicacion;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


// Recordar que dar los permisos del HW para utilizar los componentes por ejemplo la red
// Esto se hace en el archivo AndroidManifest

/**
 * Creado por Jason Leitón Jiménez para guía del curso de Principio de Modelado
 * Esta clase es la que permite enviar y recibir mensajes desde y hacia el servidor en python
 * Recordar que la gui está en res
 */
public class MainActivity extends AppCompatActivity {

    private EditText editTextMessage;
    private TextView textViewChat;
    private Socket socket;
    private PrintWriter out;
    private Scanner in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextMessage = findViewById(R.id.editTextMessage);
        textViewChat = findViewById(R.id.textViewChat);
        Button buttonSend = findViewById(R.id.buttonSend);
        Button buttonExit = findViewById(R.id.buttonExit);

        // Iniciar el hilo para conectarse al servidor y recibir mensajes
        new Thread(() -> {
            try {
                // Cambiar a la dirección IP de su servidor
                socket = new Socket("172.17.53.129", 1717);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new Scanner(socket.getInputStream());
                new Thread(() -> {
                    while (true) {
                        // Escuchar continuamente los mensajes del servidor
                        if (in.hasNextLine()) {
                            String message = in.nextLine();
                            runOnUiThread(() -> { // actualiza el la gui en un hilo
                                textViewChat.append("Servidor: " + message + "\n");
                            });
                        }
                    }


                }).start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        buttonSend.setOnClickListener(view -> { // mapeo del boton enviar
            String message = editTextMessage.getText().toString();
            sendMessage(message); // Llama a la función para enviar el mensaje
            textViewChat.append("Yo: " + message + "\n");
            editTextMessage.setText("");
        });

        buttonExit.setOnClickListener(view -> { // mapeo del boton exit
            Intent intent = new Intent(MainActivity.this, ExitActivity.class);
            startActivity(intent);
        });
    }

    private void sendMessage(String message) {
        new Thread(() -> { // un hilo por a parte
            try {
                if (out != null) {
                    out.println(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void onDestroy() { // lo que se ejecuta cuando termina, se sale de la aplicación
        super.onDestroy();
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}