package com.example.flowerking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CropAdapter extends RecyclerView.Adapter<CropAdapter.CropViewHolder> {

    public interface OnCropClickListener {
        void onCropClick(CropModel crop);
    }

    private List<CropModel> cropList;
    private final OnCropClickListener listener;

    public CropAdapter(List<CropModel> cropList, OnCropClickListener listener) {
        this.cropList = (cropList != null) ? new ArrayList<>(cropList) : new ArrayList<>();
        this.listener = listener;
    }

    // Método que actualiza la lista y redibuja las tarjetas en pantalla
    public void updateList(List<CropModel> newList) {
        this.cropList = (newList != null) ? new ArrayList<>(newList) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CropViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_crop, parent, false);
        return new CropViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CropViewHolder holder, int position) {
        CropModel crop = cropList.get(position);
        holder.bind(crop, listener);
    }

    @Override
    public int getItemCount() {
        return cropList != null ? cropList.size() : 0;
    }

    static class CropViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStatus, tvBotanical, tvLocationFrame, tvTemp, tvMoisture;

        public CropViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvStatus = itemView.findViewById(R.id.tvItemStatus);
            tvBotanical = itemView.findViewById(R.id.tvItemBotanical);
            tvLocationFrame = itemView.findViewById(R.id.tvItemLocationFrame);
            tvTemp = itemView.findViewById(R.id.tvItemTemp);
            tvMoisture = itemView.findViewById(R.id.tvItemMoisture);
        }

        public void bind(CropModel crop, OnCropClickListener listener) {
            if (tvName != null) tvName.setText(crop.getName());
            if (tvStatus != null) tvStatus.setText(crop.getStatus());
            if (tvBotanical != null) tvBotanical.setText(crop.getBotanicalName());
            if (tvLocationFrame != null) {
                tvLocationFrame.setText(crop.getLocation() + " • Marco: " + crop.getPlantingFrame());
            }
            if (tvTemp != null) tvTemp.setText("Sustrato: " + crop.getSubstrateTemp());
            if (tvMoisture != null) tvMoisture.setText("Humedad: " + crop.getMoisture());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCropClick(crop);
                }
            });
        }
    }
}