package com.flycast.emulator;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.DocumentsProvider;
import android.webkit.MimeTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

/** Private SAF provider for folders selected by the MOAI Android TV picker.
 *  No document is made public: provider is not exported.
 */
public class MoaiTvDocumentsProvider extends DocumentsProvider {
    static final String AUTH_SUFFIX = ".moaitvfiles";
    private static final String[] DOC_COLS = {
        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        DocumentsContract.Document.COLUMN_MIME_TYPE,
        DocumentsContract.Document.COLUMN_SIZE,
        DocumentsContract.Document.COLUMN_LAST_MODIFIED,
        DocumentsContract.Document.COLUMN_FLAGS
    };

    static String authority(android.content.Context context) {
        return context.getPackageName() + AUTH_SUFFIX;
    }
    static String documentId(File file) throws IOException {
        File base = Environment.getExternalStorageDirectory().getCanonicalFile();
        File f = file.getCanonicalFile();
        String b = base.getPath(), p = f.getPath();
        if (!p.equals(b) && !p.startsWith(b + File.separator))
            throw new IOException("Outside shared storage");
        return "primary:" + (p.equals(b) ? "" : p.substring(b.length() + 1).replace(File.separatorChar, '/'));
    }
    static android.net.Uri folderUri(android.content.Context context, File folder) throws IOException {
        return DocumentsContract.buildTreeDocumentUri(authority(context), documentId(folder));
    }
    private File resolve(String documentId) throws FileNotFoundException {
        if (!documentId.startsWith("primary:"))
            throw new FileNotFoundException(documentId);
        try {
            File base = Environment.getExternalStorageDirectory().getCanonicalFile();
            File file = new File(base, documentId.substring(8)).getCanonicalFile();
            if (!file.getPath().equals(base.getPath())
                && !file.getPath().startsWith(base.getPath() + File.separator))
                throw new FileNotFoundException("Invalid path");
            return file;
        } catch (IOException e) { throw new FileNotFoundException(e.getMessage()); }
    }
    @Override public boolean onCreate() { return true; }
    @Override public Cursor queryRoots(String[] projection) {
        String[] columns = projection == null ? new String[] {
            DocumentsContract.Root.COLUMN_ROOT_ID, DocumentsContract.Root.COLUMN_DOCUMENT_ID,
            DocumentsContract.Root.COLUMN_TITLE, DocumentsContract.Root.COLUMN_FLAGS
        } : projection;
        MatrixCursor c = new MatrixCursor(columns);
        MatrixCursor.RowBuilder row = c.newRow();
        for (String col: columns) {
            Object v = null;
            if (col.equals(DocumentsContract.Root.COLUMN_ROOT_ID)) v = "primary";
            else if (col.equals(DocumentsContract.Root.COLUMN_DOCUMENT_ID)) v = "primary:";
            else if (col.equals(DocumentsContract.Root.COLUMN_TITLE)) v = "Almacenamiento interno";
            else if (col.equals(DocumentsContract.Root.COLUMN_FLAGS)) v = DocumentsContract.Root.FLAG_SUPPORTS_IS_CHILD;
            row.add(v);
        }
        return c;
    }
    private void addRow(MatrixCursor c, File file) throws FileNotFoundException {
        MatrixCursor.RowBuilder row = c.newRow();
        String id;
        try { id = documentId(file); } catch (IOException e) { throw new FileNotFoundException(e.getMessage()); }
        for (String col: c.getColumnNames()) {
            Object v = null;
            if (col.equals(DocumentsContract.Document.COLUMN_DOCUMENT_ID)) v = id;
            else if (col.equals(DocumentsContract.Document.COLUMN_DISPLAY_NAME)) v = file.getName().isEmpty() ? "Interno" : file.getName();
            else if (col.equals(DocumentsContract.Document.COLUMN_MIME_TYPE)) {
                String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    file.getName().contains(".") ? file.getName().substring(file.getName().lastIndexOf('.') + 1).toLowerCase(Locale.ROOT) : "");
                v = file.isDirectory() ? DocumentsContract.Document.MIME_TYPE_DIR :
                    (mime == null ? "application/octet-stream" : mime);
            }
            else if (col.equals(DocumentsContract.Document.COLUMN_SIZE)) v = file.length();
            else if (col.equals(DocumentsContract.Document.COLUMN_LAST_MODIFIED)) v = file.lastModified();
            else if (col.equals(DocumentsContract.Document.COLUMN_FLAGS)) v = 0;
            row.add(v);
        }
    }
    @Override public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {
        MatrixCursor c = new MatrixCursor(projection == null ? DOC_COLS : projection);
        File file = resolve(documentId);
        if (!file.exists()) throw new FileNotFoundException(documentId);
        addRow(c, file);
        return c;
    }
    @Override public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException {
        MatrixCursor c = new MatrixCursor(projection == null ? DOC_COLS : projection);
        File parent = resolve(parentDocumentId);
        File[] files = parent.listFiles();
        if (files != null) for (File file : files) if (file.canRead()) addRow(c, file);
        return c;
    }
    @Override public ParcelFileDescriptor openDocument(String documentId, String mode, CancellationSignal signal) throws FileNotFoundException {
        File file = resolve(documentId);
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.parseMode(mode));
    }
    @Override public boolean isChildDocument(String parentDocumentId, String documentId) {
        try {
            File parent=resolve(parentDocumentId).getCanonicalFile(), child=resolve(documentId).getCanonicalFile();
            return child.getPath().startsWith(parent.getPath()+File.separator);
        } catch(IOException e) { return false; }
    }
}
