package info.ipsec.zdenko.isomounter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import java.io.File;

/**
 * Created by Zdenko on 24.2.2014.
 */
public class MountDestinationActivity extends GenericFileExplorerActivity {

    String isoImagePath;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent mojIntent = getIntent();
        isoImagePath = mojIntent.getStringExtra("isoImagePath");
    }

    @Override
    protected GenericFilesystemParser getFilesystemParserObject() {
        File f = new File("/");
        FsParser fp = new FsParser(f);
        return fp;
    }


    protected void actionFilenameClick(GenericDirectoryEntry entry)
    {

    }

    @Override
    protected void actionIconClick(GenericDirectoryEntry entry, View view)
    {

    }

    // toto su polozky z kontextoveho menu
    @Override
    String[] getLongClickMenuItems()
    {
        // toto implementuje subclass-a
        final String[] polozky = {"Use this directory as a mount destination"};
        return polozky;
    }

    // ked niekto klikne na meno suboru
    @Override
    protected void actionFilenameLongClickEntry(final GenericDirectoryEntry entry, final String which) {
        if (which.equals("Use this directory as a mount destination")) { //choose directory
            // Toast.makeText(getApplicationContext(), which, Toast.LENGTH_SHORT).show();


            final String path = cesta + entry.getName();

            Util.cmd("busybox modprobe iso9660");

            AlertDialog.Builder a = new AlertDialog.Builder(MountDestinationActivity.this);
            a.setMessage("Mount [" + isoImagePath + "] to [" + path + "] " + "Are you sure?");
            a.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String loop = "";
                    int i;
                    for (i = 40; i < 100; i++) {
                        loop = "/dev/block/loop"+Integer.toString(i);

                        Util.cmd("busybox mknod "+loop+" b 7 "+Integer.toString(i));
                        Util.cmd("busybox losetup -d "+loop);

                        if (Util.cmd("ls "+loop) != 0)
                            continue; // loop device z nejakeho dovodu neexistuje
                        if (Util.cmd("busybox losetup "+loop) == 0)
                            continue; // loop device uz je pouzivany :(
                        if (Util.cmd("busybox losetup "+loop+" "+isoImagePath) != 0)
                            continue; // nezbehlo pre nieco losetup
                        break;
                    }

                    int retval = -1;

                    if (i < 100) {
                        // uspech, mozme mountovat
                        retval = Util.cmd("busybox mount -o ro -t iso9660 "+loop+" "+path);
                    }
                    if (retval == 0) {

                        Context context = getApplicationContext();
                        CharSequence text = "ISO Image was successfully mounted to " + path;
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();

                        Intent intent = new Intent();
                        intent.putExtra("pathFromMount", path);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            });
            a.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Context context = getApplicationContext();
                    CharSequence text = "Mounting of ISO Image Failed.";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    return;
                }
            });
            a.show();
        }
    }
}
