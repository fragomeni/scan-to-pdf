package br.com.fragomeni.scan_to_pdf;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.text.Normalizer;
import java.util.List;

import br.com.fragomeni.scan_to_pdf.model.AppFile;
import br.com.fragomeni.scan_to_pdf.model.AppPage;
import br.com.fragomeni.scan_to_pdf.svc.EventBus;
import br.com.fragomeni.scan_to_pdf.svc.PersistenceSvc;

/**
 * Contexto principal do aplicativo
 */
public class ScanToPDF extends Application {

    private static final int PERMISSION_REQUEST_CAMERA = 1;

    private PersistenceSvc ps;

    private List<AppFile> fileList;

    private AppFile currentFile;

    private File shareDir;

    @Override
    public void onCreate() {
        super.onCreate();
        ps = new PersistenceSvc(getFilesDir());

        shareDir = new File(getFilesDir(), "share");
        if(!shareDir.isDirectory()) {
            shareDir.mkdirs();
        }
    }

    public List<AppFile> getFileList() throws Exception {
        fileList = ps.loadFileList();
        return fileList;
    }

    public void load(AppFile appFile) throws Exception {
        ps.loadFile(appFile);
    }

    public void save(AppFile appFile, boolean isNameChanged) throws Exception {
        ps.saveFile(appFile);
        EventBus.getInstance().notifyObservers(EventBus.Event.FILE_UPDATED);
        if(isNameChanged) {
            EventBus.getInstance().notifyObservers(EventBus.Event.INDEX_UPDATED);
        }
    }

    public void delete(AppFile appFile) throws Exception {
        ps.deleteFile(appFile);
        EventBus.getInstance().notifyObservers(EventBus.Event.INDEX_UPDATED);
    }

    public boolean checkDuplicateFileName(String fileName, File dir) {
        for(AppFile f: fileList) {
            if(f.getName().equalsIgnoreCase(fileName) &&
                    (dir == null || !f.getDir().getName().equals(dir.getName()))) {
                return true;
            }
        }
        return false;
    }

    public void showMessage(Activity activity, String message) {
        Toast toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void logError(Exception e) {
        Log.e("Scan-to-PDF", e.getLocalizedMessage(), e);
    }

    public void hideKeyboard(Activity activity) {
        // Check if no view has focus:
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public AppFile getCurrentFile() {
        return currentFile;
    }

    public void setCurrentFile(AppFile currentFile) {
        this.currentFile = currentFile;
    }

    public boolean openCamera(Activity activity, AppFile appFile) {

        // Check permission
        if(ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.i("Scan-to-PDF", "Sem permissão de uso da câmera.");

            if(ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.CAMERA)) {
                showMessage(activity, "Este aplicativo necessita de acesso à" +
                        " câmera do dispositivo para seu funcionamento.");
            }
            else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.CAMERA},
                        PERMISSION_REQUEST_CAMERA);
            }

            return false;
        }

        // Open action
        Intent intent = new Intent(activity, CameraActivity.class);
        setCurrentFile(appFile);
        activity.startActivity(intent);
        return true;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmap(String fileName, int reqWidth, int reqHeight) {

        // Verifica dimensões da imagem
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileName, options);

        // Calcula tamanho para redimensionamento
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Carrega o bitmap redimensionando
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(fileName, options);
    }

    public File generatePDF(AppFile appFile) throws Exception {

        File f = new File(shareDir,
                stripAccents(appFile.getName()).replaceAll("[^a-zA-Z0-9_ ]", "_") + ".pdf");

        Rectangle pageSize = appFile.getPageSize().getRectangle();

        Document document = new Document(pageSize, 0, 0, 0, 0);

        PdfWriter.getInstance(document, new FileOutputStream(f));

        document.open();

        for(AppPage p: appFile.getPages()) {
            Image image = Image.getInstance(p.getFile().getCanonicalPath());
            image.scaleToFit(pageSize.rotate());
            image.setRotationDegrees(-90);
            document.add(image);
            document.newPage();
        }

        document.close();

        return f;
    }

    public static String stripAccents(String s) {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return s;
    }

}
