package m.ashutosh.texteditor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import m.ashutosh.toastertoast.Toaster;

import static m.ashutosh.texteditor.MainActivity.DEFAULT_LOCATION;

public class OpenFile extends AppCompatActivity {

    EditText editText;
    private Boolean noFileChosen = true;
    private File filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intent);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        editText = findViewById(R.id.workspace);

        if (ContextCompat.checkSelfPermission(OpenFile.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            Intent intent = getIntent();
            if(intent != null && intent.getType() != null ) {
                openIntentFile(intent.getData());
            } else {
                initActivity();
            }

        }else
            ActivityCompat.requestPermissions(OpenFile.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 105);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.intent, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.overwrite) {

            if (!noFileChosen) {

                String content = editText.getText().toString();
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                    fileOutputStream.write(content.getBytes());
                    fileOutputStream.close();

                    Toaster.makeToast(OpenFile.this,"File saved",Toaster.LENGTH_LONG,Toaster.SUCCESS);

                }catch (IOException e) {
                    Toaster.makeToast(OpenFile.this,""+e,Toaster.LENGTH_LONG,Toaster.ERROR);
                }
            }
        }
            return super.onOptionsItemSelected(item);
        }

    @Override
    protected void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data){
            if (requestCode == 106 && resultCode == Activity.RESULT_OK) {

                if (data != null) {
                    String path = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                    String header = path.substring(path.lastIndexOf("/") + 1);
                    String ext = path.substring(path.lastIndexOf(".") + 1);
                    getSupportActionBar().setTitle(header);
                    Toaster.makeToast(OpenFile.this,ext+" file",Toaster.LENGTH_SHORT,Toaster.DEFAULT);
                    loadFile(path);
                }
                super.onActivityResult(requestCode, resultCode, data);
            } else if (requestCode == 106 && resultCode == Activity.RESULT_CANCELED)
                this.finish();
        }

    @Override
    public void onRequestPermissionsResult ( int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
            if (requestCode == 105) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initActivity();
                } else {
                    Toaster.makeToast(OpenFile.this,"Permission Denied",Toaster.LENGTH_SHORT,Toaster.ERROR);
                    finish();
                }
            }
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    private void loadFile (String path) {

            String text;
            filePath = new File(path);

            if (filePath.exists()) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(filePath);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
                    StringBuffer stringBuffer = new StringBuffer();
                    while ((text = bufferedReader.readLine()) != null) {
                        stringBuffer.append(text + "\n");
                    }
                    editText.setText(stringBuffer.toString());
                    noFileChosen = false;
                } catch (IOException e) {
                    Toaster.makeToast(OpenFile.this,""+e,Toaster.LENGTH_LONG,Toaster.ERROR);
                }
            }else
                Toaster.makeToast(OpenFile.this,"Path not found:\n"+filePath.toString(),Toaster.LENGTH_LONG,Toaster.DEFAULT);

        }

        private void setTheme() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean darkTheme = sharedPreferences.getBoolean("theme", false);
        if (darkTheme) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void openIntentFile(Uri uri) {
        Toaster.makeToast(this, "" + new UriHelper().getPath(uri), Toaster.LENGTH_LONG, Toaster.DEFAULT);
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Notice")
                .setMessage("Due to new scoped storage policy of android 11, accessing files from outside of app folder is restricted.\nOnly files inside 'Text Editor' folder can be opened")
                .setPositiveButton("okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        initActivity();
                    }
                }).create().show();
    }

    private void initActivity() {

        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
            MaterialFilePicker materialFilePicker = new MaterialFilePicker();
            materialFilePicker.withActivity(OpenFile.this);
            materialFilePicker.withCloseMenu(false).withPath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + DEFAULT_LOCATION)
                    .withRootPath(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+DEFAULT_LOCATION).withHiddenFiles(false)
                    .withFilter(Pattern.compile(".*\\.(txt|enc|xml|properties|html|java|py|cpp|c|log|md|h|conf|config|cfg)$")).withFilterDirectories(false)
                    .withTitle("Choose File")
                    .withRequestCode(106).start();
            noFileChosen = true;
        }else {
            MaterialFilePicker materialFilePicker = new MaterialFilePicker();
            materialFilePicker.withActivity(OpenFile.this);
            materialFilePicker.withCloseMenu(false).withPath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + DEFAULT_LOCATION)
                    .withRootPath(Environment.getExternalStorageDirectory().getAbsolutePath()).withHiddenFiles(false)
                    .withFilter(Pattern.compile(".*\\.(txt|enc|xml|properties|html|java|py|cpp|c|log|md|h|conf|config|cfg)$")).withFilterDirectories(false)
                    .withTitle("Choose File")
                    .withRequestCode(106).start();
            noFileChosen = true;
        }
    }
}