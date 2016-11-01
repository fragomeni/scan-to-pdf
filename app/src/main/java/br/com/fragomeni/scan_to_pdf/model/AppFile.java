package br.com.fragomeni.scan_to_pdf.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Dados de um arquivo do aplicativo.
 */
public class AppFile implements Comparable<AppFile> {

    private String name = "Novo arquivo";

    private String obs = "";

    private File dir;

    private AppPageSize pageSize = AppPageSize.A4;

    private List<AppPage> pages = new ArrayList<AppPage>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getObs() {
        return obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }

    public File getDir() {
        return dir;
    }

    public void setDir(File dir) {
        this.dir = dir;
    }

    public AppPageSize getPageSize() {
        return pageSize;
    }

    public void setPageSize(AppPageSize pageSize) {
        this.pageSize = pageSize;
    }

    public List<AppPage> getPages() { return pages; }

    public void setPages(List<AppPage> pages) {
        this.pages = pages;
    }

    @Override
    public int compareTo(AppFile o) {
        return name.compareTo(o.name);
    }

    @Override
    public String toString() {
        return name;
    }
}
