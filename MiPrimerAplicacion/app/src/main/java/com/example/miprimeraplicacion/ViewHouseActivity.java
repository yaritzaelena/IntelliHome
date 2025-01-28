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

public class ViewHouseActivity extends AppCompatActivity {

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

            JSONArray housesArray = new JSONArray(housesData);
            for (int i = 0; i < housesArray.length(); i++) {
                JSONObject house = housesArray.getJSONObject(i);
                String canton = house.getString("canton");
                String provincia = house.getString("provincia");
                String price = house.getString("price");
                String owner = house.getString("username");
                String capacidad = house.getString("capacity");
                JSONArray imagesArray = house.getJSONArray("imagenes");
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    private void displayHouses(List<JSONObject> housesList) {
        houseContainer.removeAllViews();

        for (JSONObject house : housesList) {
            try {
                String canton = house.getString("canton");
                String provincia = house.getString("provincia");
                String price = house.getString("price");
                String owner = house.getString("username");
                String capacidad = house.getString("capacity");
                JSONArray imagesArray = house.getJSONArray("imagenes");

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