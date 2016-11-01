package br.com.fragomeni.scan_to_pdf.model;

import java.io.File;

/**
 * Página de um arquivo do aplicativo
 */
public class AppPage {

    private File file;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getThumbnail() {
        return new File(file.getParent(), "thumb_" + file.getName());
    }
}
