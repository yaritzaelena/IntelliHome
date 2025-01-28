package com.example.miprimeraplicacion;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;

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
        });

        // Botón para registrar una casa
        registarHouseButton.setOnClickListener(v -> {
            // Acción para el botón de Login
            Intent intent = new Intent(LoginActivity.this, AddHouseActivity.class);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
        });

        // Botón para ver las casas disponibles
        housesActivityButton.setOnClickListener(v -> {
            // Llamamos a requestHouseData antes de abrir ViewHouseActivity
            MainActivity.requestHouseData(new MainActivity.HouseDataCallback() {
                @Override
                public void onSuccess(JSONArray houses) {
                    Intent intent = new Intent(LoginActivity.this, ViewHouseActivity.class);
                    intent.putExtra("USERNAME", username);
                    intent.putExtra("houses_data", houses.toString());
                    startActivity(intent);
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Error al obtener casas: " + error, Toast.LENGTH_LONG).show());
                }
            });
        });

    }
}
