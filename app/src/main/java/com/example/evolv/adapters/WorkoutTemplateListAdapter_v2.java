package com.example.evolv.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evolv.R;
import com.example.evolv.models.WorkoutTemplate_v2;

import java.util.List;

/**
 * Adaptador para mostrar la lista de plantillas de entrenamiento (v2) con soporte para:
 * - Plantillas por defecto (azul)
 * - Sesiones pausadas (anaranjado, botón pausa)
 * - Plantillas normales (blanco, botón play)
 * <p>
 * Cumple con atomicidad y claridad, y usa recursos de color y texto definidos en XML.
 */
public class WorkoutTemplateListAdapter_v2 extends RecyclerView.Adapter<WorkoutTemplateListAdapter_v2.ViewHolder> {
    private List<WorkoutTemplate_v2> templates;
    private Context context;
    private OnTemplateActionListener listener;
    private long currentUserId; // ID del usuario actual para controlar permisos

    public interface OnTemplateActionListener {
        void onPlay(WorkoutTemplate_v2 template);

        void onPause(WorkoutTemplate_v2 template);

        void onEdit(WorkoutTemplate_v2 template);

        /**
         * Solo debe ejecutarse para plantillas de usuario (userId != -1).
         * Si se llama con una plantilla por defecto, debe ignorar la acción.
         */
        void onDelete(WorkoutTemplate_v2 template);
    }

    public WorkoutTemplateListAdapter_v2(Context context, List<WorkoutTemplate_v2> templates, OnTemplateActionListener listener) {
        this.context = context;
        this.templates = templates;
        this.listener = listener;
        this.currentUserId = 0; // Por defecto asumimos usuario anónimo hasta que se especifique lo contrario
    }
    
    /**
     * Establece el ID del usuario actual para controlar la visibilidad de los botones de edición
     * @param userId ID del usuario actual (0 = anónimo)
     */
    public void setCurrentUserId(long userId) {
        this.currentUserId = userId;
        notifyDataSetChanged(); // Actualiza la visibilidad de los botones
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_workout_template_v2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutTemplate_v2 template = templates.get(position);
        
        // Modificar el texto mostrado para indicar si está pausado
        String displayName = template.getName();
        if (template.isPaused()) {
            displayName = "[EN PAUSE] " + displayName;
        }
        holder.textTemplateName.setText(displayName);
        
        // Logs para diagnóstico visual
        android.util.Log.d("EvolvDebug", "[VISUAL] onBindViewHolder: Template " + template.getTemplateId() + 
                 ", nombre=" + template.getName() + 
                 ", displayName=" + displayName +
                 ", isPaused=" + template.isPaused() + 
                 ", isDefault=" + template.isDefault() + 
                 ", userId=" + template.getUserId());

        // 1. Determinar color de fondo según el tipo de plantilla
        if (template.isDefault()) {
            holder.itemContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.template_default_bg));
            android.util.Log.d("EvolvDebug", "[VISUAL] Template " + template.getTemplateId() + " con fondo DEFAULT");
        } else if (template.isPaused()) {
            holder.itemContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.template_paused_bg));
            android.util.Log.d("EvolvDebug", "[VISUAL] Template " + template.getTemplateId() + " con fondo PAUSADA");
        } else {
            holder.itemContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.template_normal_bg));
            android.util.Log.d("EvolvDebug", "[VISUAL] Template " + template.getTemplateId() + " con fondo NORMAL");
        }
        
        // 2. Determinar icono según el estado pausado, independiente del tipo de plantilla
        if (template.isPaused()) {
            holder.btnPlayPause.setImageResource(R.drawable.ic_pause);
            holder.btnPlayPause.setContentDescription(context.getString(R.string.btn_pause_description));
            android.util.Log.d("EvolvDebug", "[VISUAL] Template " + template.getTemplateId() + " con icono PAUSE");
        } else {
            holder.btnPlayPause.setImageResource(R.drawable.ic_play);
            holder.btnPlayPause.setContentDescription(context.getString(R.string.btn_play_description));
            android.util.Log.d("EvolvDebug", "[VISUAL] Template " + template.getTemplateId() + " con icono PLAY");
        }

        // Acciones de los botones
        holder.btnPlayPause.setOnClickListener(v -> {
            // Siempre llamamos a onPlay, independientemente del estado pausado
            listener.onPlay(template);
        });

        // Acción al hacer clic en el item (mostrar ejercicios)
        holder.itemContainer.setOnClickListener(v -> {
            List<com.example.evolv.models.WorkoutTemplateExercise_v2> exercises = template.getExercises();
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.dialog_exercise_list_title));
            if (exercises == null || exercises.isEmpty()) {
                builder.setMessage(context.getString(R.string.dialog_no_exercises));
            } else {
                StringBuilder sb = new StringBuilder();
                int idx = 1;
                for (com.example.evolv.models.WorkoutTemplateExercise_v2 ex : exercises) {
                    sb.append(idx++)
                            .append(". ")
                            .append(ex.getName());
                    // Puedes añadir sets/reps aquí si lo deseas
                    sb.append("\n");
                }
                builder.setMessage(sb.toString());
            }
            builder.setPositiveButton(android.R.string.ok, null);
            builder.show();
        });
        // Ocultar botón de edición para usuarios anónimos
        if (currentUserId == 0) {
            holder.btnEdit.setVisibility(View.GONE); // Ocultar botón de edición para usuarios anónimos
        } else {
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnEdit.setOnClickListener(v -> listener.onEdit(template));
        }
        
        // Solo permitir eliminar si la plantilla es del usuario
        if (template.getUserId() == -1) {
            holder.btnDelete.setVisibility(View.GONE); // Ocultar para plantillas por defecto
        } else {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(template));
        }
    }

    @Override
    public int getItemCount() {
        return templates.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout itemContainer;
        TextView textTemplateName;
        ImageButton btnPlayPause, btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemContainer = itemView.findViewById(R.id.item_container);
            textTemplateName = itemView.findViewById(R.id.textTemplateName);
            btnPlayPause = itemView.findViewById(R.id.btnPlayPause);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
