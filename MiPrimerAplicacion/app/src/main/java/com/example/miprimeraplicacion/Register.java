package com.example.miprimeraplicacion;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

public class Register extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registro);

        // elementos xml
        EditText firstNameEditText = findViewById(R.id.editTextFirstName);
        EditText lastNameEditText = findViewById(R.id.editTextLastName);
        EditText addressEditText = findViewById(R.id.editTextAddress);
        EditText nicknameEditText = findViewById(R.id.editTextNickname);
        EditText passwordEditText = findViewById(R.id.editTextPassword);
        EditText hobbyEditText = findViewById(R.id.editTextHobby);
        EditText cardEditText = findViewById(R.id.editTextCard);
        EditText houseStyleEditText = findViewById(R.id.editTextHouseStyle);
        EditText transportEditText = findViewById(R.id.editTextTransport);
        Button registerButton = findViewById(R.id.buttonRegister);

        // acciones

        registerButton.setOnClickListener(v -> {
            String firstName = firstNameEditText.getText().toString().trim();
            String lastName = lastNameEditText.getText().toString().trim();
            String address = addressEditText.getText().toString().trim();
            String nickname = nicknameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String hobby = hobbyEditText.getText().toString().trim();
            String card = cardEditText.getText().toString().trim();
            String houseStyle = houseStyleEditText.getText().toString().trim();
            String transport = transportEditText.getText().toString().trim();

            // validar campos - falta contraseña
            if (firstName.isEmpty() || lastName.isEmpty() || address.isEmpty() || nickname.isEmpty() ||
                    password.isEmpty() || hobby.isEmpty() || card.isEmpty() || houseStyle.isEmpty() || transport.isEmpty()) {
                Toast.makeText(Register.this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
            } else if (!isVisaOrMastercard(card)) {
                Toast.makeText(Register.this, "El número de tarjeta no es válido. Debe ser Visa o Mastercard.", Toast.LENGTH_SHORT).show();
            } else {
                System.out.println("Estado del socket antes de enviar datos:");
                MainActivity.checkSocketStatus();
                MainActivity.sendUserData("register",firstName, lastName, address, nickname, password, hobby, card, houseStyle, transport);
                Toast.makeText(Register.this, "Registro enviado: " + nickname, Toast.LENGTH_SHORT).show();

                // Redirigir a la pantalla de inicio (LoginActivity)
                Intent intent = new Intent(Register.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Finaliza la actividad actual para que no se pueda volver con el botón atrás
            }
        });

    }

    // Visa o Mastercard validacion
    private boolean isVisaOrMastercard(String cardNumber) {
        // Visa comienza con 4, Mastercard comienza con 51-55
        if (cardNumber.length() == 16) {
            if (cardNumber.startsWith("4")) {
                return true; // Es Visa
            } else if (cardNumber.startsWith("51") || cardNumber.startsWith("52") ||
                    cardNumber.startsWith("53") || cardNumber.startsWith("54") || cardNumber.startsWith("55")) {
                return true; // Es Mastercard
            }
        }
        return false;
    }
}

