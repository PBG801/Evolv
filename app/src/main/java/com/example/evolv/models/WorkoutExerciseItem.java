package com.example.evolv.models;

public class WorkoutExerciseItem {
    private long exerciseId;
    private int sets;
    private int reps;
    private int duration; // en segundos
    private int rest;     // en segundos

    public WorkoutExerciseItem(long exerciseId) {
        this.exerciseId = exerciseId;
        this.sets = 1;
        this.reps = 10;
        this.duration = 30;
        this.rest = 30;
    }

    // Getters y setters
    public long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getRest() {
        return rest;
    }

    public void setRest(int rest) {
        this.rest = rest;
    }
}
