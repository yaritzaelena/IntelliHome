package com.example.miprimeraplicacion;

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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ViewHouseActivity extends AppCompatActivity {

    private PopupWindow filterPopup;
    private int minPrice = 0, maxPrice = 1000;
    private int selectedCapacity = 0;
    private List<String> selectedAmenities = new ArrayList<>();
    private LinearLayout houseContainer; // Se usará en lugar de RecyclerView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_house);

        ScrollView scrollView = findViewById(R.id.scrollViewHouses);
        houseContainer = findViewById(R.id.houseContainer); // ✅ Usamos houseContainer en lugar de RecyclerView
        EditText searchBar = findViewById(R.id.searchEditText);
        ImageButton filterButton = findViewById(R.id.filterButton);

        filterButton.setOnClickListener(v -> showFilterPopup());

        // Filtrar casas en tiempo real
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

        // Cargar casas desde los datos pasados
        String housesData = getIntent().getStringExtra("houses_data");
        if (housesData != null) {
            loadHouses(housesData);
        }
    }

    private void loadHouses(String housesData) {
        try {
            houseContainer.removeAllViews(); // Limpiar antes de agregar nuevas casas

            JSONArray housesArray = new JSONArray(housesData);
            for (int i = 0; i < housesArray.length(); i++) {
                JSONObject house = housesArray.getJSONObject(i);
                String canton = house.getString("canton");
                String provincia = house.getString("provincia");
                String price = house.getString("price");
                String owner = house.getString("username");
                JSONArray imagesArray = house.getJSONArray("imagenes");

                View houseView = LayoutInflater.from(this).inflate(R.layout.item_house, houseContainer, false);
                TextView textDetails = houseView.findViewById(R.id.textHouseDetails);
                ViewPager2 viewPager = houseView.findViewById(R.id.viewPagerImages);
                TabLayout tabLayout = houseView.findViewById(R.id.tabLayoutIndicator);

                textDetails.setText(provincia + ", " + canton + "\nPrecio: " + price + "\nDueño: " + owner);

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

    private void filterHouses(String query) {
        for (int i = 0; i < houseContainer.getChildCount(); i++) {
            View houseView = houseContainer.getChildAt(i);
            TextView textDetails = houseView.findViewById(R.id.textHouseDetails);
            String details = textDetails.getText().toString().toLowerCase();

            if (details.contains(query.toLowerCase())) {
                houseView.setVisibility(View.VISIBLE);
            } else {
                houseView.setVisibility(View.GONE);
            }
        }
    }

    private void showFilterPopup() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.filter_popup, null);

        filterPopup = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        filterPopup.showAtLocation(findViewById(R.id.scrollViewHouses), Gravity.CENTER, 0, 0);

        EditText editTextCapacity = popupView.findViewById(R.id.editTextCapacityFilter);
        SeekBar seekBarPrice = popupView.findViewById(R.id.seekBarPrice);
        TextView textMinPrice = popupView.findViewById(R.id.textMinPrice);
        TextView textMaxPrice = popupView.findViewById(R.id.textMaxPrice);
        LinearLayout amenitiesContainer = popupView.findViewById(R.id.amenitiesContainer);
        Button buttonApply = popupView.findViewById(R.id.buttonApplyFilter);
        Button buttonClear = popupView.findViewById(R.id.buttonClearFilter);

        seekBarPrice.setProgress(maxPrice);
        textMaxPrice.setText("$" + maxPrice);
        seekBarPrice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                maxPrice = progress;
                textMaxPrice.setText("$" + maxPrice);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        String[] amenities = {"Wi-Fi", "Piscina", "Gimnasio", "Terraza", "Cocina equipada"};
        for (String amenity : amenities) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(amenity);
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedAmenities.add(amenity);
                } else {
                    selectedAmenities.remove(amenity);
                }
            });
            amenitiesContainer.addView(checkBox);
        }

        buttonApply.setOnClickListener(v -> {
            selectedCapacity = editTextCapacity.getText().toString().isEmpty() ? 0 : Integer.parseInt(editTextCapacity.getText().toString());
            filterPopup.dismiss();
            filterHouses();
        });

        buttonClear.setOnClickListener(v -> {
            selectedCapacity = 0;
            maxPrice = 1000;
            selectedAmenities.clear();
            filterPopup.dismiss();
            filterHouses();
        });
    }

    private void filterHouses() {
        for (int i = 0; i < houseContainer.getChildCount(); i++) {
            View houseView = houseContainer.getChildAt(i);
            TextView textDetails = houseView.findViewById(R.id.textHouseDetails);
            String details = textDetails.getText().toString().toLowerCase();
            int housePrice = Integer.parseInt(details.replaceAll("[^0-9]", ""));

            boolean matchesCapacity = selectedCapacity == 0 || details.contains("capacidad: " + selectedCapacity);
            boolean matchesPrice = housePrice <= maxPrice;
            boolean matchesAmenities = selectedAmenities.isEmpty() || selectedAmenities.stream().allMatch(details::contains);

            if (matchesCapacity && matchesPrice && matchesAmenities) {
                houseView.setVisibility(View.VISIBLE);
            } else {
                houseView.setVisibility(View.GONE);
            }
        }
    }
}