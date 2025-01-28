package com.example.miprimeraplicacion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class HouseAdapter extends RecyclerView.Adapter<HouseAdapter.HouseViewHolder> {
    private Context context;
    private List<House> houseList;
    private List<House> houseListFull; // Lista completa para el filtrado

    public HouseAdapter(Context context, List<House> houseList) {
        this.context = context;
        this.houseList = houseList;
        this.houseListFull = new ArrayList<>(houseList);
    }

    @NonNull
    @Override
    public HouseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_house, parent, false);
        return new HouseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HouseViewHolder holder, int position) {
        House house = houseList.get(position);

        holder.textDetails.setText(
                house.getProvincia() + ", " + house.getCanton() +
                        "\nPrecio: " + house.getPrice() +
                        "\nCapacidad: " + house.getCapacity() +
                        "\nDueño: " + house.getOwner()
        );

        HouseImageAdapter adapter = new HouseImageAdapter(context, house.getImageUrls());
        holder.viewPager.setAdapter(adapter);
        new TabLayoutMediator(holder.tabLayout, holder.viewPager, (tab, position1) -> {}).attach();
    }

    @Override
    public int getItemCount() {
        return houseList.size();
    }

    public void updateList(List<House> newList) {
        houseList.clear();
        houseList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class HouseViewHolder extends RecyclerView.ViewHolder {
        TextView textDetails;
        ViewPager2 viewPager;
        TabLayout tabLayout;

        public HouseViewHolder(@NonNull View itemView) {
            super(itemView);
            textDetails = itemView.findViewById(R.id.textHouseDetails);
            viewPager = itemView.findViewById(R.id.viewPagerImages);
            tabLayout = itemView.findViewById(R.id.tabLayoutIndicator);
        }
    }
}

