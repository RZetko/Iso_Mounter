package info.ipsec.zdenko.isomounter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Created by Zdenko on 27.2.2014.
 */
public class ExportDestinationActivity extends GenericFileExplorerActivity {

    private IsoParser iso;
    private IsoDirectoryEntry pathInIso;

    protected void onCreate(Bundle savedInstanceState)
    {
        String isoImagePath;

        super.onCreate(savedInstanceState);
        Intent mojIntent = getIntent();

        isoImagePath = getIntent().getStringExtra("isoImagePath");
        pathInIso = (IsoDirectoryEntry) getIntent().getSerializableExtra("pathInIso");

        try {
            iso = new IsoParser(isoImagePath);
        } catch (IOException e) {

        }
    }

    @Override
    protected GenericFilesystemParser getFilesystemParserObject()
    {
        File f = new File("/");
        FsParser fp = new FsParser(f);
        return fp;
    }

    @Override
    protected void actionFilenameClick(GenericDirectoryEntry entry)
    {

    }

    @Override
    protected void actionIconClick(GenericDirectoryEntry entry, View view)
    {
        String path = cesta;
        String meno = entry.getName();

        File outputDir = new File(path+meno);
        File outputFile = new File(outputDir, pathInIso.getName());

        iso.dumpFile(pathInIso, outputFile);
    }
}
