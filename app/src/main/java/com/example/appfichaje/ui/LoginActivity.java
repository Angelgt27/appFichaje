package com.example.appfichaje.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appfichaje.R;
import com.example.appfichaje.datos.GestorSesion;
import com.example.appfichaje.viewmodel.LoginViewModel;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.lifecycle.ViewModelProvider;


public class LoginActivity extends AppCompatActivity {
    private LoginViewModel loginViewModel;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar si ya hay sesión
        GestorSesion sesion = new GestorSesion(this);
        if (sesion.obtenerToken() != null) {
            irAMain();
            return;
        }

        setContentView(R.layout.activity_login);

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Observadores
        loginViewModel.getResultadoLogin().observe(this, resultado -> {
            if ("EXITO".equals(resultado)) {
                irAMain();
            } else {
                Toast.makeText(this, resultado, Toast.LENGTH_LONG).show();
            }
        });

        loginViewModel.getCargando().observe(this, cargando -> {
            progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
            btnLogin.setEnabled(!cargando);
        });

        // Evento Botón
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String pass = etPassword.getText().toString();
            if (!email.isEmpty() && !pass.isEmpty()) {
                loginViewModel.login(email, pass);
            } else {
                Toast.makeText(this, "Complete los campos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void irAMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}