package com.example.evolv.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evolv.R;
import com.example.evolv.models.WorkoutTemplateExercise_v2;

import java.util.List;

/**
 * Adapter para mostrar los ejercicios asociados a una plantilla.
 */
public class WorkoutTemplateExerciseAdapter extends RecyclerView.Adapter<WorkoutTemplateExerciseAdapter.ViewHolder> {
    private int selectedPosition = RecyclerView.NO_POSITION;
    private OnItemClickListener itemClickListener;
    private final List<com.example.evolv.models.Exercise> allExercises;
    private final List<com.example.evolv.models.WorkoutTemplateExercise_v2> selectedExercises;
    private final OnExerciseEditListener editListener;
    private final long currentUserId; // ID del usuario actual para controlar permisos de edición

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int position) {
        int previous = selectedPosition;
        selectedPosition = position;

        // Notificar cambios para actualizar el aspecto visual
        if (previous != RecyclerView.NO_POSITION) {
            notifyItemChanged(previous);
        }
        if (selectedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(selectedPosition);
        }

        // Log para depuración
        android.util.Log.d("EvolvDebug", "[SELECT] Nueva posición seleccionada: " + position);
    }

    public interface OnExerciseEditListener {
        void onEdit(com.example.evolv.models.WorkoutTemplateExercise_v2 wte, int position);
    }

    public WorkoutTemplateExerciseAdapter(List<com.example.evolv.models.Exercise> allExercises, List<com.example.evolv.models.WorkoutTemplateExercise_v2> selectedExercises, OnExerciseEditListener editListener, long userId) {
        this.allExercises = allExercises;
        this.selectedExercises = selectedExercises;
        this.editListener = editListener;
        this.currentUserId = userId;
    }

    // Utilidad: obtener el índice en selectedExercises según exerciseId
    private int getSelectedIndex(long exerciseId) {
        for (int i = 0; i < selectedExercises.size(); i++) {
            if (selectedExercises.get(i).getExerciseId() == exerciseId) return i;
        }
        return -1;
    }

    // Devuelve el índice del ejercicio en allExercises que corresponde a la posición de selectedExercises
    private int getSelectedIndexForPosition(int position) {
        if (position < selectedExercises.size()) {
            long exerciseId = selectedExercises.get(position).getExerciseId();
            for (int i = 0; i < allExercises.size(); i++) {
                if (allExercises.get(i).getExercise_id() == exerciseId) {
                    return i;
                }
            }
        }
        return 0; // Por defecto, selecciona el primer elemento
    }

    @Override
    public int getItemCount() {
        // Mostrar una fila extra para añadir nuevo ejercicio
        return selectedExercises.size() + 1;
    }

    public List<WorkoutTemplateExercise_v2> getSelectedExercises() {
        android.util.Log.d("CREA", "[Adapter][getSelectedExercises] size=" + selectedExercises.size());
        for (WorkoutTemplateExercise_v2 ex : selectedExercises) {
            android.util.Log.d("CREA", "[Adapter][getSelectedExercises] seleccionado: " + ex.getName());
        }
        return selectedExercises;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    // Si es la última posición, es la fila "añadir ejercicio"
    if (position == selectedExercises.size()) {
        holder.textExerciseOrder.setText("");
        // Spinner con hint y solo ejercicios NO seleccionados
        java.util.List<com.example.evolv.models.Exercise> disponibles = new java.util.ArrayList<>();
        for (com.example.evolv.models.Exercise ex : allExercises) {
            boolean yaSeleccionado = false;
            for (com.example.evolv.models.WorkoutTemplateExercise_v2 wte : selectedExercises) {
                if (wte.getExerciseId() == ex.getExercise_id()) {
                    yaSeleccionado = true;
                    break;
                }
            }
            if (!yaSeleccionado || selectedExercises.size() >= allExercises.size()) disponibles.add(ex);
        }
        java.util.List<String> nombres = new java.util.ArrayList<>();
        nombres.add("Selecciona ejercicio...");
        for (com.example.evolv.models.Exercise ex : disponibles) nombres.add(ex.getName());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(holder.itemView.getContext(), android.R.layout.simple_spinner_item, nombres);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinnerExercise.setAdapter(adapter);
        holder.spinnerExercise.setSelection(0);
        holder.btnEditExercise.setVisibility(View.GONE);
        holder.btnDeleteExercise.setVisibility(View.GONE);
        holder.spinnerExercise.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean first = true;
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (first) { first = false; return; }
                if (pos > 0) {
                    com.example.evolv.models.Exercise selected = disponibles.get(pos - 1);
                    com.example.evolv.models.WorkoutTemplateExercise_v2 nuevo = new com.example.evolv.models.WorkoutTemplateExercise_v2(
                        selected.getExercise_id(), selected.getName(), 1, 10, 10, selectedExercises.size() + 1, "", "");
                    selectedExercises.add(nuevo);
                    notifyItemInserted(selectedExercises.size() - 1);
                    notifyItemChanged(selectedExercises.size());
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        return;
    }

        // SPIN: Estado al pintar ítem
        android.util.Log.d("SPIN", "onBindViewHolder position=" + position + ", selectedExercises.size=" + selectedExercises.size());
        if (position < selectedExercises.size()) {
            android.util.Log.d("SPIN", "Ejercicio seleccionado: " + selectedExercises.get(position).getName() + " (id=" + selectedExercises.get(position).getExerciseId() + ")");
        } else {
            android.util.Log.d("SPIN", "Ejercicio no seleccionado en esta posición (posición libre)");
        }
        final int posFinal = position;
        final WorkoutTemplateExercise_v2 wte = posFinal < selectedExercises.size() ? selectedExercises.get(posFinal) : null;
        // 1. Mostrar el número de orden a la izquierda de cada ítem
        holder.textExerciseOrder.setText(String.valueOf(posFinal + 1));
        // 2. Poblar el Spinner con todos los ejercicios y seleccionar el correcto
        java.util.List<String> nombres = new java.util.ArrayList<>();
        for (com.example.evolv.models.Exercise ex : allExercises) {
            nombres.add(ex.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            holder.itemView.getContext(),
            android.R.layout.simple_spinner_item,
            nombres
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinnerExercise.setAdapter(adapter);
        
        // Encontrar y seleccionar el ejercicio correcto en el spinner
        int spinnerPosition = 0;
        for (int i = 0; i < allExercises.size(); i++) {
            if (allExercises.get(i).getExercise_id() == wte.getExerciseId()) {
                spinnerPosition = i;
                break;
            }
        }
        holder.spinnerExercise.setSelection(spinnerPosition);
        holder.spinnerExercise.setEnabled(false);
        
        // Solo mostrar botón de edición si el usuario no es anónimo (userId != 0)
        holder.btnEditExercise.setVisibility(currentUserId != 0 ? View.VISIBLE : View.GONE);
        holder.btnDeleteExercise.setVisibility(View.VISIBLE);

        // Highlight the selected item if needed
        if (position == selectedPosition) {
            holder.itemView.setBackgroundColor(android.graphics.Color.LTGRAY);
        } else {
            holder.itemView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        }
        
        // Añadir listener de clic para seleccionar este ejercicio (solo si no es el ítem "añadir")
        if (position < selectedExercises.size()) {
            holder.itemView.setOnClickListener(v -> {
                int previousSelected = selectedPosition;
                selectedPosition = holder.getAdapterPosition();
                
                // Actualizar la vista del ítem previamente seleccionado
                if (previousSelected != RecyclerView.NO_POSITION) {
                    notifyItemChanged(previousSelected);
                }
                
                // Actualizar la vista del ítem recién seleccionado
                notifyItemChanged(selectedPosition);
                
                // Log de depuración
                android.util.Log.d("EvolvDebug", "[SELECT] Ejercicio seleccionado en posición: " + selectedPosition + 
                        " - " + (selectedPosition < selectedExercises.size() ? selectedExercises.get(selectedPosition).getName() : "N/A"));
                
                // Notificar al listener si existe
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(v, selectedPosition);
                }
            });
        } else {
            // Para el ítem "añadir", eliminamos cualquier listener de clic previo
            holder.itemView.setOnClickListener(null);
        }

        // 4. Listener para el Spinner:
        //    - Si el usuario selecciona un ejercicio en el último Spinner de la lista, se añade a la lista de seleccionados.
        //    - Si cambia el ejercicio de una fila ya seleccionada, se actualiza el modelo.
        holder.spinnerExercise.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
    // SPIN: Listener de selección de ejercicio
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (pos == allExercises.size()) {
    com.example.evolv.models.Exercise selectedExercise = allExercises.get(pos - 1);
    WorkoutTemplateExercise_v2 newExercise = new WorkoutTemplateExercise_v2(
        selectedExercise.getExercise_id(),
        selectedExercise.getName(),
        1,   // sets por defecto
        10,  // reps por defecto
        10,  // targetDuration por defecto (10 segundos)
        60,  // restPeriod por defecto
        "time", // durationType por defecto
        selectedExercise.getImg_url(), // imagen
        selectedExercises.size() + 1, // executionOrder 
        30   // restPeriodSeries por defecto
    );
    selectedExercises.add(newExercise);
    android.util.Log.d("SPIN", "Ejercicio añadido a selectedExercises: " + newExercise.getName() + " (id=" + newExercise.getExerciseId() + ")"); // SPIN
    android.util.Log.d("SPIN", "selectedExercises.size tras añadir: " + selectedExercises.size()); // SPIN
    notifyItemInserted(selectedExercises.size() - 1);
    android.util.Log.d("SPIN", "notifyItemInserted llamado tras añadir ejercicio"); // SPIN
} else if (wte != null) {
    com.example.evolv.models.Exercise selectedExercise = allExercises.get(pos);
    long oldId = wte.getExerciseId();
    wte.setExerciseId(selectedExercise.getExercise_id());
    wte.setName(selectedExercise.getName());
    android.util.Log.d("SPIN", "Ejercicio actualizado en posición " + posFinal + ": " + selectedExercise.getName() + " (id=" + selectedExercise.getExercise_id() + "), antes id=" + oldId); // SPIN
    notifyItemChanged(posFinal);
    android.util.Log.d("SPIN", "notifyItemChanged llamado tras actualizar ejercicio"); // SPIN
}
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // 5. Listener para el botón de editar:
        //    - Llama al listener externo para mostrar el diálogo de edición de parámetros del ejercicio
        holder.btnEditExercise.setOnClickListener(v -> {
            if (editListener != null && wte != null) {
                editListener.onEdit(wte, posFinal);
            }
        });

        // 6. Listener para el botón de eliminar:
        //    - Elimina el ejercicio de la lista y actualiza el RecyclerView
        // Listener para eliminar el ejercicio de la lista
        holder.btnDeleteExercise.setOnClickListener(v -> {
            if (posFinal < selectedExercises.size()) {
    String nombre = selectedExercises.get(posFinal).getName();
    long id = selectedExercises.get(posFinal).getExerciseId();
    selectedExercises.remove(posFinal);
    android.util.Log.d("SPIN", "Ejercicio eliminado de selectedExercises: " + nombre + " (id=" + id + ") en posición " + posFinal); // SPIN
    android.util.Log.d("SPIN", "selectedExercises.size tras eliminar: " + selectedExercises.size()); // SPIN
    notifyItemRemoved(posFinal);
    android.util.Log.d("SPIN", "notifyItemRemoved llamado tras eliminar ejercicio"); // SPIN
    notifyItemRangeChanged(posFinal, selectedExercises.size());
    android.util.Log.d("SPIN", "notifyItemRangeChanged llamado tras eliminar ejercicio"); // SPIN
}
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise_spinner, parent, false);
        return new ViewHolder(view);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textExerciseOrder;
        Spinner spinnerExercise;
        ImageButton btnEditExercise;
        ImageButton btnDeleteExercise;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textExerciseOrder = itemView.findViewById(R.id.textExerciseOrder);
            spinnerExercise = itemView.findViewById(R.id.spinnerExercise);
            btnEditExercise = itemView.findViewById(R.id.btnEditExercise);
            btnDeleteExercise = itemView.findViewById(R.id.btnDeleteExercise);
        }
    }
}
