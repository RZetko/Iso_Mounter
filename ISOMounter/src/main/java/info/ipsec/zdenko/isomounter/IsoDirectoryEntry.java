package info.ipsec.zdenko.isomounter;

/**
 * Created by Zdenko on 11/10/13.
 */
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Calendar;

class IsoDirectoryEntry extends GenericDirectoryEntry {
    private int flags;

    // tieto 3 sme z lenivosti neprerobili na private a neurobili sme k nim getXXX()
    public int interleaveUnitSize, interleaveGapSize;
    public long umiestnenieSuboru;

    IsoDirectoryEntry(ByteBuffer bb, byte[] buffer, int joliet, int dlzkaBloku, int zaciatokZaznamu, int dlzkaZaznamu) throws UnsupportedEncodingException {
        int dlzkaNazvu;

        creationDateTime.set(1900 + (bb.get(zaciatokZaznamu + 18) & 0xff), bb.get(zaciatokZaznamu + 19) & 0xff, bb.get(zaciatokZaznamu + 20) & 0xff, bb.get(zaciatokZaznamu + 21) & 0xff, bb.get(zaciatokZaznamu + 22) & 0xff, bb.get(zaciatokZaznamu + 23) & 0xff);

        dlzkaNazvu = bb.get(zaciatokZaznamu + 32) & 0xff;
        byte[] nazovSuboru = Arrays.copyOfRange(buffer, zaciatokZaznamu + 33, zaciatokZaznamu + 33 + dlzkaNazvu);

        if (dlzkaNazvu == 1 && nazovSuboru[0] == 0x00)
            name = ".";
        else if (dlzkaNazvu == 1 && nazovSuboru[0] == 0x01)
            name = "..";
        else if (joliet == 0)
            name = new String(nazovSuboru, "ASCII");
        else
            name = new String(nazovSuboru, "UTF-16BE");

        size = bb.getInt(zaciatokZaznamu + 10) & 0xffffffff;
        umiestnenieSuboru = bb.getInt(zaciatokZaznamu + 2) & 0xffffffff;
        umiestnenieSuboru = umiestnenieSuboru * dlzkaBloku;

        flags = bb.get(zaciatokZaznamu + 25);

        interleaveUnitSize = bb.get(zaciatokZaznamu + 26);
        interleaveGapSize = bb.get(zaciatokZaznamu + 27);
    }

    public boolean isDir() {
        return ((flags & 0x02) != 0);
    }

    public boolean isFile() {
        return ((flags & 0x02) == 0);
    }
}

