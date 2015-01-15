package info.ipsec.zdenko.isomounter;

import java.io.File;
import java.util.Calendar;

/**
 * Created by Zdenko on 16.2.2014.
 */
public class FsDirectoryEntry extends GenericDirectoryEntry {
    boolean flagIsFile;
    boolean flagIsDir;

    public FsDirectoryEntry(File f) {
        this(f, f.getName());
    }
    public FsDirectoryEntry(File f, String name) {
        size = f.length();
        creationDateTime.setTimeInMillis(f.lastModified());
        flagIsDir = f.isDirectory();
        flagIsFile = f.isFile();
        this.name =  name;
    }

    @Override
    public boolean isDir() {
        return flagIsDir;
    }

    @Override
    public boolean isFile() {
        return flagIsFile;
    }
}
