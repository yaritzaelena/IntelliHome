package com.example.miprimeraplicacion;

import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ViewHouseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_house);

        ScrollView scrollView = findViewById(R.id.scrollViewHouses);
        LinearLayout container = findViewById(R.id.houseContainer);

        String housesData = getIntent().getStringExtra("houses_data");
        if (housesData != null) {
            try {
                JSONArray housesArray = new JSONArray(housesData);
                for (int i = 0; i < housesArray.length(); i++) {
                    JSONObject house = housesArray.getJSONObject(i);
                    String canton = house.getString("canton");
                    String provincia = house.getString("provincia");
                    String price = house.getString("price");
                    String owner = house.getString("username");
                    JSONArray imagesArray = house.getJSONArray("imagenes");

                    View houseView = LayoutInflater.from(this).inflate(R.layout.item_house, container, false);
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

                    // Asignar el TabLayout a ViewPager2
                    new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {}).attach();

                    container.addView(houseView);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}




