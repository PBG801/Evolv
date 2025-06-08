package com.example.evolv.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.evolv.R;
import com.example.evolv.models.Exercise;
import java.util.ArrayList;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ViewHolder> {
    private List<Exercise> exercises;
    private List<Exercise> selectedExercises = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public ExerciseAdapter(List<Exercise> exercises) {
        this.exercises = exercises != null ? exercises : new ArrayList<>();
    }

    // Permite establecer la lista de seleccionados desde fuera (útil para edición)
    public void setSelectedExercises(List<Exercise> seleccionados) {
        this.selectedExercises.clear();
        if (seleccionados != null) {
            this.selectedExercises.addAll(seleccionados);
        }
        notifyDataSetChanged();
    }

    public List<Exercise> getSelectedExercises() {
        return selectedExercises;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateExercises(List<Exercise> newExercises) {
        this.exercises = newExercises != null ? newExercises : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise_block, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);
        holder.textName.setText(exercise.getName());
        // Pintar seleccionado
        if (selectedExercises.contains(exercise)) {
            holder.itemView.setBackgroundColor(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.purple_secondary_light));
        } else {
            holder.itemView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        }
        // Toggle selección visual y lógica
        holder.itemView.setOnClickListener(v -> {
            if (selectedExercises.contains(exercise)) {
                selectedExercises.remove(exercise);
                holder.itemView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            } else {
                selectedExercises.add(exercise);
                holder.itemView.setBackgroundColor(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.purple_secondary_light));
            }
            if (listener != null) listener.onItemClick(v, position);
        });
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textExerciseName);
        }
    }
}
