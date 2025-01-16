package com.example.miprimeraplicacion;
import android.view.LayoutInflater;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import android.Manifest;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;


import android.widget.ArrayAdapter;
import android.widget.Spinner;

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
import android.app.DatePickerDialog;
import java.util.Calendar;

public class Register extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;
    private ImageView imageViewPhoto;
    private Button buttonUploadPhoto;
    private CheckBox checkBoxTerms;
    private Button registerButton;

    private Button cancelButton;

    private TextView coordinatesTextView;

    // Manejar el resultado de la actividad del mapa
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            double latitude = data.getDoubleExtra("latitude", 0);
            double longitude = data.getDoubleExtra("longitude", 0);
            coordinatesTextView.setText("Coordenadas: " + latitude + ", " + longitude);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registro);

        // Inicializar los elementos de la vista
        EditText firstNameEditText = findViewById(R.id.editTextFirstName);
        EditText lastNameEditText = findViewById(R.id.editTextLastName);
        EditText nicknameEditText = findViewById(R.id.editTextNickname);
        EditText passwordEditText = findViewById(R.id.editTextPassword);
        EditText confirmPasswordEditText = findViewById(R.id.editTextConfirm);
        EditText hobbyEditText = findViewById(R.id.editTextHobby);
        EditText cardnumberEditText = findViewById(R.id.editTextCardNumber);
        EditText cardexpiryEditText = findViewById(R.id.editTextCardExpiry);
        EditText cardcvvEditText = findViewById(R.id.editTextCardCVV);
        Spinner spinnerHouseStyle = findViewById(R.id.spinnerHouseStyle);
        Spinner spinnerTransportStyle = findViewById(R.id.spinnerTransportStyle);
        imageViewPhoto = findViewById(R.id.imageViewPhoto);
        buttonUploadPhoto = findViewById(R.id.buttonUploadPhoto);
        checkBoxTerms = findViewById(R.id.checkBoxTerms);
        registerButton = findViewById(R.id.buttonRegister);

        passwordEditText.setTransformationMethod(new DiamondTransformationMethod());
        confirmPasswordEditText.setTransformationMethod(new DiamondTransformationMethod());

        EditText expirationDateField = findViewById(R.id.editTextCardExpiry);
        cancelButton = findViewById(R.id.buttonCancel);
        EditText birthDateEditText = findViewById(R.id.editTextBirthDate);

        coordinatesTextView = findViewById(R.id.textViewCoordinates);

        // Botón para abrir el mapa
        Button openMapButton = findViewById(R.id.buttonOpenMap);

        openMapButton.setOnClickListener(v -> {
            Intent mapIntent = new Intent(Register.this, MapActivity.class);
            startActivityForResult(mapIntent, 1); // El código 1 es un identificador para esta solicitud
        });

        // Botón de Cancelar
        cancelButton.setOnClickListener(v -> {
            // Acción para el botón de Cancelar
            Intent intent = new Intent(Register.this, Login.class);
            startActivity(intent);
        });

        birthDateEditText.setOnClickListener(v -> {
            // Obtener la fecha actual
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Mostrar el DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    Register.this,
                    (view, year1, month1, dayOfMonth) -> {
                        // Actualizar el campo con la fecha seleccionada
                        String selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                        birthDateEditText.setText(selectedDate);
                    },
                    year,
                    month,
                    day
            );
            datePickerDialog.show();

        });
        expirationDateField.addTextChangedListener(new TextWatcher() {
            private String current = "";
            private boolean isDeleting;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                isDeleting = count > after; // Detecta si el usuario está borrando texto
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isDeleting) {
                    String input = s.toString().replaceAll("[^\\d]", ""); // Solo deja números

                    // Formatea como MM/AA
                    if (input.length() >= 2) {
                        String month = input.substring(0, 2);
                        String year = input.length() > 2 ? input.substring(2) : "";

                        // Construye el formato final
                        String formatted = month + (year.isEmpty() ? "" : "/" + year);

                        // Evita actualizaciones infinitas
                        if (!formatted.equals(current)) {
                            current = formatted;
                            expirationDateField.setText(formatted);
                            expirationDateField.setSelection(formatted.length()); // Coloca el cursor al final
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });





        // Deshabilitar el botón de registro inicialmente
        registerButton.setEnabled(false);
        // Cargar las opciones en el Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.house_styles,
                android.R.layout.simple_spinner_item
        );
        ArrayAdapter<CharSequence> adapt = ArrayAdapter.createFromResource(
                this,
                R.array.transport_styles,
                android.R.layout.simple_spinner_item
        );


// Estilo del Spinner desplegable
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHouseStyle.setAdapter(adapter);
        spinnerTransportStyle.setAdapter(adapt);

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
            String address = coordinatesTextView.getText().toString().trim();
            String nickname = nicknameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();
            String hobby = hobbyEditText.getText().toString().trim();
            String cardnumber = cardnumberEditText.getText().toString().trim();
            String cardexpiry = cardexpiryEditText.getText().toString().trim();
            String cardcvv = cardcvvEditText.getText().toString().trim();
            String houseStyle = spinnerHouseStyle.getSelectedItem().toString();
            String transportStyle = spinnerTransportStyle.getSelectedItem().toString();
            String birthDate = birthDateEditText.getText().toString().trim();

            // Verificar si el nickname contiene palabras prohibidas
            if (containsRestrictedWords(nickname)) {
                Toast.makeText(Register.this, "El nombre de usuario contiene palabras inapropiadas.", Toast.LENGTH_LONG).show();
                return;
            }
            if (firstName.isEmpty() || lastName.isEmpty() || address.isEmpty() || nickname.isEmpty() ||
                    password.isEmpty() || confirmPassword.isEmpty() || hobby.isEmpty() ||  cardnumber.isEmpty() ||cardexpiry.isEmpty() ||cardcvv.isEmpty() ||houseStyle.isEmpty() || transportStyle.isEmpty()|| birthDate.isEmpty()) {

                Toast.makeText(Register.this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!validatePassword(password)) {
                Toast.makeText(Register.this, "La contraseña no cumple con los requisitos mínimos.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(Register.this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isVisaOrMastercard(cardnumber)) {
                Toast.makeText(Register.this, "El número de tarjeta no es válido. Debe ser Visa o Mastercard.", Toast.LENGTH_SHORT).show();
                return;
            }


            // Enviar los datos de registro al servidor
            MainActivity.sendAndReceiveRegister(

       firstName, lastName, address, nickname, password, hobby, cardnumber, cardexpiry, cardcvv, houseStyle, transportStyle, birthDate,

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

    public boolean validatePassword(String password) {
        if (password.length() < 8) {
            return false;
        }

        // Patrones para letras, números y símbolos especiales
        String letters = "[a-zA-Z]";
        String numbers = "[0-9]";
        String specialChars = "[!@#$%^&*(),.?\":{}|<>]";

        // Comprueba si la contraseña contiene cada tipo
        boolean hasLetters = password.matches(".*" + letters + ".*");
        boolean hasNumbers = password.matches(".*" + numbers + ".*");
        boolean hasSpecialChars = password.matches(".*" + specialChars + ".*");

        // Al menos dos tipos de caracteres
        int typeCount = 0;
        if (hasLetters) typeCount++;
        if (hasNumbers) typeCount++;
        if (hasSpecialChars) typeCount++;

        return typeCount >= 2;
    }

    // Método para verificar si el username contiene palabras prohibidas
    private boolean containsRestrictedWords(String username) {
        String[] restrictedWords = {
                // Inglés
                "sex", "porn", "xxx", "adult", "escort", "prostitute", "nude", "playboy", "stripper", "fetish",
                "bdsm", "erotic", "nsfw", "hooker", "lust", "swinger", "camgirl", "sugarbaby", "dildo", "orgy",
                "deepthroat", "hentai", "milf", "creampie", "bareback", "threesome", "gangbang", "cumshot", "bukkake",
                "blowjob", "handjob", "tits", "boobs", "ass", "butt", "pussy", "dick", "cock", "vagina", "penis",
                "anal", "lesbian", "gayporn", "slut", "whore", "naughty", "playmate", "lingerie", "hardcore",
                "softcore", "voyeur", "incest", "taboo", "pegging", "dominatrix", "submission", "spanking", "shemale",
                "transsexual", "bisexual", "orgasm", "climax", "penetration", "cuckold", "sissy", "squirting", "strapon",
                "fellatio", "cunnilingus", "voyeur", "peeping", "exhibitionist", "masochist", "sadist", "dominant",
                "submissive", "gagging", "kink", "bondage", "genitals",

                // Español
                "sexo", "porno", "xxx", "adulto", "escort", "prostituta", "nudista", "desnudo", "playboy", "striper",
                "fetiche", "bdsm", "erótico", "travesti", "lujuria", "swinger", "pornografía", "dildo", "orgía",
                "trío", "orgasmo", "eyaculación", "corrida", "beso negro", "felación", "cunnilingus", "masturbación",
                "pene", "vagina", "ano", "clítoris", "senos", "tetas", "nalgas", "trasero", "culo", "pecho", "dominación",
                "sumisión", "esclava", "sadomasoquismo", "voyeurismo", "exhibicionismo", "dominatrix", "sadista",
                "masoquista", "bondage", "orgasmar", "sumiso", "pecho desnudo", "azotes", "pegging", "sexo grupal",
                "zoofilia", "incesto", "tabú", "sado", "latigazo", "porno gay", "travesti", "chica cam", "chico cam",
                "película porno", "sexo en vivo", "chat erótico", "deseo carnal", "picha", "sinbrel", "riata"
        };

        for (String word : restrictedWords) {
            if (username.toLowerCase().contains(word)) {
                return true; // Contiene una palabra prohibida
            }
        }
        return false; // Username válido
    }

}
