package com.example.flowerking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class CropHistoryAdapter
        extends RecyclerView.Adapter<CropHistoryAdapter.HistoryViewHolder> {

    private final List<CropHistoryItem> historyList;

    public CropHistoryAdapter(List<CropHistoryItem> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_crop_history, parent, false);

        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull HistoryViewHolder holder,
            int position) {

        CropHistoryItem item = historyList.get(position);

        String tipo = item.getEventType().toUpperCase(Locale.ROOT);

        holder.tvType.setText(nombreActividad(tipo));
        holder.tvDescription.setText(item.getDescription());
        holder.tvDate.setText("📅 " + item.getDate());
        holder.tvIcon.setText(iconoActividad(tipo));
    }

    private String nombreActividad(String tipo) {

        switch (tipo) {
            case "RIEGO":
                return "Riego realizado";

            case "ETAPA":
                return "Cambio de etapa";

            case "ALERTA":
                return "Alerta de sensores";

            default:
                return "Actividad del cultivo";
        }
    }

    private String iconoActividad(String tipo) {

        switch (tipo) {
            case "RIEGO":
                return "💧";

            case "ETAPA":
                return "🌱";

            case "ALERTA":
                return "⚠️";

            default:
                return "🌿";
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {

        TextView tvType;
        TextView tvDescription;
        TextView tvDate;
        TextView tvIcon;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            tvType = itemView.findViewById(R.id.tvHistoryType);
            tvDescription = itemView.findViewById(R.id.tvHistoryDescription);
            tvDate = itemView.findViewById(R.id.tvHistoryDate);
            tvIcon = itemView.findViewById(R.id.tvHistoryIcon);
        }
    }
}