package com.example.miprimeraplicacion;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

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
                    String provincia = house.optString("provincia", "Desconocido");
                    String price = house.optString("price", "No especificado");
                    String owner = house.optString("username", "No disponible");
                    JSONArray imagesArray = house.getJSONArray("imagenes");

                    View houseView = getLayoutInflater().inflate(R.layout.item_house, null);
                    TextView textDetails = houseView.findViewById(R.id.textHouseDetails);
                    LinearLayout imageContainer = houseView.findViewById(R.id.imageContainer);

                    textDetails.setText(provincia + ", " + canton + "\nPrecio: " + price + "\nDueño: " + owner);

                    for (int j = 0; j < imagesArray.length(); j++) {
                        String encodedImage = imagesArray.getString(j);
                        if (!encodedImage.isEmpty()) {
                            byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                            if (bitmap != null) {
                                ImageView imageView = new ImageView(this);
                                imageView.setImageBitmap(bitmap);
                                imageView.setAdjustViewBounds(true);
                                imageContainer.addView(imageView);
                            } else {
                                System.out.println("⚠ Error al decodificar imagen " + j);
                            }
                        }
                    }
                    container.addView(houseView);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}



