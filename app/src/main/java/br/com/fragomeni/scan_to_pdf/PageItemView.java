package br.com.fragomeni.scan_to_pdf;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import br.com.fragomeni.scan_to_pdf.model.AppFile;
import br.com.fragomeni.scan_to_pdf.model.AppPage;

/**
 * Item da lista de páginas na FileEditActivity
 */
public class PageItemView extends RelativeLayout {

    private ScanToPDF app;

    private AppFile appFile;
    private AppPage page;
    private int position;

    public PageItemView(Context context, AppFile appFile, AppPage page, int position) {
        super(context);

        this.appFile = appFile;
        this.page = page;
        this.position = position;

        app = (ScanToPDF) context.getApplicationContext();

        inflate(context, R.layout.page_item, this);

        ImageView thumbnail = (ImageView) findViewById(R.id.page_thumbnail);
        thumbnail.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                view();
            }
        });
        thumbnail.setImageBitmap(BitmapFactory.decodeFile(page.getThumbnail().getAbsolutePath()));

        ImageButton viewButton = (ImageButton) findViewById(R.id.page_view_button);
        viewButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               view();
            }
        });

        ImageButton deleteButton = (ImageButton) findViewById(R.id.page_delete_button);
        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               delete();
            }
        });

        ImageButton moveButton = (ImageButton) findViewById(R.id.page_move_button);
        moveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               move();
            }
        });
        if(position == 0) {
            moveButton.setVisibility(View.INVISIBLE);
        }
    }

    private void move() {
        try {
            appFile.getPages().remove(position);
            appFile.getPages().add(position - 1, page);
            app.save(appFile, false);
        } catch (Exception e) {
            app.logError(e);
        }
    }

    private void delete() {
        new AlertDialog.Builder(getContext())
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle("Excluir página")
            .setMessage("Confirma a exclusão da página?")
            .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        appFile.getPages().remove(position);
                        app.save(appFile, false);
                    } catch (Exception e) {
                        app.logError(e);
                    }
                }
            })
            .setNegativeButton("Não", null)
            .show();
    }

    private void view() {
        Intent intent = new Intent(getContext(), PageViewActivity.class);
        intent.putExtra("fileName", page.getFile().getAbsolutePath());
        getContext().startActivity(intent);
    }

}
