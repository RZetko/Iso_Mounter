package info.ipsec.zdenko.isomounter;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.File;
import java.util.concurrent.TimeoutException;

/**
 * Created by Zdenko on 23.2.2014.
 */
public class MainActivity extends GenericFileExplorerActivity {

    boolean hasRoot = false;
    boolean hasBusybox = false;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        checkRootOnStartup();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String pathFromMount = data.getStringExtra("pathFromMount");
            //TODO start s pathFromMount
        }
    }

    @Override
    protected GenericFilesystemParser getFilesystemParserObject() {
        File f = new File("/");
        FsParser fp = new FsParser(f);
        return fp;
    }

    @Override
    protected String getInitialPath() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
            return Environment.getExternalStorageDirectory().getPath();

        return "/";
    }


    @Override
    protected void actionBackClick()
    {
        //exit();
    }

    @Override
    protected void actionFilenameClick(GenericDirectoryEntry entry)
    {
        String meno = entry.getName();

        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        String mimeType = myMime.getMimeTypeFromExtension(fileExt(meno).substring(1));

        File outputFile = new File(cesta, meno);

        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);

        intent.setDataAndType(Uri.fromFile(outputFile), mimeType);


        if (mimeType == null)
            mimeType = "application/octet-stream";

        if (mimeType == "application/x-iso9660-image") {
            if (!canMount()) {
                // len explore => nemusime sa ani pytat
                doExploreIso(cesta+"/"+meno);
                return;
            }

            String[] choices = {
              "Mount",
              "Explore"
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
                    if (which == 0)
                        doMountIso(path);
                    else
                        doExploreIso(path);
                }
            }.setPath(cesta+"/"+meno);

            ab.setItems(choices, o);
            AlertDialog a = ab.create();
            a.setCanceledOnTouchOutside(true);
            a.show();
        } else
            startActivity(intent);
    }

    private void doExploreIso(String path)
    {
        final Intent intentExploreImage = new Intent(this, IsoExploreActivity.class);
        intentExploreImage.putExtra("isoImagePath", path);
        startActivity(intentExploreImage);
    }

    private void doMountIso(String path)
    {
        final Intent intentMountImage = new Intent(this, MountDestinationActivity.class);
        intentMountImage.putExtra("isoImagePath", path);
        startActivityForResult(intentMountImage, 1);
    }

    private void checkRootOnStartup()
    {
        // ak nemame roota
        if (!RootTools.isAccessGiven()) {
            Context context = getApplicationContext();
            CharSequence text = "No root. Functionality will be limited";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        hasRoot = true;

        if (RootTools.isBusyboxAvailable()) {
            hasBusybox = true;
            return;
        }

        AlertDialog a = new AlertDialog.Builder(this).create();
        a.setMessage("Busybox was not found. It is essential to make this program work. You will be now taken to play store to download it.");
        a.setCanceledOnTouchOutside(true);
        a.setButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                try {
                    RootTools.offerBusyBox(MainActivity.this);
                } catch(ActivityNotFoundException e) {

                }
            }

        });
        a.show();


    }
    private boolean canMount()
    {
        if (hasBusybox)
            return true;
        if (!hasRoot)
            return false;
        // busybox mohli nainstalovat pocas behu tohto programu.
        hasBusybox = RootTools.isBusyboxAvailable();
        return hasBusybox;
    }

    // toto su polozky z kontextoveho menu
    @Override
    String[] getLongClickMenuItems()
    {
        // toto implementuje subclass-a
        final String[] polozky = { "Delete", "Rename", "Unmount"};
        return polozky;
    }

    @Override
    protected void actionFilenameLongClickEntry(final GenericDirectoryEntry entry, final String which) {
        super.actionFilenameLongClickEntry(entry, which);
        if (which.equals("Rename")) { //rename
            if (entry.getName().equals("..")) {
                return;
            } else {
                LayoutInflater li = LayoutInflater.from(MainActivity.this);
                View nameOfFile = li.inflate(R.layout.nameoffile, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setView(nameOfFile);

                final EditText userInput = (EditText) nameOfFile.findViewById(R.id.editTextDialogUserInput);
                userInput.setText(entry.getName());

                // Nastavi dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        // Ziska input a urobi s nim nieco
                                        File originalFile = new File (cesta, entry.getName());
                                        File changedFile = new File(cesta, userInput.getText().toString());
                                        if (!changedFile.exists()) {
                                            originalFile.renameTo(changedFile);
                                            screenRows.removeAllViews();
                                            ls();
                                        }
                                        else
                                            return;
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });

                // Vytovri alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                alertDialogBuilder.show();
            }
        } else if (which.equals("Delete")) {
            if (entry.getName().equals("..")) {
                return;
            } else { //delete
                new File(cesta + entry.getName()).delete();
                screenRows.removeAllViews();
                ls();
            }
        } else if (which.equals("Unmount")) {
            AlertDialog a = new AlertDialog.Builder(this).create();
            if (Util.cmd("busybox umount "+ cesta + entry.getName())== 0) {
                a.setMessage("Successfully unmounted");
            } else {
                a.setMessage("Unmount error");
            }
            a.setCanceledOnTouchOutside(true);
            a.show();
        }
    }
}
