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
import org.json.JSONArray; // Asegúrate de importar esto


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
                socket = new Socket("192.168.68.104", 1717); //Daniel

                //socket = new Socket("192.168.0.106", 1717); //Yaritza
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new Scanner(socket.getInputStream());

                System.out.println("Estado del socket al conectar:");
                checkSocketStatus();

                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Conectado al servidor", Toast.LENGTH_SHORT).show();
                    // Redirigir a LoginActivity
                    Intent intent = new Intent(MainActivity.this, Login.class);
                    startActivity(intent);

                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error de conexión: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();

    }

    /**
     * Enviar una solicitud para obtener los datos de las casas disponibles.
     */
    public static void requestHouseData(HouseDataCallback callback) {
        new Thread(() -> {
            try {
                if (socket == null || socket.isClosed()) {
                    callback.onError("Socket no inicializado o cerrado.");
                    return;
                }

                // Enviar solicitud al servidor
                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("action", "get_houses");

                out.println(jsonRequest.toString());
                out.flush();
                System.out.println("📤 Solicitud de casas enviada: " + jsonRequest.toString());

                // Leer la respuesta del servidor
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String response = reader.readLine();

                if (response != null) {
                    System.out.println("📥 Respuesta recibida: " + response);

                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        boolean status = jsonResponse.optBoolean("status", false);

                        if (status) {
                            JSONArray housesArray = jsonResponse.getJSONArray("houses");
                            callback.onSuccess(housesArray);
                        } else {
                            callback.onError(jsonResponse.optString("message", "Error desconocido"));
                        }

                    } catch (Exception e) {
                        System.err.println("LOG: Error al analizar el JSON: " + e.getMessage());
                        callback.onError("Respuesta inválida del servidor.");
                    }
                } else {
                    callback.onError("No se recibió respuesta del servidor.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }

    public interface HouseDataCallback {
        void onSuccess(JSONArray houses);
        void onError(String error);
    }

    public static void sendAndReceiveRegister(String firstName, String lastName, String address, String username, String password, String hobby, String cardnumber, String cardexpiry, String cardcvv, String cuentaiban, String houseStyle, String transport,  String birthDate, String userType, String photoBase64, RegisterResponseCallback callback) {

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
                json.put("cardnumber", cardnumber);
                json.put("cardexpiry", cardexpiry);
                json.put("cardcvv", cardcvv);
                json.put("cuentaiban", cuentaiban);
                json.put("houseStyle", houseStyle);
                json.put("transport", transport);
                json.put("birthDate", birthDate);
                json.put("userType", userType);
                json.put("photo", photoBase64);


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

                        if (status) {
                            // Enviar el JSON completo en lugar de solo el mensaje
                            callback.onSuccess(jsonResponse.toString());
                        } else {
                            callback.onError(jsonResponse.optString("message", "Error desconocido"));
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
    public static void sendHouseData(String username, String description, String rules, String price, String capacity, String provincia,String canton,String location, JSONArray housePhotoBase64, JSONArray jsonAmenities, RegisterResponseCallback callback) {
        new Thread(() -> {
            try {
                if (socket == null || socket.isClosed()) {
                    callback.onError("Socket no inicializado o cerrado.");
                    return;
                }

                // Construir el JSON correctamente
                JSONObject json = new JSONObject();
                json.put("action", "addHouse");
                json.put("username", username);
                json.put("description", description);
                json.put("rules", rules);
                json.put("price", price);
                json.put("capacity", capacity);
                json.put("provincia", provincia);
                json.put("canton", canton);
                json.put("location", location);
                json.put("housePhotoBase64", housePhotoBase64);  // ✅ Ahora es un JSONArray real
                json.put("amenities", jsonAmenities);

                String houseDataMessage = json.toString();
                out.println(houseDataMessage);
                out.flush();
                System.out.println("📤 JSON enviado al servidor: " + houseDataMessage);

                // Recibir la respuesta del servidor
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String response = reader.readLine();

                if (response != null) {
                    System.out.println("📥 Respuesta recibida: " + response);

                    JSONObject jsonResponse = new JSONObject(response);
                    boolean status = jsonResponse.optBoolean("status", false);
                    String message = jsonResponse.optString("message", "Sin mensaje");

                    if (status) {
                        callback.onSuccess(message);
                    } else {
                        callback.onError(message);
                    }
                } else {
                    callback.onError("No se recibió respuesta del servidor.");
                }
            } catch (Exception e) {
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }




    public static void sendHabitacionLuz(String habitacion) {
        new Thread(() -> {
            try {
                if (socket == null || socket.isClosed()) {
                    System.err.println("Socket no inicializado o cerrado.");
                    return;
                }

                // Crear el mensaje en formato JSON
                JSONObject jsonMessage = new JSONObject();
                jsonMessage.put("action", "luces");  // Acción que el servidor espera
                jsonMessage.put("habitacion", habitacion);

                // Convertir el mensaje a String
                String message = jsonMessage.toString();

                // Enviar el mensaje
                out.println(message);
                out.flush();
                System.out.println("LOG: Datos enviados: " + message);

            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error al enviar el mensaje: " + e.getMessage());
            }
        }).start();
    }



    public static void reserveHouse(String userloged,String houseId, String checkIn, String checkOut, LoginResponseCallback callback) {
        new Thread(() -> {
            try {
                if (socket == null || socket.isClosed()) {
                    callback.onError("Socket no inicializado o cerrado.");
                    return;
                }

                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("action", "reserveHouse");
                jsonRequest.put("userloged",userloged);
                jsonRequest.put("houseId", houseId);
                jsonRequest.put("checkIn", checkIn);
                jsonRequest.put("checkOut", checkOut);

                JSONObject jsonRequestWhatsapp =new JSONObject();
                jsonRequestWhatsapp.put("action","notificacionWhatsapp");
                jsonRequestWhatsapp.put("mensaje","se ha realizado la reserva con exito! para "+checkIn);
                jsonRequestWhatsapp.put("telefono","89869107");

                out.println(jsonRequest.toString());
                out.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String response = reader.readLine();

                if (response != null) {
                    callback.onSuccess(response);
                    //envia el mensaje de confirmacion a whatsapp
                    out.println(jsonRequestWhatsapp.toString());
                    out.flush();
                    BufferedReader reader2 = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                } else {
                    callback.onError("No se recibió respuesta del servidor.");
                }
            } catch (Exception e) {
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }


    public static void getBlockedDates(String houseId, LoginResponseCallback callback) {
        new Thread(() -> {
            try {
                if (socket == null || socket.isClosed()) {
                    callback.onError("Socket no inicializado o cerrado.");
                    return;
                }

                // Construir solicitud JSON
                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("action", "getBlockedDates");
                jsonRequest.put("houseId", houseId);

                // Enviar la solicitud al servidor
                out.println(jsonRequest.toString());
                out.flush();

                // Recibir respuesta del servidor
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String response = reader.readLine();

                if (response != null) {
                    callback.onSuccess(response);
                } else {
                    callback.onError("No se recibió respuesta del servidor.");
                }
            } catch (Exception e) {
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