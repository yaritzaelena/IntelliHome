package com.example.miprimeraplicacion;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.slider.RangeSlider;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.util.Log;
import java.util.HashMap;
import android.content.Intent;
import android.widget.Toast;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class ViewHouseActivity extends AppCompatActivity {
    private House house;
    private PopupWindow filterPopup;
    private int minPrice = 0, maxPrice = 1000;
    private int selectedCapacity = 0;
    private List<String> selectedAmenities = new ArrayList<>();
    private LinearLayout houseContainer;
    private Map<String, CheckBox> amenitiesMap = new LinkedHashMap<>();
    private List<JSONObject> allHouses = new ArrayList<>();
    private Map<String, String> cantonToProvinciaMap = new HashMap<>();
    private EditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_house);

        houseContainer = findViewById(R.id.houseContainer);
        searchBar = findViewById(R.id.searchEditText);
        ImageButton filterButton = findViewById(R.id.filterButton);

        filterButton.setOnClickListener(v -> showFilterPopup());

        // Obtener el nombre de usuario desde el intent
        String userloged = getIntent().getStringExtra("USERNAME");

        // Mostrar en el log para verificar que se pasó correctamente
        Log.d("ViewHouseActivity", "Usuario que inició sesión: " + userloged);

        // Cargar casas y datos de cantones/provincias
        String housesData = getIntent().getStringExtra("houses_data");
        if (housesData != null) {
            loadHouses(housesData);
        }

        loadProvinceCantonData();

        // Búsqueda en vivo mientras se escribe en la barra de búsqueda
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterHouses(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadHouses(String housesData) {
        try {
            houseContainer.removeAllViews();
            allHouses.clear();  // ✅ Asegurar que allHouses se vacíe antes de cargar

            JSONArray housesArray = new JSONArray(housesData);
            for (int i = 0; i < housesArray.length(); i++) {
                JSONObject house = housesArray.getJSONObject(i);
                allHouses.add(house);  // ✅ Guardar en allHouses para búsqueda

                String houseid = house.getString("id");
                String canton = house.getString("canton");
                String provincia = house.getString("provincia");
                String price = house.getString("price");
                String owner = house.getString("username");
                String capacidad = house.getString("capacity");
                JSONArray imagesArray = house.getJSONArray("imagenes");
                String description = house.has("description") ? house.getString("description") : "No disponible";
                String rules = house.has("rules") ? house.getString("rules") : "No disponible";
                JSONArray amenitiesArray = house.getJSONArray("amenities");

                View houseView = LayoutInflater.from(this).inflate(R.layout.item_house, houseContainer, false);
                TextView textDetails = houseView.findViewById(R.id.textHouseDetails);
                ViewPager2 viewPager = houseView.findViewById(R.id.viewPagerImages);
                TabLayout tabLayout = houseView.findViewById(R.id.tabLayoutIndicator);

                // ✅ Restauramos la conversión de amenidades a lista y las guardamos en el Tag
                List<String> amenitiesList = new ArrayList<>();
                for (int j = 0; j < amenitiesArray.length(); j++) {
                    amenitiesList.add(amenitiesArray.getString(j).trim().toLowerCase()); // Normalizar
                }

                // 🔹 Guardar la lista de amenidades en el Tag de la vista de la casa para que el filtro pueda acceder a ellas
                houseView.setTag(amenitiesList);

                // Asegurar que la capacidad está en los detalles
                textDetails.setText(provincia + ", " + canton + "\nCapacidad: " + capacidad + "\nPrecio: $" + price + "\nDueño: " + owner);

                List<String> imageList = new ArrayList<>();
                for (int j = 0; j < imagesArray.length(); j++) {
                    imageList.add(imagesArray.getString(j));
                }

                HouseImageAdapter adapter = new HouseImageAdapter(this, imageList);
                viewPager.setAdapter(adapter);
                new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {}).attach();
                houseContainer.addView(houseView);

                // Configurar el click para abrir la ventana emergente con información detallada
                textDetails.setOnClickListener(v -> showHousePopup(houseid, provincia, canton, price, owner, capacidad, description, rules, amenitiesArray));
            }

            displayHouses(allHouses);  // ✅ Mostrar casas correctamente
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void showHousePopup(String houseid, String provincia, String canton, String price, String owner, String capacidad, String description, String rules, JSONArray amenitiesArray) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_house_details, null);

        // Configurar la ventana emergente
        PopupWindow housePopup = new PopupWindow(popupView,
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                ViewGroup.LayoutParams.WRAP_CONTENT, true);

        housePopup.setFocusable(true);
        housePopup.setOutsideTouchable(true);
        housePopup.setBackgroundDrawable(getDrawable(R.drawable.popup_background));

        // Mostrar en el centro de la pantalla
        housePopup.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);

        // Referencias a los elementos de la ventana emergente
        TextView textTitle = popupView.findViewById(R.id.textHouseTitle);
        TextView textDescription = popupView.findViewById(R.id.textHouseDescription);
        TextView textRules = popupView.findViewById(R.id.textHouseRules);
        TextView textPrice = popupView.findViewById(R.id.textHousePrice);
        TextView textTaxDetails = popupView.findViewById(R.id.textHouseTaxDetails);
        TextView textCapacity = popupView.findViewById(R.id.textHouseCapacity);
        TextView textOwner = popupView.findViewById(R.id.textHouseOwner);
        TextView textLocation = popupView.findViewById(R.id.textHouseLocation);
        LinearLayout amenitiesContainer = popupView.findViewById(R.id.amenitiesContainer);
        Button buttonClose = popupView.findViewById(R.id.buttonClosePopup);
        Button buttonRent = popupView.findViewById(R.id.buttonRentHouse);

        String userloged = getIntent().getStringExtra("USERNAME");

        // Convertir precio a número y agregar el 23%
        double originalPrice = Double.parseDouble(price);
        double taxIVA = originalPrice * 0.13;  // 13% de IVA
        double taxCleaning = originalPrice * 0.10;  // 10% de limpieza
        double finalPrice = originalPrice + taxIVA + taxCleaning;  // Precio total con impuestos

        String formattedPrice = String.format("%.2f", finalPrice); // Redondear a 2 decimales
        String formattedTaxIVA = String.format("%.2f", taxIVA);
        String formattedTaxCleaning = String.format("%.2f", taxCleaning);


        // Asignar valores
        textTitle.setText("Detalles de la Casa");
        textDescription.setText("Descripción: " + description);
        textRules.setText("Reglas: " + rules);
        textPrice.setText("Precio: $" + formattedPrice + " por noche (con impuestos)");
        textTaxDetails.setText("Incluye:\n • 13% impuesto IVA: $" + formattedTaxIVA + "\n • 10% impuesto limpieza: $" + formattedTaxCleaning);
        textCapacity.setText("Capacidad: " + capacidad + " personas");
        textOwner.setText("Dueño: " + owner);
        textLocation.setText("Ubicación: " + provincia + ", " + canton);

        // Agregar amenidades dinámicamente
        amenitiesContainer.removeAllViews();
        for (int i = 0; i < amenitiesArray.length(); i++) {
            try {
                String amenity = amenitiesArray.getString(i);
                TextView amenityTextView = new TextView(this);
                amenityTextView.setText("• " + amenity);
                amenitiesContainer.addView(amenityTextView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Evento para alquilar la casa
        buttonRent.setOnClickListener(v -> {
            Log.d("Alquiler", "Usuario: " + userloged + " seleccionó la casa con ID: " + houseid);
            ReserveHouseActivity dialogFragment = ReserveHouseActivity.newInstance(houseid, userloged);
            dialogFragment.show(getSupportFragmentManager(), "ReserveHouseDialog");

           // showRentConfirmation(houseid, provincia, canton, price, owner, userloged);

            /*
            // Verificar que la casa tiene un ID válido
            if (houseid != null && !houseid.isEmpty()) {
                Intent intent = new Intent(ViewHouseActivity.this, ReserveHouseActivity.class);
                intent.putExtra("id", houseid);  // ✅ Enviar ID de la casa seleccionada
                intent.putExtra("USERNAME", userloged); // También enviar el usuario que inició sesión
                startActivity(intent);
            } else {
                Toast.makeText(ViewHouseActivity.this, "Error: No se encontró el ID de la casa", Toast.LENGTH_SHORT).show();
            }
            */

        });

        // Cerrar popup al presionar el botón
        buttonClose.setOnClickListener(v -> housePopup.dismiss());
    }

    private void showRentConfirmation(String houseid, String provincia, String canton, String price, String owner, String userloged) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirmar Alquiler")
                .setMessage("¿Deseas alquilar esta casa en " + provincia + ", " + canton + " por $" + price + "?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    Log.d("Alquiler", "Casa alquilada correctamente");

                    // Aquí puedes agregar la lógica para procesar el alquiler, como actualizar la base de datos
                    dialog.dismiss();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }



    private void displayHouses(List<JSONObject> housesList) {
        houseContainer.removeAllViews();

        for (JSONObject house : housesList) {
            try {
                String houseid = house.getString("id");
                String canton = house.getString("canton");
                String provincia = house.getString("provincia");
                String price = house.getString("price");
                String owner = house.getString("username");
                String capacidad = house.getString("capacity");
                JSONArray imagesArray = house.getJSONArray("imagenes");
                String description = house.has("description") ? house.getString("description") : "No disponible";
                String rules = house.has("rules") ? house.getString("rules") : "No disponible";
                JSONArray amenitiesArray = house.getJSONArray("amenities");

                View houseView = LayoutInflater.from(this).inflate(R.layout.item_house, houseContainer, false);
                TextView textDetails = houseView.findViewById(R.id.textHouseDetails);
                ViewPager2 viewPager = houseView.findViewById(R.id.viewPagerImages);
                TabLayout tabLayout = houseView.findViewById(R.id.tabLayoutIndicator);

                textDetails.setText(provincia + ", " + canton + "\nCapacidad: " + capacidad + "\nPrecio: $" + price + "\nDueño: " + owner);

                List<String> imageList = new ArrayList<>();
                for (int j = 0; j < imagesArray.length(); j++) {
                    imageList.add(imagesArray.getString(j));
                }

                HouseImageAdapter adapter = new HouseImageAdapter(this, imageList);
                viewPager.setAdapter(adapter);
                new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {}).attach();

                // ✅ Restauramos la conversión de amenidades a lista y las guardamos en el Tag
                List<String> amenitiesList = new ArrayList<>();
                for (int j = 0; j < amenitiesArray.length(); j++) {
                    amenitiesList.add(amenitiesArray.getString(j).trim().toLowerCase()); // Normalizar
                }
                houseView.setTag(amenitiesList);

                // 🔥 **REESTABLECER EL LISTENER PARA MOSTRAR DETALLES DE LA CASA**
                textDetails.setClickable(true);
                textDetails.setFocusable(true);
                textDetails.setOnClickListener(v -> {
                    Log.d("Popup", "Clic detectado en casa con ID: " + houseid);
                    showHousePopup(houseid, provincia, canton, price, owner, capacidad, description, rules, amenitiesArray);
                });

                houseContainer.addView(houseView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void loadProvinceCantonData() {
        try {
            String json = loadJSONFromRaw(this, R.raw.provincias_cantones_distritos_costa_rica);
            if (json == null) {
                Log.e("CargaProvincias", "Error: JSON no cargado.");
                return;
            }

            JSONObject rootObject = new JSONObject(json); // ✅ Se usa `json`, no `jsonString`

            if (!rootObject.has("provincias")) {
                Log.e("CargaProvincias", "Error: No se encontró la clave 'provincias' en el JSON");
                return;
            }

            JSONObject provinciasObject = rootObject.getJSONObject("provincias");

            for (Iterator<String> provinceIt = provinciasObject.keys(); provinceIt.hasNext(); ) {
                String provinceKey = provinceIt.next();
                JSONObject province = provinciasObject.getJSONObject(provinceKey);
                String provinceName = province.getString("nombre").trim().toLowerCase();

                if (!province.has("cantones")) {
                    Log.e("CargaProvincias", "Error: La provincia " + provinceName + " no tiene cantones");
                    continue;
                }

                JSONObject cantons = province.getJSONObject("cantones");

                for (Iterator<String> cantonIt = cantons.keys(); cantonIt.hasNext(); ) {
                    String cantonKey = cantonIt.next();
                    JSONObject canton = cantons.getJSONObject(cantonKey);
                    String cantonName = canton.getString("nombre").trim().toLowerCase();

                    // ✅ Guardamos la relación cantón -> provincia
                    cantonToProvinciaMap.put(cantonName, provinceName);

                    Log.d("CargaProvincias", "Cargado Cantón: " + cantonName + " -> Provincia: " + provinceName);
                }
            }

            Log.d("CargaProvincias", "Mapa Final: " + cantonToProvinciaMap);

        } catch (Exception e) {
            Log.e("CargaProvincias", "Error al cargar el JSON", e);
        }
    }


    private void filterHouses(String query) {
        if (query.isEmpty()) {
            displayHouses(allHouses);
            return;
        }

        query = query.trim().toLowerCase();

        if (cantonToProvinciaMap == null || cantonToProvinciaMap.isEmpty()) {
            Log.e("FiltroBusqueda", "Error: Mapa cantonToProvinciaMap no está cargado.");
            return;
        }

        String provinceMatch = cantonToProvinciaMap.get(query);
        Log.d("FiltroBusqueda", "Query: " + query + " -> Provincia encontrada: " + provinceMatch);

        List<JSONObject> matchingCanton = new ArrayList<>();
        List<JSONObject> matchingProvince = new ArrayList<>();

        for (JSONObject house : allHouses) {
            try {
                String houseCanton = house.getString("canton").trim().toLowerCase();
                String houseProvince = house.getString("provincia").trim().toLowerCase();

                Log.d("FiltroCasas", "Casa: " + houseCanton + ", " + houseProvince);

                if (houseCanton.equals(query)) {
                    matchingCanton.add(house);
                    Log.d("FiltroCasas", "✔ Coincide con cantón: " + houseCanton);
                } else if (provinceMatch != null && houseProvince.equals(provinceMatch)) {
                    matchingProvince.add(house);
                    Log.d("FiltroCasas", "✔ Coincide con provincia: " + houseProvince);
                }
            } catch (Exception e) {
                Log.e("FiltroCasas", "Error procesando casa", e);
            }
        }

        // 🔥❗️EXCLUSIÓN: Si no coincide ni por cantón ni por provincia, NO SE AGREGA
        List<JSONObject> sortedList = new ArrayList<>();
        sortedList.addAll(matchingCanton);
        sortedList.addAll(matchingProvince);

        Log.d("FiltroFinal", "Casas ordenadas: Cantón=" + matchingCanton.size() +
                ", Provincia=" + matchingProvince.size());

        displayHouses(sortedList);
    }

    private void showFilterPopup() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.filter_popup, null);

        // Configurar correctamente el PopupWindow
        filterPopup = new PopupWindow(popupView,
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                ViewGroup.LayoutParams.WRAP_CONTENT, true);

        filterPopup.setFocusable(true);
        filterPopup.setOutsideTouchable(true);
        filterPopup.setBackgroundDrawable(getDrawable(R.drawable.popup_background)); // Fondo personalizado

        // Mostrar el popup en el centro de la pantalla
        filterPopup.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);

        EditText editTextCapacity = popupView.findViewById(R.id.editTextCapacityFilter);
        RangeSlider sliderPriceRange = popupView.findViewById(R.id.sliderPriceRange);
        TextView textSelectedPriceRange = popupView.findViewById(R.id.textSelectedPriceRange);
        GridLayout amenitiesContainer = popupView.findViewById(R.id.amenitiesSection);
        Button buttonApply = popupView.findViewById(R.id.buttonApplyFilter);
        Button buttonClear = popupView.findViewById(R.id.buttonClearFilter);

        // Configurar el slider de precio
        sliderPriceRange.setValues((float) minPrice, (float) maxPrice);
        sliderPriceRange.addOnChangeListener((slider, value, fromUser) -> {
            minPrice = Math.round(slider.getValues().get(0));
            maxPrice = Math.round(slider.getValues().get(1));
            textSelectedPriceRange.setText("Rango seleccionado: $" + minPrice + " - $" + maxPrice);
        });

        // ---------------------------
        // CAPTURAR TODAS LAS AMENIDADES
        // ---------------------------
        selectedAmenities.clear();
        amenitiesMap.clear();

        for (int i = 0; i < amenitiesContainer.getChildCount(); i++) {
            View amenityView = amenitiesContainer.getChildAt(i);

            // Asegurar que es un LinearLayout que contiene un CheckBox
            if (amenityView instanceof LinearLayout) {
                LinearLayout layout = (LinearLayout) amenityView;
                for (int j = 0; j < layout.getChildCount(); j++) {
                    View childView = layout.getChildAt(j);
                    if (childView instanceof CheckBox) {
                        CheckBox checkBox = (CheckBox) childView;
                        String amenityName = checkBox.getText().toString().trim().toLowerCase();
                        amenitiesMap.put(amenityName, checkBox);

                        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (isChecked) {
                                if (!selectedAmenities.contains(amenityName)) {
                                    selectedAmenities.add(amenityName);
                                }
                            } else {
                                selectedAmenities.remove(amenityName);
                            }

                            // Log para verificar la lista de amenidades seleccionadas
                            Log.d("Filtro", "Amenidades seleccionadas actualizadas: " + selectedAmenities);
                        });
                    }
                }
            }
        }

        // ---------------------------
        // APLICAR FILTRO
        // ---------------------------
        buttonApply.setOnClickListener(v -> {
            String capacityText = editTextCapacity.getText().toString().trim();
            if (!capacityText.isEmpty()) {
                selectedCapacity = Integer.parseInt(capacityText);
            } else {
                selectedCapacity = 0; // Si está vacío, no aplicar filtro de capacidad
            }

            filterHouses(); // Aplicar filtros correctamente
            filterPopup.dismiss();
        });

        // ---------------------------
        // LIMPIAR FILTROS
        // ---------------------------
        buttonClear.setOnClickListener(v -> {
            selectedCapacity = 0;
            minPrice = 0;
            maxPrice = 1000;
            selectedAmenities.clear();
            editTextCapacity.setText(""); // Limpiar el campo de capacidad en el popup
            filterHouses();
            filterPopup.dismiss();
        });
    }

    private void filterHouses() {
        Log.d("Filtro", "Aplicando filtros con capacidad: " + selectedCapacity +
                ", Precio: $" + minPrice + " - $" + maxPrice +
                ", Amenidades: " + selectedAmenities);

        for (int i = 0; i < houseContainer.getChildCount(); i++) {
            View houseView = houseContainer.getChildAt(i);
            TextView textDetails = houseView.findViewById(R.id.textHouseDetails);
            String details = textDetails.getText().toString().toLowerCase();

            Log.d("Filtro", "Detalles de la casa: " + details);

            // ---------------------------
            // EXTRAER CAPACIDAD MÍNIMA
            // ---------------------------
            int houseCapacity = 0;
            Pattern pattern = Pattern.compile("capacidad:\\s*(\\d+)");
            Matcher matcher = pattern.matcher(details);
            if (matcher.find()) {
                houseCapacity = Integer.parseInt(matcher.group(1));
            }

            Log.d("Filtro", "Capacidad encontrada: " + houseCapacity + " | Capacidad seleccionada: " + selectedCapacity);
            boolean matchesCapacity = (selectedCapacity == 0 || houseCapacity >= selectedCapacity);

            // ---------------------------
            // EXTRAER PRECIO DE LA CASA
            // ---------------------------
            int housePrice = 0;
            Pattern pricePattern = Pattern.compile("precio:\\s*\\$(\\d+)");
            Matcher priceMatcher = pricePattern.matcher(details);
            if (priceMatcher.find()) {
                housePrice = Integer.parseInt(priceMatcher.group(1));
            }

            Log.d("Filtro", "Precio encontrado: $" + housePrice + " | Rango: $" + minPrice + " - $" + maxPrice);
            boolean matchesPrice = (housePrice >= minPrice && housePrice <= maxPrice);

            // ---------------------------
            // COMPARACIÓN DE AMENIDADES
            // ---------------------------
            List<String> houseAmenities = (List<String>) houseView.getTag();
            if (houseAmenities == null) {
                houseAmenities = new ArrayList<>();
            }

            // Normalizar amenidades almacenadas
            List<String> normalizedHouseAmenities = new ArrayList<>();
            for (String amenity : houseAmenities) {
                normalizedHouseAmenities.add(amenity.trim().toLowerCase());
            }

            Log.d("Filtro", "Amenidades encontradas: " + normalizedHouseAmenities);
            Log.d("Filtro", "Amenidades solicitadas: " + selectedAmenities);

            boolean matchesAmenities = selectedAmenities.isEmpty() ||
                    normalizedHouseAmenities.containsAll(selectedAmenities);

            Log.d("Filtro", "matchesCapacity: " + matchesCapacity +
                    ", matchesPrice: " + matchesPrice +
                    ", matchesAmenities: " + matchesAmenities);

            // ---------------------------
            // VERIFICAR TODOS LOS FILTROS
            // ---------------------------
            if (matchesCapacity && matchesPrice && matchesAmenities) {
                houseView.setVisibility(View.VISIBLE);
            } else {
                houseView.setVisibility(View.GONE);
            }
        }
    }
    private static String loadJSONFromRaw(Context context, int rawResourceId) {
        InputStream is = null;
        try {
            is = context.getResources().openRawResource(rawResourceId);
            Scanner scanner = new Scanner(is).useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        } catch (Exception e) {
            Log.e("JSONLoader", "Error al cargar el archivo JSON: " + rawResourceId, e);
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e("JSONLoader", "Error al cerrar el InputStream", e);
                }
            }
        }
    }




}