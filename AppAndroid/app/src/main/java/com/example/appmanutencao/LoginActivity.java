package com.example.appmanutencao;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editSenha;
    private Button btnLogin;

    // Login do administrador
    private static final String ADMIN_EMAIL = "adm@solar.com";
    private static final String SENHA_PADRAO = "123456";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.editSenha);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String usuario = editEmail.getText().toString().trim();
            String senha = editSenha.getText().toString().trim();

            if (usuario.isEmpty() || senha.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!senha.equals(SENHA_PADRAO)) {
                Toast.makeText(LoginActivity.this, "Senha incorreta!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (usuario.equalsIgnoreCase(ADMIN_EMAIL)) {
                // ADMIN: vai para a tela do administrador
                Intent intent = new Intent(LoginActivity.this, OrdemServicoActivity.class);
                intent.putExtra("tipoPerfil", "ADMIN");
                intent.putExtra("usuarioAtual", "ADMIN");
                startActivity(intent);
                finish();
            } else {
                // CLIENTE: vai para a tela de agendamento
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("tipoPerfil", "CLIENTE");
                intent.putExtra("usuarioAtual", usuario);
                startActivity(intent);
                finish();
            }
        });
    }
}