package com.example.miprimeraplicacion;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;

import org.json.JSONObject;


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
        Button btnTest = findViewById(R.id.button13);
        ImageButton buttonRegisterGoogle = findViewById(R.id.buttonRegisterGoogle);
        ImageButton buttonRegisterFacebook = findViewById(R.id.buttonRegisterFacebook);

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
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean propietario = jsonResponse.optString("propietario", "false").equalsIgnoreCase("true");

                            Toast.makeText(Login.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();

                            // Redirigir según el tipo de usuario
                            Intent intent;
                            if (propietario) {
                                intent = new Intent(Login.this, LoginActivity.class); // Redirigir a LoginActivity si es propietario
                            } else {
                                intent = new Intent(Login.this, ViewHouseActivity.class); // Redirigir a ExitActivity si es inquilino
                            }
                            // Pasar el nombre de usuario a la siguiente actividad
                            intent.putExtra("USERNAME", username);
                            startActivity(intent);
                            finish();
                        } catch (Exception e) {
                            Toast.makeText(Login.this, "Error al procesar la respuesta del servidor.", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
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

        btnTest.setOnClickListener(v -> {
            // Acción para el botón de Registrarse
            Intent intent = new Intent(Login.this, TestLeds.class);
            startActivity(intent);
        });

        buttonRegisterGoogle.setOnClickListener(v -> {
            // Acción para registrar con Google
            Toast.makeText(Login.this, "Registrar con Google", Toast.LENGTH_SHORT).show();
            // Acción para el botón de Registrarse
            Intent intent = new Intent(Login.this, ReserveHouseActivity.class);
            startActivity(intent);
        });

        buttonRegisterFacebook.setOnClickListener(v -> {
            // Acción para registrar con Facebook
            Toast.makeText(Login.this, "Registrar con Facebook", Toast.LENGTH_SHORT).show();
        });


    }
}
