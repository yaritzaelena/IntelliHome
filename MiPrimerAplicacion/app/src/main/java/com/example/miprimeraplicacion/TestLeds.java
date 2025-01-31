package com.example.miprimeraplicacion;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
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
        View btnCocina = findViewById(R.id.viewCocina);
        View btnSala = findViewById(R.id.viewSala);
        View btnGaraje = findViewById(R.id.viewGaraje);
        View btnCuarto1 = findViewById(R.id.viewHabitacion1);
        View btnCuarto2 = findViewById(R.id.viewHabitacion2);
        View btnBano1 = findViewById(R.id.viewBano);
        View btnBano2 = findViewById(R.id.viewBano2);
        View btnCorredor = findViewById(R.id.viewCorredor);

        Button buttonAlertaSismo = findViewById(R.id.buttonAlertaSismo);
        Button buttonIncendio = findViewById(R.id.buttonIncendio);


        final boolean[] isHighlighted = {false, false, false, false, false, false, false, false};

        // Establecer un onClickListener para cada uno
        botonRegresar.setOnClickListener(v -> {
            // Llama a una función cuando el botón Regresar es presionado
            botonRegresarFunction();
        });


        buttonAlertaSismo.setOnClickListener(v -> mostrarAlertaSismo(6.5)); // Se puede cambiar la magnitud aquí
        buttonIncendio.setOnClickListener(v -> {
            showSensorAlert("¡Emergencia de incendio activada!");
            Toast.makeText(this, "🔥 ¡Emergencia de incendio activada!", Toast.LENGTH_SHORT).show();
        });


        btnCocina.setOnClickListener(v -> {
            if (!isHighlighted[0]) {
                // Añadir contorno verde
                btnCocina.setBackground(createBorderDrawable(10, Color.GREEN));
            } else {
                // Quitar contorno (devolver a fondo transparente)
                btnCocina.setBackgroundColor(Color.TRANSPARENT);
            }
            // Cambiar el estado
            isHighlighted[0] = !isHighlighted[0];
            btnCocinaFunction();
        });

        btnSala.setOnClickListener(v -> {
            if (!isHighlighted[1]) {
                // Añadir contorno verde
                btnSala.setBackground(createBorderDrawable(10, Color.GREEN));
            } else {
                // Quitar contorno (devolver a fondo transparente)
                btnSala.setBackgroundColor(Color.TRANSPARENT);
            }
            // Cambiar el estado
            isHighlighted[1] = !isHighlighted[1];
            btnSalaFunction();
        });

        btnGaraje.setOnClickListener(v -> {
            if (!isHighlighted[2]) {
                // Añadir contorno verde
                btnGaraje.setBackground(createBorderDrawable(10, Color.GREEN));
            } else {
                // Quitar contorno (devolver a fondo transparente)
                btnGaraje.setBackgroundColor(Color.TRANSPARENT);
            }
            // Cambiar el estado
            isHighlighted[2] = !isHighlighted[2];
            btnGarajeFunction();
        });

        btnCuarto1.setOnClickListener(v -> {
            if (!isHighlighted[3]) {
                // Añadir contorno verde
                btnCuarto1.setBackground(createBorderDrawable(10, Color.GREEN));
            } else {
                // Quitar contorno (devolver a fondo transparente)
                btnCuarto1.setBackgroundColor(Color.TRANSPARENT);
            }
            // Cambiar el estado
            isHighlighted[3] = !isHighlighted[3];
            btnCuarto1Function();
        });

        btnCuarto2.setOnClickListener(v -> {
            if (!isHighlighted[4]) {
                // Añadir contorno verde
                btnCuarto2.setBackground(createBorderDrawable(10, Color.GREEN));
            } else {
                // Quitar contorno (devolver a fondo transparente)
                btnCuarto2.setBackgroundColor(Color.TRANSPARENT);
            }
            // Cambiar el estado
            isHighlighted[4] = !isHighlighted[4];
            btnCuarto2Function();
        });

        btnBano1.setOnClickListener(v -> {
            if (!isHighlighted[5]) {
                // Añadir contorno verde
                btnBano1.setBackground(createBorderDrawable(10, Color.GREEN));
            } else {
                // Quitar contorno (devolver a fondo transparente)
                btnBano1.setBackgroundColor(Color.TRANSPARENT);
            }
            // Cambiar el estado
            isHighlighted[5] = !isHighlighted[5];
            btnBano1Function();
        });

        btnBano2.setOnClickListener(v -> {
            if (!isHighlighted[6]) {
                // Añadir contorno verde
                btnBano2.setBackground(createBorderDrawable(10, Color.GREEN));
            } else {
                // Quitar contorno (devolver a fondo transparente)
                btnBano2.setBackgroundColor(Color.TRANSPARENT);
            }
            // Cambiar el estado
            isHighlighted[6] = !isHighlighted[6];
            btnBano2Function();
        });

        btnCorredor.setOnClickListener(v -> {
            if (!isHighlighted[7]) {
                // Añadir contorno verde
                btnCorredor.setBackground(createBorderDrawable(10, Color.GREEN));
            } else {
                // Quitar contorno (devolver a fondo transparente)
                btnCorredor.setBackgroundColor(Color.TRANSPARENT);
            }
            // Cambiar el estado
            isHighlighted[7] = !isHighlighted[7];
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


    private void mostrarAlertaSismo(double magnitud) {
        // Inflar el diseño del popup
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_alerta_sismo, null);

        // Crear el popup
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,  // Asegurar ancho
                ViewGroup.LayoutParams.WRAP_CONTENT, // Ajustar altura
                true
        );

        // ✅ Agregar fondo para que el clic fuera del popup lo cierre
        popupWindow.setBackgroundDrawable(getDrawable(R.drawable.popup_background));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        // Configurar la imagen y el mensaje
        ImageView imageView = popupView.findViewById(R.id.imageViewAlerta);
        imageView.setImageResource(R.mipmap.alerta_sismo); // Cargar la imagen del sismo

        TextView textView = popupView.findViewById(R.id.textViewMensaje);
        textView.setText("⚠️ Alerta: Sismo detectado de magnitud " + magnitud + " ⚠️");

        Button btnCerrar = popupView.findViewById(R.id.buttonCerrar);
        btnCerrar.setOnClickListener(v -> popupWindow.dismiss());

        // ✅ Mostrar el PopupWindow con actualización
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
        popupWindow.update(); // Actualizar el popup para asegurar visibilidad
    }



    private void showSensorAlert(String message) {
        SensorAlertDialog dialog = SensorAlertDialog.newInstance(message);
        dialog.show(getSupportFragmentManager(), "SensorAlert");
    }


    private GradientDrawable createBorderDrawable(int borderWidth, int borderColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE); // Forma rectangular
        drawable.setStroke(borderWidth, borderColor);  // Contorno de grosor y color especificados
        drawable.setColor(Color.TRANSPARENT);          // Fondo transparente
        return drawable;
    }


}
