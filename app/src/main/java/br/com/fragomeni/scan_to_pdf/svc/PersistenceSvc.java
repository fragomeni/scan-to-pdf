package br.com.fragomeni.scan_to_pdf.svc;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import br.com.fragomeni.scan_to_pdf.model.AppFile;
import br.com.fragomeni.scan_to_pdf.model.AppPage;
import br.com.fragomeni.scan_to_pdf.model.AppPageSize;

/**
 * Serviço de persistência dos dados do aplicativo
 */
public class PersistenceSvc {

    private File rootDir;

    public PersistenceSvc(File rootDir) {
        this.rootDir = rootDir;
    }

    public List<AppFile> loadFileList() throws Exception {

        List<AppFile> ret = new ArrayList<AppFile>();
        File indexFile = getIndexFile();

        Properties props = loadProperties(indexFile);

        for(String dir: props.stringPropertyNames()) {
            AppFile appFile = new AppFile();
            appFile.setName(props.getProperty(dir));
            appFile.setDir(new File(rootDir, dir));
            ret.add(appFile);
        }

        Collections.sort(ret);

        return ret;
    }

    public void saveFile(AppFile appFile) throws Exception {

        if(appFile.getDir() == null) {
            File fileDir = new File(rootDir, UUID.randomUUID().toString());
            fileDir.mkdir();
            appFile.setDir(fileDir);
        }

        // Cria/Atualiza file.properties
        Properties p = new Properties();
        p.setProperty("obs", appFile.getObs());
        p.setProperty("pageSize", appFile.getPageSize().toString());

        StringBuilder fNames = new StringBuilder();
        for(AppPage page: appFile.getPages()) {
            fNames.append(page.getFile().getName());
            fNames.append(";");
        }
        if(fNames.length() > 0) {
            fNames.deleteCharAt(fNames.length() - 1);
        }
        p.setProperty("pages", fNames.toString());

        p.store(new FileWriter(getPropertiesFile(appFile)), appFile.getName());

        // Atualiza índice
        File indexFile = getIndexFile();
        p = loadProperties(indexFile);
        p.setProperty(appFile.getDir().getName(), appFile.getName());
        p.store(new FileWriter(indexFile), "Índice");
    }

    public void deleteFile(AppFile appFile) throws Exception {

        // Apaga diretório
        File[] files = appFile.getDir().listFiles();
        if(files != null) {
            for(File f: files) {
                f.delete();
            }
            appFile.getDir().delete();
        }

        // Atualiza índice
        File indexFile = getIndexFile();
        Properties p = loadProperties(indexFile);
        p.remove(appFile.getDir().getName());
        p.store(new FileWriter(indexFile), "Índice");
    }

    public void loadFile(AppFile appFile) throws Exception {

        File pFile = getPropertiesFile(appFile);

        if(!pFile.isFile()) {
            throw new Exception("Arquivo de propriedades não encontrado para o arquivo " +
                "'" + appFile.getName() + "'");
        }

        Properties props = loadProperties(pFile);

        appFile.setObs(props.getProperty("obs"));
        appFile.setPageSize(AppPageSize.valueOf(props.getProperty("pageSize")));

        String[] pageFiles = props.getProperty("pages").split(";");
        appFile.getPages().clear();
        for(String pageFile: pageFiles) {
            if(!pageFile.isEmpty()) {
                AppPage p = new AppPage();
                p.setFile(new File(appFile.getDir(), pageFile));
                appFile.getPages().add(p);
            }
        }

    }

    private File getPropertiesFile(AppFile appFile) {
        return new File(appFile.getDir(), "file.properties");
    }

    private File getIndexFile() throws Exception {
        File f = new File(rootDir, "index.properties");
        if(!f.isFile()) {
            f.createNewFile();
        }
        return f;
    }

    @NonNull
    private Properties loadProperties(File file) throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(file));
        return props;
    }

}
