package info.ipsec.zdenko.isomounter;

/**
 * Created by Zdenko on 11/10/13.
 */
import java.io.*;
import java.nio.*;
import java.util.*;

class IsoParser extends GenericFilesystemParser {

    private int dlzkaBloku = 2048, joliet = 0;
    private IsoDirectoryEntry rootdir;

    private RandomAccessFile iso;

    public IsoParser(String path) throws FileNotFoundException, IOException {
        int i;

        System.out.println("vznika novy objekt triedy IsoParser s cestou "+path);
        File isof = new File(path);
        iso = new RandomAccessFile(isof, "r");

        iso.seek(16*dlzkaBloku);

        byte[] volumeDescArr = new byte[2048];
        ByteBuffer volumeDescBuff;
        while (true) {
            iso.readFully(volumeDescArr);

            volumeDescBuff = ByteBuffer.wrap(volumeDescArr);
            volumeDescBuff.order(ByteOrder.LITTLE_ENDIAN);

            int typ = volumeDescBuff.get(0) & 0xff;

            // TODO: testovat string na offste 1 s dlzkou 5 bajtov
            // na pritomnost CD001 - ak nie je, vyhodit exception.

            if (typ == 255) {
                break;
            } else if (typ == 1) { //primary volume descriptor
                dlzkaBloku = volumeDescBuff.getShort(128) & 0xffff;
                byte[] rootDirEntry = Arrays.copyOfRange(volumeDescArr, 156, 156 + 34);
                List<GenericDirectoryEntry> pomocna = interneLs(rootDirEntry);
                rootdir = (IsoDirectoryEntry)(pomocna.get(0));
            } else if (typ == 2) { //supplementary volume descriptor (Joliet)
                int tmpDlzkaBloku = volumeDescBuff.getShort(128) & 0xffff;
                if (tmpDlzkaBloku != 0)
                    dlzkaBloku = tmpDlzkaBloku;
                byte[] rootDirEntry = Arrays.copyOfRange(volumeDescArr, 156, 156 + 34);
                List<GenericDirectoryEntry> pomocna = interneLs(rootDirEntry);
                rootdir = (IsoDirectoryEntry)(pomocna.get(0));
                if (volumeDescBuff.get(88) == 0x25 && volumeDescBuff.get(89) == 0x2F) {
                    if (volumeDescBuff.get(90) == 0x40)
                        joliet = 1;
                    else if (volumeDescBuff.get(90) == 0x43)
                        joliet = 2;
                    else if (volumeDescBuff.get(90) == 0x45)
                        joliet = 3;
                }

            }
        }
    }
    public List<GenericDirectoryEntry> getDirectory(String cestaVstupna) throws IOException, FileNotFoundException {
        String tmp;
        String[] cesta;
        boolean najdenyPodadresar;

        tmp = cestaVstupna;

        if (tmp == "" || tmp.charAt(0) != '/')
            tmp = "/" + tmp;

        tmp = tmp.replaceAll("/+", "/");
        tmp = tmp.replaceAll("/$", "");


        //System.out.println("Cesta kde robim ls: "+tmp);
        cesta = tmp.split("/");

        cesta = Arrays.copyOfRange(cesta, 1, cesta.length);

        for (String komponent : cesta) {
            //System.out.println("Komponent je: [" + komponent + "]");
        }

        iso.seek(rootdir.umiestnenieSuboru);
        byte[] buffer = new byte[(int)rootdir.getSize()];
        iso.readFully(buffer);
        List<GenericDirectoryEntry> adresar = interneLs(buffer);

        for (String komponent : cesta) {
            najdenyPodadresar = false;

            for (GenericDirectoryEntry pol_gen : adresar) {
                IsoDirectoryEntry pol = (IsoDirectoryEntry)pol_gen;

                if (!pol.isDir())
                    continue;
                if (pol.interleaveUnitSize != 0 || pol.interleaveUnitSize != 0)
                    continue;
                if (pol.getName().equals(komponent)){
                    iso.seek(pol.umiestnenieSuboru);
                    buffer = new byte[(int)pol.getSize()];
                    iso.readFully(buffer);
                    adresar = interneLs(buffer);
                    najdenyPodadresar = true;
                    break;
                }
            }
            if (!najdenyPodadresar) {
                throw new FileNotFoundException(cestaVstupna);
            }
        }

        return adresar;

    }
    public boolean dumpFile(GenericDirectoryEntry polozka_gen, File f) {
        IsoDirectoryEntry polozka = (IsoDirectoryEntry)polozka_gen;

        OutputStream out = null;
        byte[] buff = new byte[dlzkaBloku];
        long zvysnaDlzka = polozka.getSize();
        long pozicia = polozka.umiestnenieSuboru;

        try {
            out = new BufferedOutputStream(new FileOutputStream(f));
        } catch (FileNotFoundException e){
            return false;
        }

        try {
            while (zvysnaDlzka >= dlzkaBloku) {
                iso.seek(pozicia);
                iso.readFully(buff);
                out.write(buff);

                pozicia = pozicia + dlzkaBloku;
                zvysnaDlzka = zvysnaDlzka - dlzkaBloku;
            }
            iso.seek(pozicia);
            iso.read(buff, 0, (int)zvysnaDlzka);
            out.write(buff, 0, (int) zvysnaDlzka);

            out.close();
        } catch (IOException e) {
            return false;
        }

        return true;

    }

    private List<GenericDirectoryEntry> interneLs(byte[] buffer) throws IOException {
        int i, zaciatokZaznamu = 0, dlzkaZaznamu;

        List<GenericDirectoryEntry> adresar = new ArrayList<GenericDirectoryEntry>();

        ByteBuffer bb = ByteBuffer.wrap(buffer);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        while (true) {

            if (zaciatokZaznamu >= buffer.length)
                break;

            dlzkaZaznamu = bb.get(zaciatokZaznamu) & 0xff;
            if (dlzkaZaznamu == 0)
                break;

            if (zaciatokZaznamu + dlzkaZaznamu > buffer.length)
                break;

            IsoDirectoryEntry polozka = new IsoDirectoryEntry(bb, buffer, joliet, dlzkaBloku, zaciatokZaznamu, dlzkaZaznamu);
            adresar.add(polozka);

            zaciatokZaznamu += dlzkaZaznamu;
        }
        return(adresar);
    }

}

