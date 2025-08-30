package com.senati.apptiendasvehiculos;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class Actualizar extends AppCompatActivity {
    // Si no lo usas, elimina este Activity y reusa Registrar en mode=edit.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actualizar);
        // Puedes redirigir a Registrar si prefieres:
        // Intent it = new Intent(this, Registrar.class); startActivity(it); finish();
    }
}

