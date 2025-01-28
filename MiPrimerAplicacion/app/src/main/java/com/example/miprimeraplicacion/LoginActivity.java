package com.example.miprimeraplicacion;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Recibir el nombre de usuario
        String username = getIntent().getStringExtra("USERNAME");

        // Mostrar mensaje de bienvenida
        Toast.makeText(this, "Bienvenido, " + username, Toast.LENGTH_LONG).show();

        ImageView imageView = findViewById(R.id.imageView2);
        Button registarHouseButton = findViewById(R.id.button);
        Button housesActivityButton = findViewById(R.id.button3);

        // Configura una acción para la imagen
        imageView.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "Imagen presionada", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, OwnerHousesActivity.class);
            intent.putExtra("USERNAME", username);
            startActivity(intent); // Llama a la actividad Login
            Toast.makeText(LoginActivity.this, "LLAMADA", Toast.LENGTH_SHORT).show();

        });

        // Botón de Login
        registarHouseButton.setOnClickListener(v -> {
            // Acción para el botón de Login
            Intent intent = new Intent(LoginActivity.this, AddHouseActivity.class);
            intent.putExtra("USERNAME", username);
            startActivity(intent); // Llama a la actividad Login
        });

        // Botón de Registrarse
        housesActivityButton.setOnClickListener(v -> {
            // Acción para el botón de Registrarse
            Intent intent = new Intent(LoginActivity.this, ExitActivity.class);
            startActivity(intent);
        });

    }
}
