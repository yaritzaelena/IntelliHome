package com.example.miprimeraplicacion;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

public class TestLeds extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_leds); // Se carga el layout

        // Asociar los botones
        Button botonRegresar = findViewById(R.id.button4);
        Button btnCocina = findViewById(R.id.button5);
        Button btnSala = findViewById(R.id.button6);
        Button btnGaraje = findViewById(R.id.button7);
        Button btnCuarto1 = findViewById(R.id.button8);
        Button btnCuarto2 = findViewById(R.id.button9);
        Button btnBano1 = findViewById(R.id.button10);
        Button btnBano2 = findViewById(R.id.button11);
        Button btnCorredor = findViewById(R.id.button12);

        // Establecer un onClickListener para cada uno
        botonRegresar.setOnClickListener(v -> {
            // Llama a una función cuando el botón Regresar es presionado
            botonRegresarFunction();
        });

        btnCocina.setOnClickListener(v -> {
            // Llama a una función cuando el botón Cocina es presionado
            btnCocinaFunction();
        });

        btnSala.setOnClickListener(v -> {
            // Llama a una función cuando el botón Sala es presionado
            btnSalaFunction();
        });

        btnGaraje.setOnClickListener(v -> {
            // Llama a una función cuando el botón Garaje es presionado
            btnGarajeFunction();
        });

        btnCuarto1.setOnClickListener(v -> {
            // Llama a una función cuando el botón Cuarto 1 es presionado
            btnCuarto1Function();
        });

        btnCuarto2.setOnClickListener(v -> {
            // Llama a una función cuando el botón Cuarto 2 es presionado
            btnCuarto2Function();
        });

        btnBano1.setOnClickListener(v -> {
            // Llama a una función cuando el botón Baño 1 es presionado
            btnBano1Function();
        });

        btnBano2.setOnClickListener(v -> {
            // Llama a una función cuando el botón Baño 2 es presionado
            btnBano2Function();
        });

        btnCorredor.setOnClickListener(v -> {
            // Llama a una función cuando el botón Corredor es presionado
            btnCorredorFunction();
        });
    }

    // Funciones asociadas a cada botón
    private void botonRegresarFunction() {
        // Lógica para el botón Regresar
        Intent intent = new Intent(TestLeds.this, Login.class);
        startActivity(intent);
    }

    private void btnCocinaFunction() {
        // Lógica para el botón Cocina
        MainActivity.sendHabitacionLuz("COCINA");
    }

    private void btnSalaFunction() {
        // Lógica para el botón Sala
        MainActivity.sendHabitacionLuz("SALA");
    }

    private void btnGarajeFunction() {
        // Lógica para el botón Garaje
        MainActivity.sendHabitacionLuz("GARAJE");
    }

    private void btnCuarto1Function() {
        // Lógica para el botón Cuarto 1
        MainActivity.sendHabitacionLuz("CUARTO1");
    }

    private void btnCuarto2Function() {
        // Lógica para el botón Cuarto 2
        MainActivity.sendHabitacionLuz("CUARTO2");
    }

    private void btnBano1Function() {
        // Lógica para el botón Baño 1
        MainActivity.sendHabitacionLuz("BANO1");
    }

    private void btnBano2Function() {
        // Lógica para el botón Baño 2
        MainActivity.sendHabitacionLuz("BANO2");
    }

    private void btnCorredorFunction() {
        // Lógica para el botón Corredor
        MainActivity.sendHabitacionLuz("CORREDOR");
    }

}
