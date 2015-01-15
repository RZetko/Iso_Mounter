package info.ipsec.zdenko.isomounter;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by Zdenko on 10.2.2014.
 */
abstract public class GenericDirectoryEntry implements Serializable{
    protected String name;
    protected long size;
    protected Calendar creationDateTime;

    GenericDirectoryEntry() {
        creationDateTime = Calendar.getInstance();
    }

    public String getName() {
        return name;
    }
    public long getSize() {
        return size;
    }
    public Calendar getCreationDateTime() {
        return creationDateTime;
    }

    abstract public boolean isDir();
    abstract public boolean isFile();
}