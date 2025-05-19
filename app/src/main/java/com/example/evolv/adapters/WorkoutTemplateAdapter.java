package com.example.evolv.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.evolv.R;
import com.example.evolv.models.WorkoutTemplate;
import java.util.List;

/**
 * Adapter para mostrar la lista de plantillas de entrenamiento.
 */
public class WorkoutTemplateAdapter extends RecyclerView.Adapter<WorkoutTemplateAdapter.ViewHolder> {
    private final List<WorkoutTemplate> templates;
    private final OnTemplateClickListener listener;

    public interface OnTemplateClickListener {
        void onTemplateClick(long templateId);
    }

    public WorkoutTemplateAdapter(List<WorkoutTemplate> templates, OnTemplateClickListener listener) {
        this.templates = templates;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout_template, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutTemplate template = templates.get(position);
        holder.textName.setText(template.getName());
        holder.textType.setText(template.getWorkoutType());
        holder.textNotes.setText(template.getNotes());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTemplateClick(template.getTemplateId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return templates.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textType, textNotes;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textTemplateName);
            textType = itemView.findViewById(R.id.textTemplateType);
            textNotes = itemView.findViewById(R.id.textTemplateNotes);
        }
    }
}
