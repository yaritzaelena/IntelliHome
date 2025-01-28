package com.example.miprimeraplicacion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HouseAdapter extends RecyclerView.Adapter<HouseAdapter.ViewHolder> {

    private List<House> houseList;
    private Context context;

    public HouseAdapter(Context context, List<House> houseList) {
        this.context = context;
        this.houseList = houseList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_house, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        House house = houseList.get(position);

        holder.houseDescription.setText(house.getDescription());
        holder.houseRules.setText(house.getRules());
        holder.housePrice.setText("$" + house.getPrice() + " por noche");
        holder.houseCapacity.setText("Capacidad: " + house.getCapacity() + " personas");
        holder.houseProvinciaCanton.setText(house.getProvincia() + ", " + house.getCanton());
        holder.houseLocation.setText("Ubicación: " + house.getLocation());

        // Configurar RecyclerView de fotos
        PhotoAdapter photoAdapter = new PhotoAdapter(context, house.getPhotos());
        holder.recyclerViewPhotos.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerViewPhotos.setAdapter(photoAdapter);

        // Configurar RecyclerView de amenidades
        AmenitiesAdapter amenitiesAdapter = new AmenitiesAdapter(context, house.getAmenities());
        holder.recyclerViewAmenities.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerViewAmenities.setAdapter(amenitiesAdapter);
    }

    @Override
    public int getItemCount() {
        return houseList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView houseDescription, houseRules, housePrice, houseCapacity, houseProvinciaCanton, houseLocation;
        RecyclerView recyclerViewPhotos, recyclerViewAmenities;

        public ViewHolder(View itemView) {
            super(itemView);
            houseDescription = itemView.findViewById(R.id.houseDescription);
            houseRules = itemView.findViewById(R.id.houseRules);
            housePrice = itemView.findViewById(R.id.housePrice);
            houseCapacity = itemView.findViewById(R.id.houseCapacity);
            houseProvinciaCanton = itemView.findViewById(R.id.houseProvinciaCanton);
            houseLocation = itemView.findViewById(R.id.houseLocation);
            recyclerViewPhotos = itemView.findViewById(R.id.recyclerViewPhotos);
            recyclerViewAmenities = itemView.findViewById(R.id.recyclerViewAmenities);
        }
    }
}
