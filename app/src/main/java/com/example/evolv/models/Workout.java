package com.example.evolv.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo para representar un entrenamiento concreto en una fecha específica.
 * Un entrenamiento puede tener varios ejercicios asociados y mantiene un estado
 * (pendiente, completado, no completado).
 */
public class Workout implements Parcelable {
    private long workoutId;
    private String name;
    private String date;
    private String notes;
    private String state;
    private long userId;
    private List<WorkoutExercise> exercises;

    /**
     * Constructor completo para crear un nuevo objeto Workout
     */
    public Workout(long workoutId, String name, String date, String notes, String state, long userId) {
        this.workoutId = workoutId;
        this.name = name;
        this.date = date;
        this.notes = notes;
        this.state = state;
        this.userId = userId;
        this.exercises = new ArrayList<>();
    }

    /**
     * Constructor sobrecargado que inicializa también la lista de ejercicios
     */
    public Workout(long workoutId, String name, String date, String notes, String state, long userId, List<WorkoutExercise> exercises) {
        this(workoutId, name, date, notes, state, userId);
        this.exercises = exercises != null ? exercises : new ArrayList<>();
    }

    // Getters y setters

    public long getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(long workoutId) {
        this.workoutId = workoutId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public List<WorkoutExercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<WorkoutExercise> exercises) {
        this.exercises = exercises;
    }

    /**
     * Añade un ejercicio a este entrenamiento
     */
    public void addExercise(WorkoutExercise exercise) {
        if (this.exercises == null) {
            this.exercises = new ArrayList<>();
        }
        this.exercises.add(exercise);
    }

    // Implementación de Parcelable
    protected Workout(Parcel in) {
        workoutId = in.readLong();
        name = in.readString();
        date = in.readString();
        notes = in.readString();
        state = in.readString();
        userId = in.readLong();
        exercises = new ArrayList<>();
        in.readTypedList(exercises, WorkoutExercise.CREATOR);
    }

    public static final Creator<Workout> CREATOR = new Creator<Workout>() {
        @Override
        public Workout createFromParcel(Parcel in) {
            return new Workout(in);
        }

        @Override
        public Workout[] newArray(int size) {
            return new Workout[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(workoutId);
        dest.writeString(name);
        dest.writeString(date);
        dest.writeString(notes);
        dest.writeString(state);
        dest.writeLong(userId);
        dest.writeTypedList(exercises);
    }
}
