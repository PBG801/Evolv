package com.example.evolv.models;

/**
 * Modelo para la tabla workout_template.
 */
public class WorkoutTemplate {
    private long templateId;
    private String name;
    private String workoutType;
    private String notes;

    public WorkoutTemplate(long templateId, String name, String workoutType, String notes) {
        this.templateId = templateId;
        this.name = name;
        this.workoutType = workoutType;
        this.notes = notes;
    }

    public long getTemplateId() { return templateId; }
    public void setTemplateId(long templateId) { this.templateId = templateId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getWorkoutType() { return workoutType; }
    public void setWorkoutType(String workoutType) { this.workoutType = workoutType; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
