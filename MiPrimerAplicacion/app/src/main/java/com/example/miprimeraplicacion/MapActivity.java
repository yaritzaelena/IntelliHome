package com.example.miprimeraplicacion;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng selectedLocation;
    private Button confirmLocationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Obtener el fragmento del mapa y notificar cuando esté listo
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        confirmLocationButton = findViewById(R.id.confirmLocationButton);
        confirmLocationButton.setOnClickListener(v -> {
            if (selectedLocation != null) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("latitude", selectedLocation.latitude);
                returnIntent.putExtra("longitude", selectedLocation.longitude);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng defaultLocation = new LatLng(9.7489, -83.7534); // Costa Rica como punto de inicio
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));

        mMap.setOnMapClickListener(latLng -> {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Ubicación seleccionada"));
            selectedLocation = latLng;
        });
    }
}

