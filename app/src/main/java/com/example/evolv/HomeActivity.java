package com.example.evolv;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class HomeActivity extends AppCompatActivity {
    private TextView tvWelcome;
    private MaterialButton btnLogout;
    private boolean isAnonymous;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvWelcome = findViewById(R.id.tvWelcome);
        btnLogout = findViewById(R.id.btnLogout);

        String username = getIntent().getStringExtra("username");
        isAnonymous = getIntent().getBooleanExtra("isAnonymous", false);

        if (username != null && !username.isEmpty()) {
            if (isAnonymous) {
                tvWelcome.setText(username);
            } else {
                tvWelcome.setText(getString(R.string.welcome) + ", " + username + "!");
            }
        }

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Evitar que el usuario regrese a la pantalla de login usando el botón back
        if (isAnonymous) {
            super.onBackPressed();
        } else {
            moveTaskToBack(true);
        }
    }
}
