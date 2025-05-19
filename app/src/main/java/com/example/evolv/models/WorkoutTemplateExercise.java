package com.example.evolv.models;

/**
 * Modelo para la tabla workout_template_exercise y su relación con Exercise.
 */
public class WorkoutTemplateExercise {
    private long templateId;
    private long exerciseId;
    private int executionOrder;
    private Integer sets;
    private Integer repetitions;
    private Integer targetDuration;
    private Integer restPeriod;
    private Integer restPeriodSeries;
    private String durationType;
    private Exercise exercise; // Objeto Exercise asociado

    public WorkoutTemplateExercise(long templateId, long exerciseId, int executionOrder, Integer sets, Integer repetitions, Integer targetDuration, Integer restPeriod, Integer restPeriodSeries, String durationType, Exercise exercise) {
        this.templateId = templateId;
        this.exerciseId = exerciseId;
        this.executionOrder = executionOrder;
        this.sets = sets;
        this.repetitions = repetitions;
        this.targetDuration = targetDuration;
        this.restPeriod = restPeriod;
        this.restPeriodSeries = restPeriodSeries;
        this.durationType = durationType;
        this.exercise = exercise;
    }

    public long getTemplateId() { return templateId; }
    public void setTemplateId(long templateId) { this.templateId = templateId; }
    public long getExerciseId() { return exerciseId; }
    public void setExerciseId(long exerciseId) { this.exerciseId = exerciseId; }
    public int getExecutionOrder() { return executionOrder; }
    public void setExecutionOrder(int executionOrder) { this.executionOrder = executionOrder; }
    public Integer getSets() { return sets; }
    public void setSets(Integer sets) { this.sets = sets; }
    public Integer getRepetitions() { return repetitions; }
    public void setRepetitions(Integer repetitions) { this.repetitions = repetitions; }
    public Integer getTargetDuration() { return targetDuration; }
    public void setTargetDuration(Integer targetDuration) { this.targetDuration = targetDuration; }
    public Integer getRestPeriod() { return restPeriod; }
    public void setRestPeriod(Integer restPeriod) { this.restPeriod = restPeriod; }
    public Integer getRestPeriodSeries() { return restPeriodSeries; }
    public void setRestPeriodSeries(Integer restPeriodSeries) { this.restPeriodSeries = restPeriodSeries; }
    public String getDurationType() { return durationType; }
    public void setDurationType(String durationType) { this.durationType = durationType; }
    public Exercise getExercise() { return exercise; }
    public void setExercise(Exercise exercise) { this.exercise = exercise; }
}
