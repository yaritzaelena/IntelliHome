package com.example.miprimeraplicacion;
import android.view.LayoutInflater;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import android.Manifest;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.view.View;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Spinner;
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
    private CheckBox checkBoxTerms;
    private Button registerButton;

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
        Spinner spinnerHouseStyle = findViewById(R.id.spinnerHouseStyle);
        EditText transportEditText = findViewById(R.id.editTextTransport);
        imageViewPhoto = findViewById(R.id.imageViewPhoto);
        buttonUploadPhoto = findViewById(R.id.buttonUploadPhoto);
        checkBoxTerms = findViewById(R.id.checkBoxTerms);
        registerButton = findViewById(R.id.buttonRegister);

        // Deshabilitar el botón de registro inicialmente
        registerButton.setEnabled(false);
        // Cargar las opciones en el Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.house_styles,
                android.R.layout.simple_spinner_item
        );

// Estilo del Spinner desplegable
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHouseStyle.setAdapter(adapter);

// Manejar la selección del Spinner
        spinnerHouseStyle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedStyle = parent.getItemAtPosition(position).toString();
                Log.d("Spinner", "Estilo de Casa seleccionado: " + selectedStyle);
                // Puedes guardar el valor seleccionado si es necesario
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Manejar el caso cuando no se selecciona nada (opcional)
            }
        });
        // Manejar el clic del botón "Seleccionar Foto"
        buttonUploadPhoto.setOnClickListener(v -> showPhotoOptions());

        // Manejar el CheckBox de términos y condiciones
        // Configurar el OnCheckedChangeListener
        CheckBox checkBoxTerms = findViewById(R.id.checkBoxTerms);
        if (checkBoxTerms == null) {
            Log.e("CheckBox", "El CheckBox es nulo. Revisa el ID en el diseño XML.");
            return;
        }

        checkBoxTerms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d("CheckBox", "onCheckedChanged: CheckBox cambiado a " + isChecked);
            showTermsDialog(checkBoxTerms, isChecked, registerButton);
        });

        // Manejar el clic del botón de registro
        registerButton.setOnClickListener(v -> {
            String firstName = firstNameEditText.getText().toString().trim();
            String lastName = lastNameEditText.getText().toString().trim();
            String address = addressEditText.getText().toString().trim();
            String nickname = nicknameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String hobby = hobbyEditText.getText().toString().trim();
            String card = cardEditText.getText().toString().trim();
            String houseStyle = spinnerHouseStyle.getSelectedItem().toString();
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
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    private void checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(cameraIntent);
    }

    // Lanzadores para manejar resultados de actividad
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImage = result.getData().getData();
                    Glide.with(this).load(selectedImage).into(imageViewPhoto);
                }
            }
    );

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                    imageViewPhoto.setImageBitmap(photo);
                }
            }
    );

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
     * Mostrar el cuadro de diálogo de términos y condiciones
     */

    private void showTermsDialog(CheckBox checkBox, boolean isChecked, Button registerButton) {
        Log.d("Dialog", "showTermsDialog: Mostrando diálogo de términos y condiciones.");

        // Leer el contenido del archivo
        String terms = readTermsFromFile();

        // Inflar el diseño desde el XML
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_terms_conditions, null);

        // Referencias a los elementos del layout
        ScrollView scrollViewTerms = dialogView.findViewById(R.id.scrollViewTerms);
        TextView termsTextView = dialogView.findViewById(R.id.termsTextView);
        termsTextView.setText(terms);

        // Crear el AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Términos y Condiciones")
                .setView(dialogView)
                .setCancelable(false);

        // Botones
        builder.setPositiveButton("Aceptar", null);
        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            Log.d("Dialog", "showTermsDialog: El usuario canceló los términos.");
            checkBox.setOnCheckedChangeListener(null); // Desactiva temporalmente el listener
            checkBox.setChecked(false); // Desmarca el CheckBox
            registerButton.setEnabled(false); // Desactiva el botón Register
            checkBox.setOnCheckedChangeListener((buttonView, checked) -> {
                showTermsDialog(checkBox, checked, registerButton);
            });
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        // Referencia al botón "Aceptar" después de mostrar el diálogo
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setEnabled(false); // Desactivar inicialmente

        // Listener para habilitar el botón solo cuando se llegue al final del ScrollView
        scrollViewTerms.getViewTreeObserver().addOnScrollChangedListener(() -> {
            View child = scrollViewTerms.getChildAt(0);
            if (child != null) {
                int diff = (child.getBottom() - (scrollViewTerms.getHeight() + scrollViewTerms.getScrollY()));
                if (diff <= 0) {
                    // Usuario llegó al final del ScrollView
                    Log.d("Dialog", "showTermsDialog: Usuario llegó al final del texto.");
                    positiveButton.setEnabled(true);
                }
            }
        });

        // Acción del botón "Aceptar"
        positiveButton.setOnClickListener(v -> {
            Log.d("Dialog", "showTermsDialog: El usuario aceptó los términos.");
            checkBox.setOnCheckedChangeListener(null); // Desactiva temporalmente el listener
            checkBox.setChecked(true); // Marca el CheckBox
            registerButton.setEnabled(true); // Habilita el botón Register
            checkBox.setOnCheckedChangeListener((buttonView, checked) -> {
                showTermsDialog(checkBox, checked, registerButton);
            });
            dialog.dismiss();
        });
    }




    private String readTermsFromFile() {
        InputStream inputStream = getResources().openRawResource(R.raw.terms_conditions);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            int i;
            while ((i = inputStream.read()) != -1) {
                byteArrayOutputStream.write(i);
            }
            inputStream.close();
        } catch (IOException e) {
            Log.e("FileError", "Error al leer el archivo: " + e.getMessage());
            return "Error al cargar los términos y condiciones.";
        }

        return byteArrayOutputStream.toString();
    }


    // Métodos adicionales (openGallery, checkAndRequestCameraPermission, openCamera, etc.) permanecen sin cambios

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
}
