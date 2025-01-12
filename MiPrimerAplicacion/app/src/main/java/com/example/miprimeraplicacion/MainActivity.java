package com.example.miprimeraplicacion;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

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
    private static Socket socket;
    public static PrintWriter out;
    public static Scanner in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Iniciar el hilo para conectarse al servidor y recibir mensajes
        new Thread(() -> {
            try {
                socket = new Socket("192.168.0.152", 1717);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new Scanner(socket.getInputStream());

                System.out.println("Estado del socket al conectar:");
                checkSocketStatus();

                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Conectado al servidor", Toast.LENGTH_SHORT).show();
                    // Redirigir a LoginActivity
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);

                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error de conexión: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();



    }

    public static void sendUserData(String action, String firstName, String lastName, String address, String username, String password, String hobby, String card, String houseStyle, String transport) {
        new Thread(() -> {
            try {
                if (socket == null || socket.isClosed()) {
                    System.err.println("Error: El socket no está inicializado o está cerrado.");
                    return;
                }

                if (out == null) {
                    System.err.println("Error: `out` no está inicializado.");
                    return;
                }

                // Construir el JSON
                JSONObject json = new JSONObject();
                json.put("action", action);
                json.put("firstName", firstName);
                json.put("lastName", lastName);
                json.put("address", address);
                json.put("username", username);
                json.put("password", password);
                json.put("hobby", hobby);
                json.put("card", card);
                json.put("houseStyle", houseStyle);
                json.put("transport", transport);


                // Convertir a cadena y enviar
                String jsonString = json.toString();
                System.out.println("Preparando mensaje JSON para enviar: " + jsonString);

                out.println(jsonString);
                out.flush();
                System.out.println("Mensaje JSON enviado: " + jsonString);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }




    public static void checkSocketStatus() {
        if (socket == null) {
            System.out.println("Socket no inicializado.");
        } else if (socket.isClosed()) {
            System.out.println("Socket está cerrado.");
        } else if (!socket.isConnected()) {
            System.out.println("Socket no está conectado.");
        } else {
            System.out.println("Socket está abierto y conectado.");
        }
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (out != null) {
                out.close();
                System.out.println("Salida (`out`) cerrada.");
            }
            if (in != null) {
                in.close();
                System.out.println("Entrada (`in`) cerrada.");
            }
            if (socket != null) {
                socket.close();
                System.out.println("Socket cerrado.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}