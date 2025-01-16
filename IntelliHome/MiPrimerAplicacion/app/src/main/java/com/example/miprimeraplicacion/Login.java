package com.example.miprimeraplicacion;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login); // xml

        EditText usernameEditText = findViewById(R.id.editTextUsername);
        EditText passwordEditText = findViewById(R.id.editTextPassword);
        passwordEditText.setTransformationMethod(new DiamondTransformationMethod());
        Button loginButton = findViewById(R.id.buttonLogin);
        Button registerButton = findViewById(R.id.buttonRegister);

        passwordEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int drawableEnd = 2;//icono de la derecha
                //compara el X de donde se toco con la ubicacion del icono
                if (event.getRawX() >= (passwordEditText.getRight() - passwordEditText.getCompoundDrawables()[drawableEnd].getBounds().width())) {
                    if (passwordEditText.getTransformationMethod() instanceof PasswordTransformationMethod) {
                        passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_open, 0);
                    } else {
                        passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_closed, 0);
                    }
                    passwordEditText.setSelection(passwordEditText.getText().length());
                    v.performClick();
                    return true;
                }
            }
            return false;
        });

        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(Login.this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Llamada centralizada a MainActivity
            MainActivity.sendAndReceive(username, password, new MainActivity.LoginResponseCallback() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        Toast.makeText(Login.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Login.this, ExitActivity.class);
                        startActivity(intent);
                        finish();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> Toast.makeText(Login.this, "Error: " + error, Toast.LENGTH_SHORT).show());
                }
            });
        });

        // Botón de Registrarse
        registerButton.setOnClickListener(v -> {
            // Acción para el botón de Registrarse
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
        });




    }
}
