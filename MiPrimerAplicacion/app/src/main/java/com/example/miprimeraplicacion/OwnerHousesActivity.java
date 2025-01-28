package com.example.miprimeraplicacion;

import android.os.Bundle;
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
    private RecyclerView recyclerViewHouses;
    private HouseAdapter houseAdapter;
    private List<House> houseList;
    private String username;  // Nombre de usuario del propietario

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_houses);

        username = getIntent().getStringExtra("USERNAME");

        recyclerViewHouses = findViewById(R.id.recyclerViewHouses);
        recyclerViewHouses.setLayoutManager(new LinearLayoutManager(this));

        houseList = new ArrayList<>();
        houseAdapter = new HouseAdapter(this, houseList);
        recyclerViewHouses.setAdapter(houseAdapter);

        obtenerCasasDelServidor();
    }

    private void obtenerCasasDelServidor() {
        MainActivity.getOwnerHouses(username, new MainActivity.ServerCallback() {

            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    try {
                        JSONArray housesArray = new JSONArray(response);
                        houseList.clear();

                        for (int i = 0; i < housesArray.length(); i++) {
                            JSONObject obj = housesArray.getJSONObject(i);
                            List<String> photos = new ArrayList<>();
                            JSONArray photosArray = obj.getJSONArray("photos");
                            for (int j = 0; j < photosArray.length(); j++) {
                                photos.add(photosArray.getString(j));
                            }

                            List<String> amenities = new ArrayList<>();
                            JSONArray amenitiesArray = obj.getJSONArray("amenities");
                            for (int j = 0; j < amenitiesArray.length(); j++) {
                                amenities.add(amenitiesArray.getString(j));
                            }

                            House house = new House(
                                    obj.getString("description"),
                                    obj.getString("rules"),
                                    obj.getString("price"),
                                    obj.getString("capacity"),
                                    obj.getString("provincia"),
                                    obj.getString("canton"),
                                    obj.getString("location"),
                                    photos,
                                    amenities
                            );
                            houseList.add(house);
                        }

                        houseAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Toast.makeText(OwnerHousesActivity.this, "Error al procesar datos", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(OwnerHousesActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }


}
