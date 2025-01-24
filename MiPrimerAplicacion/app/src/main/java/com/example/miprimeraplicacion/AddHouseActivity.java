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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray; // Asegúrate de importar esto
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.view.View;


// Convierte la lista de imágenes en Base64 a un JSONArray

public class AddHouseActivity extends AppCompatActivity {

    private ImageButton buttonAddPhoto;
    private Button buttonOpenMap, buttonRegister;
    private TextView textViewCoordinates;
    private ViewPager2 viewPagerImages;
    private List<Bitmap> selectedImages = new ArrayList<>(); // Lista para almacenar imágenes seleccionadas
    private ImagePagerAdapter imagePagerAdapter; // Adaptador para ViewPager2

    private String username;

    private Map<String, CheckBox> amenitiesMap = new HashMap<>();

    private Spinner spinnerProvincia, spinnerCanton;
    private Map<String, List<String>> provinciasCantonesMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_house);
        username = getIntent().getStringExtra("USERNAME");
        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
            finish(); // Cierra la actividad si no hay usuario
            return;
        }
        Log.d("AddHouseActivity", "Usuario recibido: " + username);

        // Inicializar CheckBoxes
        amenitiesMap.put("Cocina equipada", findViewById(R.id.check_cocina));
        amenitiesMap.put("Aire acondicionado", findViewById(R.id.check_aire));
        amenitiesMap.put("Calefacción", findViewById(R.id.check_calefaccion));
        amenitiesMap.put("Wi-Fi gratuito", findViewById(R.id.check_wifi));
        amenitiesMap.put("Televisión por cable", findViewById(R.id.check_tv));
        amenitiesMap.put("Lavadora y secadora", findViewById(R.id.check_lavadora));
        amenitiesMap.put("Piscina", findViewById(R.id.check_piscina));
        amenitiesMap.put("Jardín o patio", findViewById(R.id.check_jardin));
        amenitiesMap.put("Barbacoa o parrilla", findViewById(R.id.check_barbacoa));
        amenitiesMap.put("Terraza o balcón", findViewById(R.id.check_terraza));
        amenitiesMap.put("Gimnasio en casa", findViewById(R.id.check_gimnasio));
        amenitiesMap.put("Garaje o estacionamiento", findViewById(R.id.check_garaje));
        amenitiesMap.put("Sistema de seguridad", findViewById(R.id.check_seguridad));
        amenitiesMap.put("Habitaciones con baño en suite", findViewById(R.id.check_habitaciones_bano));
        amenitiesMap.put("Muebles de exterior", findViewById(R.id.check_muebles_exterior));
        amenitiesMap.put("Microondas", findViewById(R.id.check_microondas));
        amenitiesMap.put("Lavavajillas", findViewById(R.id.check_lavavajillas));
        amenitiesMap.put("Cafetera", findViewById(R.id.check_cafetera));
        amenitiesMap.put("Ropa de cama y toallas", findViewById(R.id.check_ropa_cama));
        amenitiesMap.put("Áreas comunes", findViewById(R.id.check_areas_comunes));
        amenitiesMap.put("Sofá cama", findViewById(R.id.check_sofa_cama));
        amenitiesMap.put("Servicios de limpieza", findViewById(R.id.check_limpieza));
        amenitiesMap.put("Transporte público cercano", findViewById(R.id.check_transporte));
        amenitiesMap.put("Mascotas permitidas", findViewById(R.id.check_mascotas));
        amenitiesMap.put("Tiendas y restaurantes", findViewById(R.id.check_tiendas));
        amenitiesMap.put("Calefacción por suelo", findViewById(R.id.check_calefaccion_suelo));
        amenitiesMap.put("Escritorio o área de trabajo", findViewById(R.id.check_escritorio));
        amenitiesMap.put("Entretenimiento", findViewById(R.id.check_entretenimiento));
        amenitiesMap.put("Chimenea", findViewById(R.id.check_chimenea));
        amenitiesMap.put("Internet de alta velocidad", findViewById(R.id.check_internet));
        // Inicializar elementos
        buttonAddPhoto = findViewById(R.id.buttonAddPhoto);
        buttonOpenMap = findViewById(R.id.buttonOpenMap);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewCoordinates = findViewById(R.id.textViewCoordinates);
        viewPagerImages = findViewById(R.id.viewPagerImages);

        // Configurar ViewPager2 para mostrar imágenes
        imagePagerAdapter = new ImagePagerAdapter(this, selectedImages);
        viewPagerImages.setAdapter(imagePagerAdapter);

        // Botón para seleccionar imágenes
        buttonAddPhoto.setOnClickListener(v -> showPhotoOptions());

        // Botón para seleccionar ubicación en el mapa
        buttonOpenMap.setOnClickListener(v -> {
            Intent mapIntent = new Intent(AddHouseActivity.this, MapActivity.class);
            startActivityForResult(mapIntent, 1);
        });

        // Botón para registrar la casa
        buttonRegister.setOnClickListener(v -> registerHouse());

        spinnerProvincia = findViewById(R.id.spinner_provincia);
        spinnerCanton = findViewById(R.id.spinner_canton);

        cargarDatosProvincias(); // Llama a la función que carga los datos
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
        Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        galleryIntent.setType("image/*");
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Permitir selección múltiple
        galleryLauncher.launch(galleryIntent);
    }

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    if (result.getData().getClipData() != null) { // Si el usuario seleccionó múltiples imágenes
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri imageUri = result.getData().getClipData().getItemAt(i).getUri();
                            if (selectedImages.size() < 10) { // Máximo 10 imágenes
                                addImageToList(imageUri);
                            } else {
                                Toast.makeText(this, "Solo puedes seleccionar hasta 10 imágenes", Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                    } else if (result.getData().getData() != null) { // Si el usuario seleccionó una sola imagen
                        if (selectedImages.size() < 10) {
                            Uri selectedImage = result.getData().getData();
                            addImageToList(selectedImage);
                        } else {
                            Toast.makeText(this, "Solo puedes seleccionar hasta 10 imágenes", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    private void addImageToList(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            selectedImages.add(bitmap);
            imagePagerAdapter.notifyDataSetChanged(); // Actualizar ViewPager2
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

        String provinciaSeleccionada = spinnerProvincia.getSelectedItem().toString();
        String cantonSeleccionado = spinnerCanton.getSelectedItem().toString();


        if (description.isEmpty() || rules.isEmpty() || price.isEmpty() || capacity.isEmpty() ||provinciaSeleccionada.isEmpty() ||cantonSeleccionado.isEmpty()|| location.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImages.isEmpty()) {
            Toast.makeText(this, "Por favor, selecciona al menos una imagen.", Toast.LENGTH_SHORT).show();
            return;
        }

        //  Obtener amenidades seleccionadas
        // Obtener amenidades seleccionadas
        List<String> selectedAmenities = getSelectedAmenities();

        // Verificar si la lista está vacía
        if (selectedAmenities.isEmpty()) {
            Toast.makeText(this, "Por favor, selecciona al menos una amenidad.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convertir la lista a JSONArray


        new Thread(() -> {
            List<String> base64Images = new ArrayList<>();


            for (Bitmap bitmap : selectedImages) {
                base64Images.add(encodeToBase64(bitmap)); // Ahora se ejecuta en segundo plano
            }

            JSONArray jsonImages = new JSONArray(base64Images);
            JSONArray jsonAmenities = new JSONArray(getSelectedAmenities());

            MainActivity.sendHouseData(username, description, rules, price, capacity, provinciaSeleccionada, cantonSeleccionado,location, jsonImages, jsonAmenities,
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
                    }
            );
        }).start();




    }

    private String loadJSONFromRaw() {
        String json = null;
        try {
            InputStream is = getResources().openRawResource(R.raw.provincias_cantones_distritos_costa_rica);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return json;
    }

    private void cargarDatosProvincias() {
        try {
            JSONObject jsonObject = new JSONObject(loadJSONFromRaw());
            JSONObject provinciasJson = jsonObject.getJSONObject("provincias");

            List<String> listaProvincias = new ArrayList<>();
            provinciasCantonesMap.clear();

            Iterator<String> keys = provinciasJson.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject provincia = provinciasJson.getJSONObject(key);
                String nombreProvincia = provincia.getString("nombre");
                listaProvincias.add(nombreProvincia);

                JSONObject cantonesJson = provincia.getJSONObject("cantones");
                List<String> listaCantones = new ArrayList<>();

                Iterator<String> cantonKeys = cantonesJson.keys();
                while (cantonKeys.hasNext()) {
                    String cantonKey = cantonKeys.next();
                    listaCantones.add(cantonesJson.getJSONObject(cantonKey).getString("nombre"));
                }

                provinciasCantonesMap.put(nombreProvincia, listaCantones);
            }

            // Llenar Spinner de Provincias
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaProvincias);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerProvincia.setAdapter(adapter);

            // Configurar listener para cargar cantones cuando cambie la provincia
            spinnerProvincia.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String provinciaSeleccionada = listaProvincias.get(position);
                    cargarCantones(provinciaSeleccionada);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void cargarCantones(String provinciaSeleccionada) {
        List<String> listaCantones = provinciasCantonesMap.get(provinciaSeleccionada);

        if (listaCantones != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaCantones);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCanton.setAdapter(adapter);
        }
    }




    private List<String> getSelectedAmenities() {
        List<String> selectedAmenities = new ArrayList<>();

        for (Map.Entry<String, CheckBox> entry : amenitiesMap.entrySet()) {
            if (entry.getValue().isChecked()) {
                selectedAmenities.add(entry.getKey());
            }
        }
        return selectedAmenities;
    }




    private String encodeToBase64(Bitmap image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // Redimensionar imagen (ejemplo: 800px de ancho máx)
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(image, 800, (800 * image.getHeight()) / image.getWidth(), true);

        // Comprimir imagen al 40% en JPEG para reducir tamaño
        scaledBitmap.compress(Bitmap.CompressFormat.PNG, 40, byteArrayOutputStream);

        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

}
