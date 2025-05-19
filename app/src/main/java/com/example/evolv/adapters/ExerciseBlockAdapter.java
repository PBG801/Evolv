package com.example.evolv.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.evolv.R;
import com.example.evolv.models.Exercise;
import java.util.List;

/**
 * Adapter para mostrar bloques agrupados de ejercicios y descansos.
 */
public class ExerciseBlockAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int TYPE_EXERCISE = 0;
    public static final int TYPE_REST = 1;

    private final List<Item> items;

    public static class Item {
    public int type; // 0: ejercicio, 1: descanso
    public Exercise exercise;
    public int sets; // número de sets
    public int reps; // repeticiones por set
    public int durationPerRep; // tiempo por repetición
    public int restDuration; // solo para descanso
    public Item(int type, Exercise exercise, int sets, int reps, int durationPerRep, int restDuration) {
        this.type = type;
        this.exercise = exercise;
        this.sets = sets;
        this.reps = reps;
        this.durationPerRep = durationPerRep;
        this.restDuration = restDuration;
    }
}

    public ExerciseBlockAdapter(List<Item> items) {
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_REST) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rest_block, parent, false);
            return new RestViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise_block, parent, false);
            return new ExerciseViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Item item = items.get(position);
        if (item.type == TYPE_EXERCISE) {
            ExerciseViewHolder vh = (ExerciseViewHolder) holder;
            vh.textName.setText(item.exercise.getName());
            // Mostrar formato: "2 sets x 5 reps" y duración total
            if (item.sets > 0 && item.reps > 0) {
                vh.textCount.setText(item.sets + " sets x " + item.reps + " reps");
                int totalDuration = item.sets * item.reps * item.durationPerRep;
                vh.textDuration.setText(totalDuration + " s");
            } else if (item.sets > 0) {
                vh.textCount.setText(item.sets + " sets");
                vh.textDuration.setText("");
            } else if (item.reps > 0) {
                vh.textCount.setText("x" + item.reps + " reps");
                vh.textDuration.setText("");
            } else {
                vh.textCount.setText("");
                vh.textDuration.setText("");
            }
            // Cargar imagen específica si existe, usando el campo img_url
            int resId = holder.itemView.getContext().getResources().getIdentifier(
                item.exercise.getImg_url(), "drawable", holder.itemView.getContext().getPackageName());
            if (resId != 0) {
                vh.imageExercise.setImageResource(resId);
            } else {
                vh.imageExercise.setImageResource(R.drawable.ic_exercise_placeholder);
            }
        } else if (item.type == TYPE_REST) {
            RestViewHolder vh = (RestViewHolder) holder;
            vh.textRest.setText(holder.itemView.getContext().getString(R.string.rest_block_placeholder, item.restDuration));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        ImageView imageExercise;
        TextView textName, textCount, textDuration;
        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            imageExercise = itemView.findViewById(R.id.imageExercise);
            textName = itemView.findViewById(R.id.textExerciseName);
            textCount = itemView.findViewById(R.id.textExerciseCount);
            textDuration = itemView.findViewById(R.id.textExerciseDuration);
        }
    }
    public static class RestViewHolder extends RecyclerView.ViewHolder {
        TextView textRest;
        public RestViewHolder(@NonNull View itemView) {
            super(itemView);
            textRest = itemView.findViewById(R.id.textRestDuration);
        }
    }
}
