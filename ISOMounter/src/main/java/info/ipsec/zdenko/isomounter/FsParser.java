package info.ipsec.zdenko.isomounter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Zdenko on 16.2.2014.
 */
public class FsParser extends GenericFilesystemParser {

    private File start;

    public FsParser(File start) {
        this.start = start;

    }

    @Override
    public List<GenericDirectoryEntry> getDirectory(String path) throws IOException, FileNotFoundException {
        List<GenericDirectoryEntry> zoznam = new ArrayList<GenericDirectoryEntry>();
        String tmp = path;
        String[] cesta;
        File tmpFile = start;

        if (tmp == "" || tmp.charAt(0) != '/')
            tmp = "/" + tmp;

        tmp = tmp.replaceAll("/+", "/");
        tmp = tmp.replaceAll("/$", "");

        tmpFile = new File(start, tmp);

        GenericDirectoryEntry e = new FsDirectoryEntry(tmpFile, "..");
        zoznam.add(e);

        if (!tmpFile.isDirectory())
            return zoznam;

        final File[] obsah = tmpFile.listFiles();

        if (obsah == null)
            return zoznam;
        else {
            Arrays.sort(obsah, new Comparator<File>(){
                public int compare(File f1, File f2)
                {
                    if (f1.isDirectory() && !f2.isDirectory())
                        return -1;
                    else if (!f1.isDirectory() && f2.isDirectory())
                        return 1;
                    else
                        return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());


                } });
        }


        for (File polozka : obsah) {

            e = new FsDirectoryEntry(polozka);
            zoznam.add(e);
        }
        return zoznam;
    }

    @Override
    public boolean dumpFile(GenericDirectoryEntry polozka_gen, File f) {
        return false;
    }

}
