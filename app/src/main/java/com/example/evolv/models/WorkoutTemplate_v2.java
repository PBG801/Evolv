package com.example.evolv.models;

import java.util.List;

/**
 * Modelo para la plantilla de entrenamiento versión 2 (MVP Evolv)
 * Incluye lógica para distinguir plantillas por defecto y sesiones pausadas.
 * Cumple con el protocolo de balanceo de llaves y claridad en comentarios.
 */
public class WorkoutTemplate_v2 implements java.io.Serializable {
    private long templateId;
    private String name;
    private String workoutType;
    private String notes;
    private int userId; // -1 si es por defecto
    private List<WorkoutTemplateExercise_v2> exercises;
    // Estado de sesión (no persistente, solo para la UI)
    private boolean paused;

    public WorkoutTemplate_v2(long templateId, String name, String workoutType, String notes, int userId, List<WorkoutTemplateExercise_v2> exercises) {
        this.templateId = templateId;
        this.name = name;
        this.workoutType = workoutType;
        this.notes = notes;
        this.userId = userId;
        this.exercises = exercises;
        this.paused = false;
    }

    public boolean isDefault() {
        return userId == -1;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    // Getters y setters
    public long getTemplateId() { return templateId; }
    public String getName() { return name; }
    public String getWorkoutType() { return workoutType; }
    public String getNotes() { return notes; }
    public int getUserId() { return userId; }
    public List<WorkoutTemplateExercise_v2> getExercises() { return exercises; }
    public void setExercises(List<WorkoutTemplateExercise_v2> exercises) { this.exercises = exercises; }
}
