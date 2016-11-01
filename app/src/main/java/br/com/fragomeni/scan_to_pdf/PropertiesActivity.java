package br.com.fragomeni.scan_to_pdf;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import br.com.fragomeni.scan_to_pdf.model.AppFile;
import br.com.fragomeni.scan_to_pdf.model.AppPageSize;

/**
 * Activity para edição das propriedades do arquivo
 */
public class PropertiesActivity extends AppCompatActivity {

    private ScanToPDF app;

    private AppFile appFile;
    private boolean isNew;
    private String oldName;

    private EditText txtName, txtObs;
    private Spinner spnPageSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_properties);

        app = (ScanToPDF)getApplicationContext();

        // ActionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Formulário
        txtName = (EditText) findViewById(R.id.prop_name);
        txtObs = (EditText) findViewById(R.id.prop_obs);
        spnPageSize = (Spinner) findViewById(R.id.prop_page_size);
        SpinnerAdapter spnAdapter = new ArrayAdapter<AppPageSize>(this,
                android.R.layout.simple_spinner_dropdown_item, AppPageSize.values());
        spnPageSize.setAdapter(spnAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_properties, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        appFile = app.getCurrentFile();
        if(isNew = (appFile == null)) {
            appFile = new AppFile();
        }
        oldName = appFile.getName();

        getSupportActionBar().setTitle(appFile.getName());
        txtName.setText(appFile.getName());
        txtName.setSelection(txtName.getText().length());
        txtObs.setText(appFile.getObs());
        spnPageSize.setSelection(((ArrayAdapter<AppPageSize>)spnPageSize.getAdapter())
                .getPosition(appFile.getPageSize()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == android.R.id.home) {
            finish();
        }
        else if(id == R.id.menu_validate) {
            save();
        }

        return super.onOptionsItemSelected(item);
    }

    private void save() {
        try {
            app.hideKeyboard(this);

            // Validação
            String name = txtName.getText().toString().trim();
            if(name.isEmpty()) {
                app.showMessage(this, "O nome deve ser preenchido.");
                txtName.requestFocus();
                return;
            }

            if(app.checkDuplicateFileName(name, appFile.getDir())) {
                app.showMessage(this, "Já existe outro arquivo com este nome.");
                txtName.requestFocus();
                return;
            }

            // Dados do formulário
            appFile.setName(name);
            appFile.setObs(txtObs.getText().toString());
            appFile.setPageSize(AppPageSize.values()[spnPageSize.getSelectedItemPosition()]);

            app.save(appFile, isNew || !name.equals(oldName));

            if(!isNew || app.openCamera(this, appFile)) {
                finish();
            }

        } catch (Exception e) {
            app.logError(e);
        }
    }
}
