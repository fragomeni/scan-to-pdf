package br.com.fragomeni.scan_to_pdf;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import br.com.fragomeni.scan_to_pdf.model.AppFile;

/**
 * Item da lista de arquivos na MainActivity
 */
public class AppFileItemView extends RelativeLayout {

    private ScanToPDF app;

    public AppFileItemView(Context context, final AppFile appFile) {
        super(context);

        app = (ScanToPDF) context.getApplicationContext();

        inflate(context, R.layout.app_file_item, this);

        TextView textView = (TextView) findViewById(R.id.app_file_name);
        textView.setText(appFile.getName());

        ImageButton editButton = (ImageButton) findViewById(R.id.app_file_edit_button);
            editButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                editFile(appFile);
            }
        });

        ImageButton deleteButton = (ImageButton) findViewById(R.id.app_file_delete_button);
            deleteButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteFile(appFile);
            }
        });

    }

    private void editFile(AppFile appFile) {
        Intent intent = new Intent(getContext(), FileEditActivity.class);
        app.setCurrentFile(appFile);
        getContext().startActivity(intent);
    }

    private void deleteFile(final AppFile appFile) {
        new AlertDialog.Builder(getContext())
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle("Excluir arquivo")
            .setMessage("Confirma a exclusão do arquivo '" +
                    appFile.getName() + "'?")
            .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        app.delete(appFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            })
            .setNegativeButton("Não", null)
            .show();
    }

}
