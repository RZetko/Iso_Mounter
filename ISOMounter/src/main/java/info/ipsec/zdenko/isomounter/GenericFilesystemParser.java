package info.ipsec.zdenko.isomounter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Created by Zdenko on 9.2.2014.
 */
abstract public class GenericFilesystemParser {
    abstract public List<GenericDirectoryEntry> getDirectory(String path) throws IOException, FileNotFoundException;
    abstract public boolean dumpFile(GenericDirectoryEntry polozka_gen, File f);

    public void ls(String cesta) throws IOException, FileNotFoundException
    {
        List<GenericDirectoryEntry> adresar = getDirectory(cesta);

        for (GenericDirectoryEntry polozka_gen : adresar) {
            IsoDirectoryEntry polozka = (IsoDirectoryEntry)polozka_gen;
            System.out.println("Meno: ["+ polozka.getName() + "]");
            System.out.println("Datum a cas vytvorenia: " + polozka.getCreationDateTime().getTime());
            System.out.println("Dlzka suboru: "+ polozka.getSize());
            System.out.println("Umiestnenie suboru: "+ polozka.umiestnenieSuboru);
        }
    }

}