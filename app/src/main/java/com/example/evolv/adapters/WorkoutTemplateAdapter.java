package com.example.evolv.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evolv.R;
import com.example.evolv.models.WorkoutTemplate_v2;

import java.util.List;

/**
 * Adapter para mostrar la lista de plantillas de entrenamiento.
 */
public class WorkoutTemplateAdapter extends RecyclerView.Adapter<WorkoutTemplateAdapter.ViewHolder> {
    private final List<WorkoutTemplate_v2> templates;
    private final OnTemplateClickListener listener;

    /**
     * Actualiza la lista de plantillas y refresca el RecyclerView.
     * Se utiliza cuando cambian los datos (filtrado, borrado, etc.).
     * @param nuevasPlantillas Nueva lista de plantillas
     */
    /**
     * Actualiza la lista de plantillas y refresca el RecyclerView.
     * Se utiliza cuando cambian los datos (filtrado, borrado, etc.).
     *
     * @param nuevasPlantillas Nueva lista de plantillas
     */
    public void updateTemplates(List<WorkoutTemplate_v2> nuevasPlantillas) {
        this.templates.clear();
        if (nuevasPlantillas != null) {
            this.templates.addAll(nuevasPlantillas);
        }
        notifyDataSetChanged();
    }

    /**
     * Interfaz para manejar eventos de interacción sobre las plantillas.
     * Permite notificar a la Activity cuando el usuario selecciona, edita, etc.
     */
    public interface OnTemplateClickListener {
        void onTemplateClick(long templateId);

        void onTemplateEdit(long templateId);

        void onTemplateDelete(WorkoutTemplate_v2 template);
    }

    /**
     * Constructor del adaptador.
     *
     * @param templates Lista inicial de plantillas (puede ser null)
     * @param listener  Listener para eventos de click y edición
     */
    public WorkoutTemplateAdapter(List<WorkoutTemplate_v2> templates, OnTemplateClickListener listener) {
        this.templates = (templates != null) ? templates : new java.util.ArrayList<>(); // Previene NullPointerException
        this.listener = listener;
    }

    @NonNull
    @Override
    /**
     * Infla la vista de cada elemento (item) del RecyclerView.
     */
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout_template_v2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    /**
     * Asocia los datos de cada plantilla a la vista correspondiente.
     * Configura los listeners de click y edición.
     * Si hay distinción visual (por defecto/propia), debe implementarse aquí.
     */
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutTemplate_v2 template = templates.get(position);
        holder.textName.setText(template.getName());
        // Listener para abrir detalle de plantilla
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTemplateClick(template.getTemplateId());
            }
        });
        // Listener para editar plantilla (icono lápiz)
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTemplateEdit(template.getTemplateId());
            }
        });

    }


    @Override
    public int getItemCount() {
        return templates.size();
    }

    /**
     * ViewHolder para los elementos del RecyclerView.
     * Mantiene referencias a las vistas de cada plantilla para optimizar el rendimiento.
     */
    /**
     * ViewHolder para los elementos del RecyclerView.
     * Mantiene referencias a las vistas de cada plantilla para optimizar el rendimiento.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName;
        ImageButton btnEdit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textTemplateName);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}