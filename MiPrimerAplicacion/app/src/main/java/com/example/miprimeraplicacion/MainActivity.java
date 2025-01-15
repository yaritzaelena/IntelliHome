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

import java.io.BufferedReader;
import java.io.InputStreamReader;



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
                //socket = new Socket("192.168.0.152", 1717); //Olman
                socket = new Socket("192.168.0.152", 1717); //Yaritza
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

    public static void sendAndReceiveRegister(String firstName, String lastName, String address, String username, String password, String hobby, String card, String houseStyle, String transport, RegisterResponseCallback callback) {
        new Thread(() -> {
            try {
                if (socket == null || socket.isClosed()) {
                    callback.onError("Socket no inicializado o cerrado.");
                    return;
                }

                // Construir y enviar los datos
                JSONObject json = new JSONObject();
                json.put("action", "register");
                json.put("firstName", firstName);
                json.put("lastName", lastName);
                json.put("address", address);
                json.put("username", username);
                json.put("password", password);
                json.put("hobby", hobby);
                json.put("card", card);
                json.put("houseStyle", houseStyle);
                json.put("transport", transport);

                String registerMessage = json.toString();
                out.println(registerMessage);
                out.flush();
                System.out.println("LOG: Datos de registro enviados: " + registerMessage);

                // Recibir la respuesta
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String response = reader.readLine();

                if (response != null) {
                    System.out.println("LOG: Respuesta recibida: " + response);

                    // Validar el JSON recibido
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        boolean status = jsonResponse.optBoolean("status", false); // Default a false si no está el campo
                        String message = jsonResponse.optString("message", "Sin mensaje");

                        if (status) {
                            // Éxito
                            callback.onSuccess(message);
                        } else {
                            // Error
                            callback.onError(message);
                        }
                    } catch (Exception e) {
                        System.err.println("LOG: Error al analizar el JSON: " + e.getMessage());
                        callback.onError("Respuesta inválida del servidor.");
                    }
                } else {
                    System.err.println("LOG: Respuesta nula o vacía del servidor.");
                    callback.onError("No se recibió respuesta del servidor.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }

    public interface RegisterResponseCallback {
        void onSuccess(String response);
        void onError(String error);
    }



    public static void sendAndReceive(String username, String password, LoginResponseCallback callback) {
        new Thread(() -> {
            try {
                if (socket == null || socket.isClosed()) {
                    callback.onError("Socket no inicializado o cerrado.");
                    return;
                }

                // Construir y enviar los datos
                String loginMessage = "{\"action\":\"login\",\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
                out.println(loginMessage);
                out.flush();
                System.out.println("LOG: Datos enviados: " + loginMessage);

                // Recibir la respuesta
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String response = reader.readLine();

                if (response != null) {
                    System.out.println("LOG: Respuesta recibida: " + response);

                    // Validar el JSON recibido
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        boolean status = jsonResponse.optBoolean("status", false); // Default a false si no está el campo
                        String message = jsonResponse.optString("message", "Sin mensaje");

                        if (status) {
                            // Éxito
                            callback.onSuccess(message);
                        } else {
                            // Error
                            callback.onError(message);
                        }
                    } catch (Exception e) {
                        System.err.println("LOG: Error al analizar el JSON: " + e.getMessage());
                        callback.onError("Respuesta inválida del servidor.");
                    }
                } else {
                    System.err.println("LOG: Respuesta nula o vacía del servidor.");
                    callback.onError("No se recibió respuesta del servidor.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }


    public interface LoginResponseCallback {
        void onSuccess(String response);
        void onError(String error);
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