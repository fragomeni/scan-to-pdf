package br.com.fragomeni.scan_to_pdf;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import br.com.fragomeni.scan_to_pdf.model.AppFile;

/**
 * Activity para visualização de uma página do arquivo
 */
public class PageViewActivity extends AppCompatActivity {

    private ScanToPDF app;
    private AppFile appFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_page_view);
        app = (ScanToPDF)getApplicationContext();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String fileName = getIntent().getStringExtra("fileName");
        ImageView imageView = (ImageView) findViewById(R.id.page_view_image);

        Bitmap bitmap = app.decodeSampledBitmap(fileName, 1024, 1024);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                matrix, true);
        imageView.setImageBitmap(bitmap);
    }

    void close(final View v) {
        finish();
    }

}
