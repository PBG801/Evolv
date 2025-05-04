package com.example.evolv;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

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
