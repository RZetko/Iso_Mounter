package info.ipsec.zdenko.isomounter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class GenericFileExplorerActivity extends Activity implements View.OnClickListener, View.OnLongClickListener {

    LinearLayout screenRows, riadok;
    String cesta;
    ScrollView tabulka;

    GenericFilesystemParser filesystemParser;
    TextView nazovSuboru, datumVytvorenia, pathView;
    ImageView ikona;

    private List<GenericDirectoryEntry> polozky;

    private static long mBackPressed;
    private static final int TIME_INTERVAL = 2000;

    protected GenericFilesystemParser getFilesystemParserObject()
    {
        return null;
    }

    protected String getInitialPath() {
        return "/";
    }

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iso_explore);
        setupActionBar();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        screenRows = (LinearLayout)findViewById(R.id.screenRows);



        /* velkost textviewu na cestu */
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        int actionBarHeight = 0;
        TypedValue typeValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.actionBarSize, typeValue, true);
        actionBarHeight = TypedValue.complexToDimensionPixelSize(typeValue.data,getResources().getDisplayMetrics());
        pathView = (TextView)findViewById(R.id.pathView);
        pathView.setBackgroundDrawable(getResources().getDrawable(R.drawable.ciara));
        pathView.setHeight(actionBarHeight);
        pathView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        pathView.setTextSize(24);
        pathView.setPadding(20, 0, 0, 0);

        /* o tolko neskor je posunuty (a zmenseny) scrollview na obsah adresara */
        tabulka = (ScrollView)findViewById(R.id.tabulka);
        //tabulka.setOverScrollMode(View.OVER_SCROLL_NEVER);
        tabulka.setPadding(0,actionBarHeight,0,0);



        filesystemParser = getFilesystemParserObject();

        cesta = getInitialPath();
        if (cesta.isEmpty())
            cesta = "/";
        else if (!cesta.substring(cesta.length() - 1).equals("/")) {
                cesta = cesta + "/";
        }

        ls();
    }

    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    //  pomocna trieda kvoli kliknutiu na ikonu, pouziva sa v ls()
    class OnClickListenerWithData implements View.OnClickListener {
        GenericDirectoryEntry entry;

        public void setEntry(GenericDirectoryEntry entry)
        {
            this.entry = entry;
        }
        @Override
        public void onClick(View view) {
            GenericFileExplorerActivity.this.actionIconClick(entry, view);
        }
    };

    public void ls()
    {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();

        try {
            polozky = filesystemParser.getDirectory(cesta);
            if (polozky == null)
                return;
            pathView.setText(cesta);

            Resources res = getResources();

            Drawable draw0 = res.getDrawable( R.drawable.directory_up);
            Drawable draw1 = res.getDrawable( R.drawable.directory_icon);
            Drawable draw2 = res.getDrawable( R.drawable.mime_application_octet_stream );

            for (final GenericDirectoryEntry polozka : polozky) {

                if (polozka.getName().equals("."))
                    continue;

                LinearLayout riadok = new LinearLayout(this);
                riadok.setOrientation(0);
                riadok.setPadding(2,2,0,0);

                String meno = polozka.getName();

                ikona = new ImageView(this);
                ikona.setScaleType(ImageView.ScaleType.FIT_CENTER);
                LinearLayout.LayoutParams vp =
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                ikona.setLayoutParams(vp);
                ikona.getLayoutParams().width = 72;
                ikona.getLayoutParams().height = 72;
                ikona.setPadding(10, 0, 0, 0);

                if (polozka.isDir() && (  meno.equals(".") || meno.equals("..")))
                    ikona.setImageDrawable(draw0);
                else if (polozka.isDir())
                    ikona.setImageDrawable(draw1);
                else if (polozka.isFile()) {
                    MimeTypeMap myMime = MimeTypeMap.getSingleton();
                    String extensionMeno = fileExt(meno);
                    String mimeType;

                    if (extensionMeno == null) {
                        mimeType = "mime_application_octet_stream";
                    } else {
                        mimeType = myMime.getMimeTypeFromExtension(extensionMeno.substring(1));

                        if (mimeType == null) {
                            mimeType = "mime_application_octet_stream";
                        } else {
                            mimeType = "mime_" + mimeType.replaceAll("[^0-9a-zA-Z]", "_");
                        }
                    }

                    int resId = res.getIdentifier(mimeType, "drawable", getPackageName());
                    if (resId == 0)
                        ikona.setImageDrawable(draw2); // defaultna ikona
                    else
                        ikona.setImageResource(resId);
                }

                OnClickListenerWithData l = new OnClickListenerWithData();
                l.setEntry(polozka);
                ikona.setOnClickListener(l);

                nazovSuboru = new TextView(this);
                nazovSuboru.setText(polozka.getName());
                nazovSuboru.setHeight(nazovSuboru.getLineHeight() * 4);
                nazovSuboru.setWidth(displayMetrics.widthPixels / 3);
                nazovSuboru.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL | Gravity.LEFT);
                nazovSuboru.setPadding(20,0,0,0);

                datumVytvorenia = new TextView(this);

                SimpleDateFormat datumString = new SimpleDateFormat("dd-MM-yyyy HH:mm");

                String velkostString = "";

                if (polozka.isFile())
                    velkostString = niceSize(polozka.getSize());

                datumVytvorenia.setText(datumString.format(polozka.getCreationDateTime().getTime()) + "\n" + velkostString);
                datumVytvorenia.setPadding(20,0,0,0);
                datumVytvorenia.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL | Gravity.LEFT);

                riadok.addView(ikona);
                riadok.addView(datumVytvorenia);
                riadok.addView(nazovSuboru);
                riadok.setMinimumWidth(displayMetrics.widthPixels);

                riadok.setBackgroundDrawable(getResources().getDrawable(R.drawable.ciara));
                riadok.setBackgroundDrawable(getResources().getDrawable(R.drawable.click));

                screenRows.addView(riadok);

                riadok.setOnClickListener(this);
                riadok.setOnLongClickListener(this);


            }
        } catch (IOException e) {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.iso_explore, menu);
        return true;
    }


    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar() {

        getActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_refresh:
                screenRows.removeAllViews();
                ls();
                return true;
            case R.id.action_newFile:
                final CharSequence[] items={"File","Folder"};

                AlertDialog.Builder builder = new AlertDialog.Builder(GenericFileExplorerActivity.this);
                builder.setTitle("Create new").setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (items[which] == "Folder") {
                            LayoutInflater li = LayoutInflater.from(GenericFileExplorerActivity.this);
                            View nameOfFile = li.inflate(R.layout.nameoffile, null);

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GenericFileExplorerActivity.this);
                            alertDialogBuilder.setView(nameOfFile);

                            final EditText userInput = (EditText) nameOfFile.findViewById(R.id.editTextDialogUserInput);

                            // Nastavi dialog message
                            alertDialogBuilder
                                    .setCancelable(false)
                                    .setPositiveButton("OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,int id) {
                                                    // Ziska input a urobi s nim nieco
                                                    File folder = new File(cesta + userInput.getText().toString());
                                                    if (!folder.exists()) {
                                                        folder.mkdir();
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
                        } else {
                            LayoutInflater li = LayoutInflater.from(GenericFileExplorerActivity.this);
                            View nameOfFile = li.inflate(R.layout.nameoffile, null);

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GenericFileExplorerActivity.this);
                            alertDialogBuilder.setView(nameOfFile);

                            final EditText userInput = (EditText) nameOfFile.findViewById(R.id.editTextDialogUserInput);

                            // Nastavi dialog message
                            alertDialogBuilder
                                    .setCancelable(false)
                                    .setPositiveButton("OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    // Ziska input a urobi s nim nieco
                                                    File file = new File(cesta + userInput.getText().toString());
                                                    if (!file.exists()) {
                                                        try {
                                                            file.createNewFile();
                                                        } catch (IOException e) {
                                                        }
                                                        screenRows.removeAllViews();
                                                        ls();
                                                    } else
                                                        return;
                                                }
                                            }
                                    )
                                    .setNegativeButton("Cancel",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            }
                                    );

                            // Vytovri alert dialog
                            AlertDialog alertDialog = alertDialogBuilder.create();

                            alertDialogBuilder.show();
                        }
                    }
                });
                builder.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_iso_explore, container, false);
            return rootView;
        }
    }

    protected String fileExt(String url) {
        if (url.indexOf("?")>-1) {
            url = url.substring(0,url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf(".") );
            if (ext.indexOf("%")>-1) {
                ext = ext.substring(0,ext.indexOf("%"));
            }
            if (ext.indexOf("/")>-1) {
                ext = ext.substring(0,ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }

    public void onClick(View v) {

        TextView napis = (TextView)(((LinearLayout)v).getChildAt(2));
        String meno = napis.getText().toString();
        GenericDirectoryEntry najdena = null;

        if (meno.equals("."))
            return;
        if (meno.equals("..")) {
            if (cesta.equals("/"))
                return;
            String tmp = cesta.substring(0, cesta.length() - 1);
            cesta = cesta.substring(0, tmp.lastIndexOf("/") + 1);

            tabulka.pageScroll(View.FOCUS_UP);
            tabulka.fullScroll(View.FOCUS_UP);

            screenRows.removeAllViews();
            ls();

            return;
        }

        for (GenericDirectoryEntry polozka : polozky) {
            if (polozka.getName() == meno) {
                najdena = polozka;
                break;
            }
        }

        if (najdena == null)
            return; // toto by nemalo nastat...

        if (najdena.isDir()) {
            tabulka.pageScroll(View.FOCUS_UP);
            tabulka.fullScroll(View.FOCUS_UP);
            screenRows.removeAllViews();
            cesta = cesta + meno + "/";
            ls();
        } else {
            actionFilenameClick(najdena);
        }

    }

    // ked niekto klikne na meno suboru
    protected void actionFilenameClick(GenericDirectoryEntry entry)
    {
        // toto implementuje subclass-a
    }

    // ked niekto klikne na ikonu vlavo od suboru
    protected void actionIconClick(GenericDirectoryEntry entry, View view)
    {
        // toto implementuje subclass-a
        //riadok.setBackgroundDrawable(getResources().getDrawable(R.drawable.selection));
    }

    protected void actionBackClick()
    {
        // toto implementuje subclass-a
    }

    public boolean onLongClick(View v)
    {
        TextView napis = (TextView)(((LinearLayout)v).getChildAt(2));
        String meno = napis.getText().toString();
        GenericDirectoryEntry najdena = null;

        if (meno.equals(".") || meno.equals(".."))
            return true;

        for (GenericDirectoryEntry polozka : polozky) {
            if (polozka.getName() == meno) {
                najdena = polozka;
                break;
            }
        }

        if (najdena == null)
            return true;

        actionFilenameLongClick(najdena);
        return true;
    }

    // ked niekto klikne na meno suboru, objavi sa kontextove menu
    protected void actionFilenameLongClick(final GenericDirectoryEntry entry)
    {
        // toto moze subclass-a reimplementovat, ak chce ine spravanie ako kontextove menu
        final String[] items = getLongClickMenuItems();

        final GenericDirectoryEntry polozka = entry;

        AlertDialog.Builder builder = new AlertDialog.Builder(GenericFileExplorerActivity.this);

        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                GenericFileExplorerActivity.this.actionFilenameLongClickEntry(entry, items[which]);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    // toto su polozky z kontextoveho menu
    String[] getLongClickMenuItems()
    {
        // toto implementuje subclass-a
        final String[] polozky = {"(default)"};
        return polozky;
    }


    // ked niekto klikne na polozku v kontextovom menu...
    protected void actionFilenameLongClickEntry(final GenericDirectoryEntry entry, final String which)
    {
        if (which.equals("Choose directory")) {

        }
    }

    @Override
    public void onBackPressed() {
        if (cesta.equals("/")) {
            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis())
            {
                super.onBackPressed();
                return;
            }
            else { Toast.makeText(getBaseContext(), "Tap back button in order to exit", Toast.LENGTH_SHORT).show(); }

            mBackPressed = System.currentTimeMillis();
        } else {
            String tmp = cesta.substring(0, cesta.length() - 1);
            cesta = cesta.substring(0, tmp.lastIndexOf("/") + 1);

            screenRows.removeAllViews();
            ls();
        }
    }

    public String niceSize(long velkost){

        double n = velkost;
        DecimalFormat dec = new DecimalFormat("0.00");

        if (n < 1024.)
             return Long.toString(velkost).concat("B");

        n = n / 1024.;
        if (n < 1024.)
            return dec.format(n).concat("kB");

        n = n / 1024.;
        if (n < 1024.)
            return dec.format(n).concat("MB");

        n = n / 1024.;
        if (n < 1024.)
            return dec.format(n).concat("GB");

        n = n / 1024.;
        return dec.format(n).concat("TB");

    }
}