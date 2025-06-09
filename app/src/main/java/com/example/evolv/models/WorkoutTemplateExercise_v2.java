package com.example.evolv.models;

/**
 * Modelo para un ejercicio asociado a una plantilla de entrenamiento (v2).
 * Incluye todos los parámetros críticos para la ejecución y validación.
 * Cumple con el protocolo de balanceo de llaves y claridad en comentarios.
 */
public class WorkoutTemplateExercise_v2 implements java.io.Serializable {
    private long exerciseId;
    private String name;
    private int sets;
    private int repetitions;
    private int targetDuration; // en segundos
    private int restPeriod;     // en segundos
    private String durationType; // "reps" o "time"
    private String img_url; // nombre del recurso de imagen
    private int executionOrder; // orden de ejecución en la plantilla
    private int restPeriodSeries; // descanso entre series en segundos

    public WorkoutTemplateExercise_v2(long exerciseId, String name, int sets, int repetitions, int targetDuration, int restPeriod, String durationType, String img_url, int executionOrder, int restPeriodSeries) {
        this.exerciseId = exerciseId;
        this.name = name;
        this.sets = sets;
        this.repetitions = repetitions;
        this.targetDuration = targetDuration;
        this.restPeriod = restPeriod;
        this.durationType = durationType;
        this.img_url = img_url;
        this.executionOrder = executionOrder;
        this.restPeriodSeries = restPeriodSeries;
    }

    public WorkoutTemplateExercise_v2(long exerciseId, String name, int sets, int repetitions, int targetDuration, int restPeriod, String durationType, String img_url) {
        this(exerciseId, name, sets, repetitions, targetDuration, restPeriod, durationType, img_url, 0, 0);
    }

    // Constructor anterior para compatibilidad
    public WorkoutTemplateExercise_v2(long exerciseId, String name, int sets, int repetitions, int targetDuration, int restPeriod, String durationType) {
        this(exerciseId, name, sets, repetitions, targetDuration, restPeriod, durationType, null, 0, 0);
    }

    // Getters y setters
    public long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSets() {
        return sets;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public int getTargetDuration() {
        return targetDuration;
    }

    public int getRestPeriod() {
        return restPeriod;
    }

    public String getDurationType() {
        return durationType;
    }

    public String getImg_url() {
        return img_url;
    }

    // Alias para compatibilidad con WorkoutPlayerActivity_v2
    public int getDuration() {
        return targetDuration;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }

    public void setTargetDuration(int targetDuration) {
        this.targetDuration = targetDuration;
    }

    public void setRestPeriod(int restPeriod) {
        this.restPeriod = restPeriod;
    }

    public void setDurationType(String durationType) {
        this.durationType = durationType;
    }
    
    public int getExecutionOrder() {
        return executionOrder;
    }

    public void setExecutionOrder(int executionOrder) {
        this.executionOrder = executionOrder;
    }

    public int getRestPeriodSeries() {
        return restPeriodSeries;
    }

    public void setRestPeriodSeries(int restPeriodSeries) {
        this.restPeriodSeries = restPeriodSeries;
    }
}

