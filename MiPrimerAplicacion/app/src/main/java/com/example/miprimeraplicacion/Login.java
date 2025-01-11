package com.example.miprimeraplicacion;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

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
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(Login.this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(Login.this, "Bienvenido, " + username + "!", Toast.LENGTH_SHORT).show();

            }
        });
    }
}
