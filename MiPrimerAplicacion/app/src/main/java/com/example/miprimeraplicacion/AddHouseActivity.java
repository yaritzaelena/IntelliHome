package com.example.miprimeraplicacion;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AddHouseActivity extends AppCompatActivity {

    private ImageView imagePreview;
    private ImageButton buttonAddPhoto;
    private Button buttonOpenMap, buttonRegister;
    private TextView textViewCoordinates;
    private Bitmap housePhotoBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_house);

        // Inicializar elementos

        buttonAddPhoto = findViewById(R.id.buttonAddPhoto);
        buttonOpenMap = findViewById(R.id.buttonOpenMap);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewCoordinates = findViewById(R.id.textViewCoordinates);

        // Botón para seleccionar imagen
        buttonAddPhoto.setOnClickListener(v -> showPhotoOptions());

        // Botón para seleccionar ubicación en el mapa
        buttonOpenMap.setOnClickListener(v -> {
            Intent mapIntent = new Intent(AddHouseActivity.this, MapActivity.class);
            startActivityForResult(mapIntent, 1);
        });

        // Botón para registrar la casa
        buttonRegister.setOnClickListener(v -> registerHouse());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            double latitude = data.getDoubleExtra("latitude", 0);
            double longitude = data.getDoubleExtra("longitude", 0);
            textViewCoordinates.setText("Coordenadas: " + latitude + ", " + longitude);
        }
    }

    private void showPhotoOptions() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImage = result.getData().getData();
                    Glide.with(this).load(selectedImage).into(imagePreview);
                    try {
                        housePhotoBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    private void registerHouse() {
        EditText editTextDescription = findViewById(R.id.editTextDescription);
        EditText editTextRules = findViewById(R.id.editTextRules);
        EditText editTextPrice = findViewById(R.id.editTextPrice);
        EditText editTextCapacity = findViewById(R.id.editTextCapacity);

        String description = editTextDescription.getText().toString().trim();
        String rules = editTextRules.getText().toString().trim();
        String price = editTextPrice.getText().toString().trim();
        String capacity = editTextCapacity.getText().toString().trim();
        String location = textViewCoordinates.getText().toString().trim();
        String housePhotoBase64 = housePhotoBitmap != null ? encodeToBase64(housePhotoBitmap) : "";

        if (description.isEmpty() || rules.isEmpty() || price.isEmpty() || capacity.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Enviar los datos de la casa al servidor
        MainActivity.sendHouseData(description, rules, price, capacity, location, housePhotoBase64,
                new MainActivity.RegisterResponseCallback() {
                    @Override
                    public void onSuccess(String response) {
                        runOnUiThread(() -> {
                            Toast.makeText(AddHouseActivity.this, "Casa registrada exitosamente", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> Toast.makeText(AddHouseActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show());
                    }
                });
    }

    private String encodeToBase64(Bitmap image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}

