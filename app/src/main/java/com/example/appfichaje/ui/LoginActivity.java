package com.example.appfichaje.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.appfichaje.R;
import com.example.appfichaje.datos.GestorSesion;
import com.example.appfichaje.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {
    private LoginViewModel loginViewModel;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GestorSesion sesion = new GestorSesion(this);
        if (sesion.obtenerToken() != null) {
            irAMain();
            return;
        }

        setContentView(R.layout.activity_login);

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRecuperarPass = findViewById(R.id.tvRecuperarPass);
        progressBar = findViewById(R.id.progressBar);

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        loginViewModel.getResultadoLogin().observe(this, resultado -> {
            if ("EXITO".equals(resultado)) {
                irAMain();
            } else {
                Toast.makeText(this, resultado, Toast.LENGTH_LONG).show();
            }
        });

        loginViewModel.getMensajeRecuperacion().observe(this, mensaje -> {
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        });

        loginViewModel.getCargando().observe(this, cargando -> {
            progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
            btnLogin.setEnabled(!cargando);
            tvRecuperarPass.setEnabled(!cargando);
        });

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString();
            if (!email.isEmpty() && !pass.isEmpty()) {
                loginViewModel.login(email, pass);
            } else {
                Toast.makeText(this, "Complete los campos", Toast.LENGTH_SHORT).show();
            }
        });

        tvRecuperarPass.setOnClickListener(v -> mostrarDialogoRecuperacion());
    }

    private void mostrarDialogoRecuperacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recuperar Contraseña");
        builder.setMessage("Introduce tu correo electrónico para cambiar la contraseña.");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        builder.setPositiveButton("Enviar", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (!email.isEmpty()) {
                loginViewModel.solicitarRecuperacion(email);
            } else {
                Toast.makeText(this, "El correo no puede estar vacío", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void irAMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}