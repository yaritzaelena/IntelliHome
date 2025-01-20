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


        ImageView imageView = findViewById(R.id.imageView2);
        Button registarHouseButton = findViewById(R.id.button);
        Button housesActivityButton = findViewById(R.id.button3);

        // Configura una acción para la imagen
        imageView.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "Imagen presionada", Toast.LENGTH_SHORT).show();
        });

        // Botón de Login
        registarHouseButton.setOnClickListener(v -> {
            // Acción para el botón de Login
            Intent intent = new Intent(LoginActivity.this, ExitActivity.class);
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
