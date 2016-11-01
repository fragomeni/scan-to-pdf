package br.com.fragomeni.scan_to_pdf.model;

import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;

/**
 * Tamanhos de página para arquivos do aplicativo.
 */
public enum AppPageSize {

    A4(PageSize.A4),
    A5(PageSize.A5),
    CARTA(PageSize.LETTER);

    private Rectangle rectangle;

    AppPageSize(Rectangle rectangle) {
        this.rectangle = rectangle;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

}
