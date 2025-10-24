package com.example.proyectopresion001;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.proyectopresion001.api.N8nApiClient;
import com.example.proyectopresion001.utils.PdfGenerator;

public class ResultadoActivity extends AppCompatActivity {
    public static final String EXTRA_RESULTADO = "resultado";
    private static final int STORAGE_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultado);

        String resultado = getIntent().getStringExtra(EXTRA_RESULTADO);
        TextView resultadoTextView = findViewById(R.id.resultadoTextView);
        resultadoTextView.setText(resultado);

        // Enviar datos a n8n
        enviarDatosAN8n(resultado);

        Button guardarPdfButton = findViewById(R.id.PdfButton);
        guardarPdfButton.setOnClickListener(v -> {
            if (checkPermission()) {
                generarPdf();
            } else {
                requestPermission();
            }
        });

        Button volverButton = findViewById(R.id.volverButton);
        volverButton.setOnClickListener(v -> finish());
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int write = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            return write == PackageManager.PERMISSION_GRANTED
                    && read == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                startActivityForResult(intent, STORAGE_PERMISSION_CODE);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, STORAGE_PERMISSION_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    STORAGE_PERMISSION_CODE
            );
        }
    }

    private void generarPdf() {
        TextView resultadoTextView = findViewById(R.id.resultadoTextView);
        String contenido = resultadoTextView.getText().toString();
        String rutaPdf = PdfGenerator.generatePdf(this, contenido);
        if (!rutaPdf.isEmpty()) {
            Toast.makeText(this, "PDF guardado en: " + rutaPdf, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Error al generar PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void enviarDatosAN8n(String resultado) {
        // Obtener los valores del Intent
        Intent intent = getIntent();
        String presionSistolica = intent.getStringExtra("SISTOLICA");
        String presionDiastolica = intent.getStringExtra("DIASTOLICA");
        String edad = intent.getStringExtra("EDAD");

        if (presionSistolica == null) presionSistolica = "N/A";
        if (presionDiastolica == null) presionDiastolica = "N/A";
        if (edad == null) edad = "N/A";

        // Usar valores finales para el lambda
        final String finalSistolica = presionSistolica;
        final String finalDiastolica = presionDiastolica;
        final String finalEdad = edad;

        N8nApiClient.enviarDatos(finalSistolica, finalDiastolica, finalEdad, resultado,
                new N8nApiClient.ApiCallback() {
                    @Override
                    public void onSuccess(String response) {
                        Toast.makeText(ResultadoActivity.this,
                                "Datos enviados exitosamente", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(ResultadoActivity.this,
                                "Error al enviar datos: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generarPdf();
            } else {
                Toast.makeText(this, "Permiso denegado para guardar PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    generarPdf();
                } else {
                    Toast.makeText(this, "Permiso denegado para guardar PDF", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
