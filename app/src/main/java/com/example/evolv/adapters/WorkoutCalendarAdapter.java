package com.example.evolv.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evolv.DatabaseHelper;
import com.example.evolv.R;
import com.example.evolv.models.Workout;

import java.util.List;

/**
 * Adaptador para mostrar la lista de workouts en el calendario.
 * Permite visualizar el nombre, estado y notas de cada entrenamiento.
 */
public class WorkoutCalendarAdapter extends RecyclerView.Adapter<WorkoutCalendarAdapter.ViewHolder> {
    private final List<Workout> workouts;
    private final Context context;
    private final OnWorkoutClickListener listener;

    /**
     * Interface para manejar eventos de click en los workouts
     */
    public interface OnWorkoutClickListener {
        void onWorkoutClick(Workout workout);
    }

    /**
     * Constructor del adaptador
     * @param context El contexto de la aplicación
     * @param workouts Lista de entrenamientos a mostrar
     * @param listener Listener para eventos de click
     */
    public WorkoutCalendarAdapter(Context context, List<Workout> workouts, OnWorkoutClickListener listener) {
        this.context = context;
        this.workouts = workouts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Workout workout = workouts.get(position);
        holder.tvWorkoutName.setText(workout.getName());
        holder.tvWorkoutNotes.setText(workout.getNotes());
        
        // Configurar el estado y su color correspondiente
        String stateText;
        int backgroundColor;
        
        switch (workout.getState()) {
            case DatabaseHelper.WORKOUT_STATE_COMPLETED:
                stateText = context.getString(R.string.workout_state_completed);
                backgroundColor = ContextCompat.getColor(context, R.color.colorSuccess);
                break;
            case DatabaseHelper.WORKOUT_STATE_NOT_COMPLETED:
                stateText = context.getString(R.string.workout_state_not_completed);
                backgroundColor = ContextCompat.getColor(context, R.color.colorError);
                break;
            default:
                stateText = context.getString(R.string.workout_state_pending);
                backgroundColor = ContextCompat.getColor(context, R.color.colorWarning);
                break;
        }
        
        holder.tvWorkoutState.setText(stateText);
        holder.tvWorkoutState.setBackgroundColor(backgroundColor);
        
        // Configurar el click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onWorkoutClick(workout);
            }
        });
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    /**
     * Actualiza la lista de workouts
     * @param newWorkouts Nueva lista de workouts
     */
    public void updateWorkouts(List<Workout> newWorkouts) {
        this.workouts.clear();
        this.workouts.addAll(newWorkouts);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder para los items del RecyclerView
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvWorkoutName, tvWorkoutNotes, tvWorkoutState;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWorkoutName = itemView.findViewById(R.id.tvWorkoutName);
            tvWorkoutNotes = itemView.findViewById(R.id.tvWorkoutNotes);
            tvWorkoutState = itemView.findViewById(R.id.tvWorkoutState);
        }
    }
}
