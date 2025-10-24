package com.example.proyectopresion001.utils;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PdfGenerator {
    public static String generatePdf(Context context, String content) {
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Paint paint = new Paint();
        paint.setTextSize(12f);

        // Dividir el contenido en líneas
        String[] lines = content.split("\n");
        float y = 50f;
        for (String line : lines) {
            page.getCanvas().drawText(line, 50f, y, paint);
            y += 20f;
        }

        pdfDocument.finishPage(page);

        // Crear nombre de archivo único con fecha y hora
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String timeStamp = sdf.format(new Date());
        String fileName = "Analisis_" + timeStamp + ".pdf";

        // Guardar PDF en la carpeta Documents
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File file = new File(path, fileName);

        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            pdfDocument.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}

