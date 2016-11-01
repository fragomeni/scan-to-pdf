package br.com.fragomeni.scan_to_pdf;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

import br.com.fragomeni.scan_to_pdf.model.AppFile;
import br.com.fragomeni.scan_to_pdf.model.AppPage;
import br.com.fragomeni.scan_to_pdf.svc.EventBus;

/**
 * Activity de edição do arquivo com a listagem de páginas
 */
public class FileEditActivity extends AppCompatActivity implements Observer {

    private ScanToPDF app;
    private AppFile appFile;
    private ArrayAdapter<AppPage> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_edit);

        app = (ScanToPDF)getApplicationContext();

        EventBus.getInstance().addObserver(this);

        // ActionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Conteúdo
        GridView gridView = (GridView) findViewById(R.id.page_grid);
        final Context appContext = app;
        listAdapter = new ArrayAdapter<AppPage>(this, android.R.layout.simple_list_item_1) {

            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return new PageItemView(parent.getContext(), appFile,
                        getItem(position), position);
            }

        };
        gridView.setAdapter(listAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_file_edit, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        appFile = app.getCurrentFile();
        try {
            app.load(appFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateScreen();
    }

    private void updateScreen() {
        getSupportActionBar().setTitle(appFile.getName());
        listAdapter.clear();
        listAdapter.addAll(appFile.getPages());
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == android.R.id.home) {
            finish();
        }
        else if(id == R.id.menu_edit_properties) {
            Intent intent = new Intent(this, PropertiesActivity.class);
            app.setCurrentFile(appFile);
            startActivity(intent);
        }
        else if(id == R.id.menu_share) {
            sharePDF();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getInstance().deleteObserver(this);
    }

    private void sharePDF() {
        if(appFile.getPages().isEmpty()) {
            app.showMessage(this, "O arquivo deve ter ao menos uma página para ser compartilhado.");
            return;
        }

        File file = null;
        try {
            file = app.generatePDF(appFile);
        } catch (Exception e) {
            app.showMessage(this, "Ocorreu um erro na montagem do PDF.");
            app.logError(e);
            return;
        }

        Uri contentUri = FileProvider.getUriForFile(this,
                "br.com.fragomeni.scan_to_pdf.fileprovider", file);
        grantUriPermission("*", contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setType("image/jpeg");
        sendIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, appFile.getName());
        startActivity(Intent.createChooser(sendIntent, "Enviar PDF para"));
    }

    @Override
    public void update(Observable o, Object arg) {
        if(arg == EventBus.Event.FILE_UPDATED) {
            updateScreen();
        }
    }

    public void openCamera(View v) {
        if(app.openCamera(this, appFile)) {
            finish();
        }
    }

}
