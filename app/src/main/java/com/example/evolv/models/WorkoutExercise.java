package com.example.evolv.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Modelo que representa un ejercicio específico dentro de un entrenamiento.
 * Contiene información como el número de series, repeticiones, duración objetivo,
 * períodos de descanso y si ha sido completado.
 */
public class WorkoutExercise implements Parcelable {
    private long workoutId;
    private long exerciseId;
    private int executionOrder;
    private int sets;
    private int repetitions;
    private int targetDuration;
    private int restPeriod;
    private int restPeriodSeries;
    private String durationType;
    private boolean completed;
    private String notes;
    
    // Campos opcionales adicionales (desde la tabla exercise)
    private String exerciseName;
    private String imgUrl;
    private String description;

    /**
     * Constructor principal
     */
    public WorkoutExercise(long workoutId, long exerciseId, int executionOrder, int sets, int repetitions,
                          int targetDuration, int restPeriod, int restPeriodSeries, String durationType,
                          boolean completed, String notes) {
        this.workoutId = workoutId;
        this.exerciseId = exerciseId;
        this.executionOrder = executionOrder;
        this.sets = sets;
        this.repetitions = repetitions;
        this.targetDuration = targetDuration;
        this.restPeriod = restPeriod;
        this.restPeriodSeries = restPeriodSeries;
        this.durationType = durationType;
        this.completed = completed;
        this.notes = notes;
    }

    // Getters y Setters
    public long getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(long workoutId) {
        this.workoutId = workoutId;
    }

    public long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public int getExecutionOrder() {
        return executionOrder;
    }

    public void setExecutionOrder(int executionOrder) {
        this.executionOrder = executionOrder;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }

    public int getTargetDuration() {
        return targetDuration;
    }

    public void setTargetDuration(int targetDuration) {
        this.targetDuration = targetDuration;
    }

    public int getRestPeriod() {
        return restPeriod;
    }

    public void setRestPeriod(int restPeriod) {
        this.restPeriod = restPeriod;
    }

    public int getRestPeriodSeries() {
        return restPeriodSeries;
    }

    public void setRestPeriodSeries(int restPeriodSeries) {
        this.restPeriodSeries = restPeriodSeries;
    }

    public String getDurationType() {
        return durationType;
    }

    public void setDurationType(String durationType) {
        this.durationType = durationType;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Implementación de Parcelable
    protected WorkoutExercise(Parcel in) {
        workoutId = in.readLong();
        exerciseId = in.readLong();
        executionOrder = in.readInt();
        sets = in.readInt();
        repetitions = in.readInt();
        targetDuration = in.readInt();
        restPeriod = in.readInt();
        restPeriodSeries = in.readInt();
        durationType = in.readString();
        completed = in.readByte() != 0;
        notes = in.readString();
        exerciseName = in.readString();
        imgUrl = in.readString();
        description = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(workoutId);
        dest.writeLong(exerciseId);
        dest.writeInt(executionOrder);
        dest.writeInt(sets);
        dest.writeInt(repetitions);
        dest.writeInt(targetDuration);
        dest.writeInt(restPeriod);
        dest.writeInt(restPeriodSeries);
        dest.writeString(durationType);
        dest.writeByte((byte) (completed ? 1 : 0));
        dest.writeString(notes);
        dest.writeString(exerciseName);
        dest.writeString(imgUrl);
        dest.writeString(description);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<WorkoutExercise> CREATOR = new Creator<WorkoutExercise>() {
        @Override
        public WorkoutExercise createFromParcel(Parcel in) {
            return new WorkoutExercise(in);
        }

        @Override
        public WorkoutExercise[] newArray(int size) {
            return new WorkoutExercise[size];
        }
    };
}
