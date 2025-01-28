package com.example.miprimeraplicacion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AmenitiesAdapter extends RecyclerView.Adapter<AmenitiesAdapter.ViewHolder> {

    private List<String> amenities;
    private Context context;

    public AmenitiesAdapter(Context context, List<String> amenities) {
        this.context = context;
        this.amenities = amenities;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_amenity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.amenityName.setText(amenities.get(position));
    }

    @Override
    public int getItemCount() {
        return amenities.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView amenityName;

        public ViewHolder(View itemView) {
            super(itemView);
            amenityName = itemView.findViewById(R.id.amenityName);
        }
    }
}
