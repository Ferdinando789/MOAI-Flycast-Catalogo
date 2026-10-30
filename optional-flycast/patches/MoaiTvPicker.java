package com.flycast.emulator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/** D-pad accessible local folder and file chooser for Android TV.
 * Uses the app's internal DocumentsProvider to hand back tree URIs.
 */
public final class MoaiTvPicker {
    private MoaiTvPicker() {}
    public static void open(Activity activity, AndroidStorage storage, boolean directory) {
        activity.runOnUiThread(() -> {
            if (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()) {
                new AlertDialog.Builder(activity)
                    .setTitle("Acceso a carpetas de juegos")
                    .setMessage("MOAI Flycast necesita permiso para leer archivos de Dreamcast en el almacenamiento del Streamer. Habilita 'Permitir administrar todos los archivos', volve a Flycast y toca Add Game Folder otra vez.")
                    .setPositiveButton("Abrir permisos", (dialog, which) -> {
                        try {
                            Intent i = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                                Uri.parse("package:" + activity.getPackageName()));
                            activity.startActivity(i);
                        } catch (Exception e) {
                            try { activity.startActivity(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)); }
                            catch (Exception ex) { Toast.makeText(activity, "Abrir Ajustes > Apps > Acceso especial > Todos los archivos", Toast.LENGTH_LONG).show(); }
                        }
                        storage.addStorageCallback(null);
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> storage.addStorageCallback(null))
                    .setOnCancelListener(dialog -> storage.addStorageCallback(null))
                    .show();
                return;
            }
            browse(activity, storage, Environment.getExternalStorageDirectory(), directory);
        });
    }

    private static void browse(Activity activity, AndroidStorage storage, File current, boolean directory) {
        File[] visible = current.listFiles(file -> file.canRead() && (file.isDirectory() || !directory));
        if (visible == null) {
            new AlertDialog.Builder(activity).setTitle("Sin acceso")
                .setMessage("No se pudo leer " + current.getAbsolutePath())
                .setPositiveButton("Volver", (d,w) -> {
                    File parent = current.getParentFile();
                    if (parent != null && parent.getAbsolutePath().startsWith(Environment.getExternalStorageDirectory().getAbsolutePath()))
                        browse(activity, storage, parent, directory);
                    else storage.addStorageCallback(null);
                }).show();
            return;
        }
        Arrays.sort(visible, Comparator.comparing((File f) -> !f.isDirectory())
                .thenComparing(f -> f.getName().toLowerCase(java.util.Locale.ROOT)));
        List<String> choices = new ArrayList<>();
        List<File> files = new ArrayList<>();
        File root = Environment.getExternalStorageDirectory();
        if (!current.equals(root)) {
            choices.add("⬆ Carpeta anterior");
            files.add(current.getParentFile());
        }
        if (directory) {
            choices.add("✔ Seleccionar ESTA carpeta");
            files.add(null);
        }
        for (File f : visible) {
            choices.add((f.isDirectory() ? "📁 " : "📄 ") + f.getName());
            files.add(f);
        }
        AlertDialog dlg = new AlertDialog.Builder(activity)
            .setTitle("MOAI · " + current.getAbsolutePath())
            .setItems(choices.toArray(new String[0]), (dialog, which) -> {
                File selected = files.get(which);
                if (selected == null) complete(activity, storage, current, true);
                else if (selected.isDirectory()) browse(activity, storage, selected, directory);
                else complete(activity, storage, selected, false);
            })
            .setNegativeButton("Cancelar", (dialog, which) -> storage.addStorageCallback(null))
            .setOnCancelListener(dialog -> storage.addStorageCallback(null))
            .create();
        dlg.show();
        // Android AlertDialog is focusable using the TV remote D-pad and OK.
        if (dlg.getListView() != null) dlg.getListView().requestFocus();
    }

    private static void complete(Activity activity, AndroidStorage storage, File file, boolean directory) {
        try {
            Uri uri = directory ? MoaiTvDocumentsProvider.folderUri(activity, file)
                : android.provider.DocumentsContract.buildDocumentUri(
                    MoaiTvDocumentsProvider.authority(activity), MoaiTvDocumentsProvider.documentId(file));
            storage.addStorageCallback(uri.toString());
        } catch (IOException e) {
            Toast.makeText(activity, "Error al seleccionar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            storage.addStorageCallback(null);
        }
    }
}
