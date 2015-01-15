package info.ipsec.zdenko.isomounter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class IsoExploreActivity extends GenericFileExplorerActivity {

    String isoImagePath;

    @Override
    protected GenericFilesystemParser getFilesystemParserObject() {
        IsoParser iso;

        Intent mojIntent = getIntent();
        isoImagePath = mojIntent.getStringExtra("isoImagePath");
        try {
            iso = new IsoParser(isoImagePath);
        } catch (IOException e) {
            return null;
        }
        return (GenericFilesystemParser)iso;
    }

    @Override
    protected String getInitialPath() {
        Intent mojIntent = getIntent();

        cesta = mojIntent.getStringExtra("path");
        if (cesta == null)
            cesta = "/";

           return cesta;
    }

    @Override
    protected void actionFilenameClick(final GenericDirectoryEntry entry)
    {
        final String meno = entry.getName();

        String[] choices = {
                "Open",
                "Export"
        };

        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        DialogInterface.OnClickListener o;

        o = new DialogInterface.OnClickListener() {
            public String path;
            private DialogInterface.OnClickListener setPath(String s)
            {
                this.path = s;
                return this;
            }
            public void onClick(DialogInterface dialog, int which) {
                // The 'which' argument contains the index position
                // of the selected item
                if (which == 0) {
                    //doFileOpen();

                    File outputDir = IsoExploreActivity.this.getCacheDir();
                    File outputFile = new File(outputDir, meno);
                    filesystemParser.dumpFile(entry, outputFile);

                    MimeTypeMap myMime = MimeTypeMap.getSingleton();
                    String mimeType = myMime.getMimeTypeFromExtension(fileExt(meno).substring(1));

                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);

                    intent.setDataAndType(Uri.fromFile(outputFile), mimeType);
                    startActivity(intent);
                }
                else {
                    doFileExport(path, entry);
                }
            }
        }.setPath(cesta+"/"+meno);


        ab.setItems(choices, o);
        AlertDialog a = ab.create();
        a.setCanceledOnTouchOutside(true);
        a.show();

            // TODO: zmazat subor ked sa zavolana aktivita vrati nazad..
    }

    @Override
    protected void actionIconClick(GenericDirectoryEntry entry, View view)
    {
        //nastavovanie farby riadku a oznacovanie suborov
    }

    private void doFileOpen()
    {

    }

    private void doFileExport(String path, GenericDirectoryEntry entry)
    {
        Bundle b = new Bundle();
        b.putSerializable("pathInIso", (IsoDirectoryEntry)entry);


        final Intent intentExportFile = new Intent(this, ExportDestinationActivity.class);
        intentExportFile.putExtras(b);
        intentExportFile.putExtra("isoImagePath", isoImagePath);
        startActivity(intentExportFile);

    }


}