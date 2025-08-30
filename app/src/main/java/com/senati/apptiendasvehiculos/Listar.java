package com.senati.apptiendasvehiculos;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

public class Listar extends AppCompatActivity {
    private ListView lstVehiculos;
    private EditText etSearch;
    private Button btnSearch;
    private ProgressBar progressBar;

    private final String TAG = "Listar";
    private final ArrayList<JSONObject> rawList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar);

        lstVehiculos = findViewById(R.id.lstVehiculos);
        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
        progressBar = findViewById(R.id.progressBar);

        btnSearch.setOnClickListener(v -> {
            String q = etSearch.getText().toString().trim();
            if (q.isEmpty()) getData();
            else searchVehiculos(q);
        });

        lstVehiculos.setOnItemLongClickListener((adapterView, view, position, id) -> {
            showItemOptions(position);
            return true;
        });

        // carga inicial
        getData();
    }

    private void getData() {
        progressBar.setVisibility(View.VISIBLE);
        String url = ApiConfig.BASE_URL;
        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    renderData(response);
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "getData error: " + (error == null ? "null" : error.toString()));
                    Toast.makeText(Listar.this, "Error al obtener datos", Toast.LENGTH_SHORT).show();
                });
        Volley.newRequestQueue(this).add(req);
    }

    private void searchVehiculos(String q) {
        progressBar.setVisibility(View.VISIBLE);
        try {
            String encoded = URLEncoder.encode(q, "UTF-8");
            String url = ApiConfig.BASE_URL + "/search/all?q=" + encoded;
            JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                    response -> {
                        progressBar.setVisibility(View.GONE);
                        renderData(response);
                    },
                    error -> {
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "search error: " + (error == null ? "null" : error.toString()));
                        Toast.makeText(Listar.this, "Error en búsqueda", Toast.LENGTH_SHORT).show();
                    });
            Volley.newRequestQueue(this).add(req);
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error codificando búsqueda", Toast.LENGTH_SHORT).show();
        }
    }

    private void renderData(JSONArray jsonArray) {
        try {
            rawList.clear();
            ArrayList<String> lista = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject o = jsonArray.getJSONObject(i);
                rawList.add(o);
                String marca = o.optString("marca", "");
                String modelo = o.optString("modelo", "");
                String placa = o.optString("placa", "");
                lista.add(marca + " " + modelo + (placa.isEmpty() ? "" : " - " + placa));
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lista);
            lstVehiculos.setAdapter(adapter);
        } catch (JSONException e) {
            Log.e(TAG, "renderData error: " + e.toString());
        }
    }

    private void showItemOptions(int position) {
        if (position < 0 || position >= rawList.size()) return;
        JSONObject item = rawList.get(position);
        String marca = item.optString("marca", "");
        String modelo = item.optString("modelo", "");
        CharSequence[] options = {"Editar", "Eliminar", "Cancelar"};
        new AlertDialog.Builder(this)
                .setTitle(marca + " " + modelo)
                .setItems(options, (dialogInterface, i) -> {
                    if (i == 0) {
                        Intent it = new Intent(Listar.this, Registrar.class);
                        it.putExtra("mode", "edit");
                        it.putExtra("id", item.optInt("id", -1));
                        it.putExtra("marca", marca);
                        it.putExtra("modelo", modelo);
                        it.putExtra("color", item.optString("color", ""));
                        it.putExtra("precio", item.optDouble("precio", 0.0));
                        it.putExtra("placa", item.optString("placa", ""));
                        startActivity(it);
                    } else if (i == 1) {
                        int id = item.optInt("id", -1);
                        if (id != -1) confirmDelete(id);
                    } else {
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    private void confirmDelete(int id) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar")
                .setMessage("¿Eliminar vehículo id " + id + "?")
                .setPositiveButton("Sí", (dialog, which) -> deleteVehiculo(id))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteVehiculo(int id) {
        String url = ApiConfig.BASE_URL + "/" + id;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.DELETE, url, null,
                response -> {
                    Toast.makeText(Listar.this, "Eliminado correctamente", Toast.LENGTH_SHORT).show();
                    getData();
                },
                error -> {
                    Log.e(TAG, "delete error: " + (error == null ? "null" : error.toString()));
                    Toast.makeText(Listar.this, "Error al eliminar", Toast.LENGTH_SHORT).show();
                });
        Volley.newRequestQueue(this).add(req);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getData();
    }
}
