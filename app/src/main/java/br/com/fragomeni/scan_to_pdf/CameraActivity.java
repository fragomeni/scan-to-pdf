package br.com.fragomeni.scan_to_pdf;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

import br.com.fragomeni.scan_to_pdf.model.AppFile;
import br.com.fragomeni.scan_to_pdf.model.AppPage;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PictureCallback {

    private ScanToPDF app;
    private AppFile appFile;

    private ImageButton shootButton;
    private Animation touchAnimation;
    private SurfaceView preview;

    private Camera camera;
    private SurfaceHolder surfaceHolder;

    private boolean inPreview = false;
    private boolean safeToTakePicture = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        app = (ScanToPDF)getApplicationContext();

        shootButton = (ImageButton) findViewById(R.id.camera_shoot_button);
        touchAnimation = AnimationUtils.loadAnimation(this, R.anim.alpha);
        preview = (SurfaceView) findViewById(R.id.camera_preview);

        surfaceHolder = preview.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        appFile = app.getCurrentFile();
        try {
            camera = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(inPreview) {
            camera.stopPreview();
        }
        if(camera != null) {
            camera.release();
            camera = null;
        }
        inPreview = false;
    }

    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result=null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result=size;
                }
                else {
                    int resultArea=result.width * result.height;
                    int newArea=size.width * size.height;

                    if (newArea > resultArea) {
                        result=size;
                    }
                }
            }
        }

        return result;
    }

    private Camera.Size getPictureSize(Camera.Parameters parameters) {
        Camera.Size result=null;

        for (Camera.Size size : parameters.getSupportedPictureSizes()) {
            if (result == null) {
                result=size;
            }
            else {
                int resultArea=result.width * result.height;
                int newArea=size.width * size.height;

                if (newArea > resultArea) {
                    result=size;
                }
            }
        }

        return result;
    }

    void shoot(final View v) {

        if(camera == null) {
            return;
        }

        if(safeToTakePicture) {
            touchAnimation.reset();
            v.setEnabled(false);
            v.startAnimation(touchAnimation);
            camera.takePicture(null, null, this);
            safeToTakePicture = false;
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        File f = new File(appFile.getDir(), UUID.randomUUID().toString() + ".jpg");

        try {
            camera.startPreview();

            FileOutputStream fos = new FileOutputStream(f);
            fos.write(data);
            fos.close();

            AppPage p = new AppPage();
            p.setFile(f);

            // Salva um thumbnail
            Bitmap thumb = app.decodeSampledBitmap(f.getCanonicalPath(), 100, 100);
            fos = new FileOutputStream(p.getThumbnail());
            thumb.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();

            appFile.getPages().add(p);

            app.save(appFile, false);

            safeToTakePicture = true;

            shootButton.setEnabled(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void done(View v) {
        Intent intent = new Intent(this, FileEditActivity.class);
        app.setCurrentFile(appFile);
        startActivity(intent);
        finish();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.setDisplayOrientation(90);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        if(camera == null) return;

        Camera.Parameters params = camera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        Camera.Size size = getBestPreviewSize(width, height, params);
        Camera.Size pictureSize = getPictureSize(params);
        if (size != null && pictureSize != null) {
            params.setPreviewSize(size.width, size.height);
            params.setPictureSize(pictureSize.width, pictureSize.height);
            params.setPictureSize(pictureSize.width, pictureSize.height);
            camera.setParameters(params);
            camera.startPreview();
            safeToTakePicture = true;
            inPreview=true;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

}
