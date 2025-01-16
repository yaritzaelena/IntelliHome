package com.example.miprimeraplicacion;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;


public class Login extends AppCompatActivity {

    @SuppressLint("ClickableViewAccessibility")
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
                int drawableEnd = 2; // Ícono de la derecha

                // Compara la posición X del toque con la ubicación del ícono
                if (event.getRawX() >= (passwordEditText.getRight() - passwordEditText.getCompoundDrawables()[drawableEnd].getBounds().width())) {
                    if (passwordEditText.getTransformationMethod() instanceof HideReturnsTransformationMethod) {
                        // Si está visible, volver a rombos
                        passwordEditText.setTransformationMethod(new DiamondTransformationMethod());
                        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_eye_closed, 0);
                    } else {
                        // Mostrar la contraseña en texto normal
                        passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_eye_open, 0);
                    }
                    passwordEditText.setSelection(passwordEditText.getText().length());

                    // Llamar a performClick() explícitamente
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
