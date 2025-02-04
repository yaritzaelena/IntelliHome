package com.example.miprimeraplicacion;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OwnerHousesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private HouseAdapter houseAdapter;
    private List<House> houseList;
    private String username;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_houses);

        recyclerView = findViewById(R.id.recyclerViewOwnerHouses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        username = getIntent().getStringExtra("USERNAME");
        String housesData = getIntent().getStringExtra("houses_data");

        if (housesData != null) {
            loadHouses(housesData);
        } else {
            Toast.makeText(this, "No se recibieron datos de casas", Toast.LENGTH_LONG).show();
        }
    }

    private void loadHouses(String housesData) {
        try {
            JSONArray housesArray = new JSONArray(housesData);
            houseList = new ArrayList<>();

            for (int i = 0; i < housesArray.length(); i++) {
                JSONObject obj = housesArray.getJSONObject(i);

                List<String> photos = convertJsonArrayToList(obj.getJSONArray("imagenes"));
                List<String> amenities = convertJsonArrayToList(obj.getJSONArray("amenities"));

                House house = new House(
                        obj.getString("id"),
                        obj.getString("provincia"),
                        obj.getString("canton"),
                        obj.getString("price"),
                        obj.getString("capacity"),
                        obj.getString("username"),  // Dueño
                        photos,
                        amenities
                );

                houseList.add(house);
            }

            houseAdapter = new HouseAdapter(this, houseList);
            recyclerView.setAdapter(houseAdapter);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al procesar datos", Toast.LENGTH_SHORT).show();
        }
    }

    private List<String> convertJsonArrayToList(JSONArray jsonArray) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(jsonArray.optString(i, ""));
        }
        return list;
    }
}
