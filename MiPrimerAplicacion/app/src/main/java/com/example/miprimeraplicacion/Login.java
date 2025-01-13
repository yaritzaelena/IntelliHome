package com.example.miprimeraplicacion;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.PrintWriter;
import java.util.Scanner;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login); // xml

        // elementos del xml
        EditText usernameEditText = findViewById(R.id.editTextUsername);
        EditText passwordEditText = findViewById(R.id.editTextPassword);
        Button loginButton = findViewById(R.id.buttonLogin);

        // Configura el evento para el botón de "Aceptar"
        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(Login.this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            MainActivity.sendLoginData(username, password, new MainActivity.LoginResponseCallback() {
                @Override
                public void onSuccess(String response) {
                    // Mostrar la respuesta recibida directamente
                    System.out.println("Respuesta cruda recibida: " + response);
                    runOnUiThread(() -> Toast.makeText(Login.this, "Respuesta del servidor: " + response, Toast.LENGTH_LONG).show());
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> Toast.makeText(Login.this, "Error: " + error, Toast.LENGTH_LONG).show());
                }
            });
        });


    }
}