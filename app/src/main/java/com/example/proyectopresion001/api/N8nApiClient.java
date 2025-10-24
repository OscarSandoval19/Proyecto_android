package com.example.proyectopresion001.api;

import android.os.Handler;
import android.os.Looper;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class N8nApiClient {
    // Actualiza esta URL con la correcta de tu webhook de n8n
    private static final String N8N_WEBHOOK_URL = "https://drexxen.app.n8n.cloud/webhook/guardar-pdf";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface ApiCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    public static void enviarDatos(String presionSistolica, String presionDiastolica, String edad, String analisis, ApiCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(N8N_WEBHOOK_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("presionSistolica", presionSistolica);
                jsonBody.put("presionDiastolica", presionDiastolica);
                jsonBody.put("edad", edad);
                jsonBody.put("analisis", analisis);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonBody.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(
                        responseCode == HttpURLConnection.HTTP_OK ? conn.getInputStream() : conn.getErrorStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                }

                final String finalResponse = response.toString();
                mainHandler.post(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        callback.onSuccess(finalResponse);
                    } else {
                        callback.onError("Error HTTP: " + responseCode + " - " + finalResponse);
                    }
                });

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Error: " + e.getMessage()));
            }
        });
    }
}
