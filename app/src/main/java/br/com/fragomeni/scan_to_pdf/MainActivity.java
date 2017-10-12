package br.com.fragomeni.scan_to_pdf;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import br.com.fragomeni.scan_to_pdf.model.AppFile;
import br.com.fragomeni.scan_to_pdf.svc.EventBus;

/**
 * Activity inicial do aplicativo com a lista de arquivos
 */
public class MainActivity extends AppCompatActivity implements Observer {

    private ScanToPDF app;
    private ListView listView;
    private ArrayAdapter<AppFile> listAdapter;
    private static final int REQUEST_CODE = 1234;
    public  String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        app = (ScanToPDF) getApplicationContext();

        // Conteúdo
        listView = (ListView) findViewById(R.id.file_list);
        listAdapter = new ArrayAdapter<AppFile>(this, android.R.layout.simple_list_item_1) {

            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return new AppFileItemView(parent.getContext(), getItem(position));
            }

        };
        listView.setAdapter(listAdapter);

        EventBus.getInstance().addObserver(this);
        makeRequest();
          }

    @Override
    protected void onStart() {
        super.onStart();
        updateFileList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getInstance().deleteObserver(this);
    }

    private void updateFileList() {

        try {
            List<AppFile> files = app.getFileList();
            listAdapter.clear();
            listAdapter.addAll(files);

        } catch (Exception e) {
            app.logError(e);
        }
    }

    public void openPropertiesActivity(View view) {
        Intent intent = new Intent(this, PropertiesActivity.class);
        app.setCurrentFile(null);
        startActivity(intent);
    }

    @Override
    public void update(Observable o, Object arg) {
        if(arg == EventBus.Event.INDEX_UPDATED) {
            updateFileList();
        }
    }

    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_CODE);
    }protected void makeRequestCamera() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CODE);
    }
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE: {

                if (grantResults.length == 0
                        || grantResults[0] !=
                        PackageManager.PERMISSION_GRANTED) {
                    closeNow();
                     } else {
                    Log.i(TAG, "Permission has been granted by user");
                }
                return;
            }
        }
    }
    //close if permissions not granted.
    private void closeNow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            finishAffinity();
        } else {
            finish();
        }
    }

}
