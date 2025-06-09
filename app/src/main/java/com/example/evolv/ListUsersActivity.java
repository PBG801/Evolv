package com.example.evolv;

import android.os.Bundle; // NECESARIO: Para el ciclo de vida de la Activity
import androidx.appcompat.app.AppCompatActivity; // NECESARIO: Clase base para la Activity

public class ListUsersActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Listar usuarios en el logcat
        DatabaseHelper db = new DatabaseHelper(this);
        db.listUsers();
        db.close();
        
        // Cerrar la actividad
        finish();
    }
}
