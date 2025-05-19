package com.example.evolv.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.evolv.R;
import com.example.evolv.models.WorkoutTemplateExercise;
import java.util.List;

/**
 * Adapter para mostrar los ejercicios asociados a una plantilla.
 */
public class WorkoutTemplateExerciseAdapter extends RecyclerView.Adapter<WorkoutTemplateExerciseAdapter.ViewHolder> {
    private final List<WorkoutTemplateExercise> exercises;

    public WorkoutTemplateExerciseAdapter(List<WorkoutTemplateExercise> exercises) {
        this.exercises = exercises;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout_template_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutTemplateExercise exercise = exercises.get(position);
        holder.textName.setText(exercise.getExercise().getName());
        holder.textParams.setText(
            holder.itemView.getContext().getString(
                R.string.exercise_params_format,
                exercise.getSets(),
                exercise.getRepetitions(),
                exercise.getTargetDuration()
            )
        );
        // Aquí podrías cargar la imagen si tienes una URL válida
        holder.imageExercise.setImageResource(R.drawable.ic_exercise_placeholder);
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageExercise;
        TextView textName, textParams;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageExercise = itemView.findViewById(R.id.imageExercise);
            textName = itemView.findViewById(R.id.textExerciseName);
            textParams = itemView.findViewById(R.id.textExerciseParams);
        }
    }
}
