package com.senati.apptiendasvehiculos;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class Registrar extends AppCompatActivity {
    EditText edtMarca, edtModelo, edtColor, edtPrecio, edtPlaca;
    Button btnGuardar;

    private int editId = -1;
    private String mode = "create";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);

        edtMarca = findViewById(R.id.edtMarca);
        edtModelo = findViewById(R.id.edtModelo);
        edtColor = findViewById(R.id.edtColor);
        edtPrecio = findViewById(R.id.edtPrecio);
        edtPlaca = findViewById(R.id.edtPlaca);
        btnGuardar = findViewById(R.id.btnGuardar);

        if (getIntent() != null && "edit".equals(getIntent().getStringExtra("mode"))) {
            mode = "edit";
            editId = getIntent().getIntExtra("id", -1);
            edtMarca.setText(getIntent().getStringExtra("marca"));
            edtModelo.setText(getIntent().getStringExtra("modelo"));
            edtColor.setText(getIntent().getStringExtra("color"));
            double precio = getIntent().getDoubleExtra("precio", 0.0);
            edtPrecio.setText(String.valueOf(precio));
            edtPlaca.setText(getIntent().getStringExtra("placa"));
        }

        btnGuardar.setOnClickListener(v -> {
            String marca = edtMarca.getText().toString().trim();
            String modelo = edtModelo.getText().toString().trim();
            String color = edtColor.getText().toString().trim();
            String precioStr = edtPrecio.getText().toString().trim();
            String placa = edtPlaca.getText().toString().trim();

            if (TextUtils.isEmpty(marca) || TextUtils.isEmpty(modelo)) {
                Toast.makeText(this, "Marca y Modelo son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            double precio = 0.0;
            try {
                if (!precioStr.isEmpty()) precio = Double.parseDouble(precioStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Precio inválido", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject body = new JSONObject();
            try {
                body.put("marca", marca);
                body.put("modelo", modelo);
                body.put("color", color);
                body.put("precio", precio);
                body.put("placa", placa);
            } catch (JSONException e) {
                Log.e("Registrar", e.toString());
            }

            if ("create".equals(mode)) createVehiculo(body);
            else updateVehiculo(editId, body);
        });
    }

    private void createVehiculo(JSONObject body) {
        String url = ApiConfig.BASE_URL;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, body,
                response -> {
                    Toast.makeText(Registrar.this, "Creado correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    Log.e("Registrar - create", error == null ? "null" : error.toString());
                    Toast.makeText(Registrar.this, "Error al crear", Toast.LENGTH_SHORT).show();
                });
        Volley.newRequestQueue(this).add(req);
    }

    private void updateVehiculo(int id, JSONObject body) {
        if (id == -1) {
            Toast.makeText(this, "ID inválido para actualizar", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = ApiConfig.BASE_URL + "/" + id;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, url, body,
                response -> {
                    Toast.makeText(Registrar.this, "Actualizado correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    Log.e("Registrar - update", error == null ? "null" : error.toString());
                    Toast.makeText(Registrar.this, "Error al actualizar", Toast.LENGTH_SHORT).show();
                });
        Volley.newRequestQueue(this).add(req);
    }
}
