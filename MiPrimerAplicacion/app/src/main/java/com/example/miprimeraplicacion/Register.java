package com.example.miprimeraplicacion;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

public class Register extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;
    private ImageView imageViewPhoto;
    private Button buttonUploadPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registro);

        // Inicializar los elementos de la vista
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
        imageViewPhoto = findViewById(R.id.imageViewPhoto);
        buttonUploadPhoto = findViewById(R.id.buttonUploadPhoto);

        // Manejar el clic del botón "Seleccionar Foto"
        buttonUploadPhoto.setOnClickListener(v -> showPhotoOptions());

        // Manejar el clic del botón de registro
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
            
            if (firstName.isEmpty() || lastName.isEmpty() || address.isEmpty() || nickname.isEmpty() ||
                    password.isEmpty() || hobby.isEmpty() || card.isEmpty() || houseStyle.isEmpty() || transport.isEmpty()) {
                Toast.makeText(Register.this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isVisaOrMastercard(card)) {
                Toast.makeText(Register.this, "El número de tarjeta no es válido. Debe ser Visa o Mastercard.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Enviar los datos de registro al servidor
            MainActivity.sendAndReceiveRegister(
                    firstName, lastName, address, nickname, password, hobby, card, houseStyle, transport,
                    new MainActivity.RegisterResponseCallback() {
                        @Override
                        public void onSuccess(String response) {
                            runOnUiThread(() -> {
                                Toast.makeText(Register.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Register.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> Toast.makeText(Register.this, "Error: " + error, Toast.LENGTH_SHORT).show());
                        }
                    }
            );

        });
    }

    /**
     * Mostrar opciones al usuario: "Cargar desde galería" o "Tomar una foto"
     */
    private void showPhotoOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona una opción");
        builder.setItems(new CharSequence[]{"Cargar desde galería", "Tomar una foto", "Cancelar"}, (dialog, which) -> {
            switch (which) {
                case 0: // Cargar desde galería
                    openGallery();
                    break;
                case 1: // Tomar una foto
                    checkAndRequestCameraPermission();
                    break;
                case 2: // Cancelar
                    dialog.dismiss();
                    break;
            }
        });
        builder.show();
    }

    /**
     * Abrir la galería para seleccionar una foto
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    /**
     * Verificar y solicitar permiso para la cámara
     */
    private void checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            openCamera();
        }
    }

    /**
     * Abrir la cámara para tomar una foto
     */
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(intent);
        } else {
            Toast.makeText(this, "No se puede abrir la cámara", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Manejar el resultado de la galería
     */
    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    Glide.with(this)
                            .load(selectedImageUri)
                            .circleCrop()
                            .placeholder(R.drawable.circular_image)
                            .into(imageViewPhoto);

                    Toast.makeText(this, "Foto cargada desde la galería", Toast.LENGTH_SHORT).show();
                }
            });

    /**
     * Manejar el resultado de la cámara
     */
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                    Glide.with(this)
                            .load(photo)
                            .circleCrop()
                            .placeholder(R.drawable.circular_image)
                            .into(imageViewPhoto);

                    Toast.makeText(this, "Foto capturada", Toast.LENGTH_SHORT).show();
                }
            });

    /**
     * Validar si el número de tarjeta es Visa o Mastercard
     */
    private boolean isVisaOrMastercard(String cardNumber) {
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

    /**
     * Manejar la respuesta del usuario a los permisos solicitados
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
