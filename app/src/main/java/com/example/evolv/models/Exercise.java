package com.example.evolv.models;

/**
 * Modelo para la tabla 'exercise'.
 * Los IDs son tipo long para compatibilidad con INTEGER de SQLite.
 */
public class Exercise {
    // ID único del ejercicio (corresponde a exercise_id en la base de datos)
    private long exercise_id;
    // Nombre del ejercicio
    private String name;
    // URL o path de la imagen asociada al ejercicio
    private String img_url;
    // Descripción del ejercicio
    private String description_text;
    // Fecha de creación (opcional, se puede usar para auditoría o historial)
    private String created_at;

    public Exercise() {}

    /**
     * Constructor completo para el modelo Exercise.
     * @param exercise_id ID único
     * @param name Nombre del ejercicio
     * @param img_url URL o path de la imagen
     * @param description_text Descripción del ejercicio
     * @param created_at Fecha de creación
     */
    public Exercise(long exercise_id, String name, String img_url, String description_text, String created_at) {
        this.exercise_id = exercise_id;
        this.name = name;
        this.img_url = img_url;
        this.description_text = description_text;
        this.created_at = created_at;
    }

    // Getters y Setters
    public long getExercise_id() { return exercise_id; }
    public void setExercise_id(long exercise_id) { this.exercise_id = exercise_id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImg_url() { return img_url; }
    public void setImg_url(String img_url) { this.img_url = img_url; }

    public String getDescription_text() { return description_text; }
    public void setDescription_text(String description_text) { this.description_text = description_text; }

    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
}
