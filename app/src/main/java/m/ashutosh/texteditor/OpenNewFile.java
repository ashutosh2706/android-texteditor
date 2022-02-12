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

public class OpenNewFile extends AppCompatActivity {

    EditText editText;
    private Boolean noFileChosen = true;
    private String path, fPath;
    private File filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intent);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        editText = findViewById(R.id.workspace);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        fPath = preferences.getString("dirName", DEFAULT_LOCATION);

        Intent viewIntent = getIntent();
        String action = viewIntent.getAction();
        String type = viewIntent.getType();

        if (action != null) {

            if (action.equals(Intent.ACTION_VIEW)) {

                if (type != null) {

                    if (type.startsWith("text/")) {
                        Uri data = viewIntent.getData();

                        if (data != null) {
                            String a = data.getPath();
                            String title = a.substring(a.lastIndexOf("/") + 1);
                            getSupportActionBar().setTitle(title);

                            if (a.contains("root")) {
                                path = a.substring(5);
                                loadFile();
                            }else if (a.contains("home")) {
                                String c = a.substring(a.indexOf(":") + 1);
                                path = "Documents/" + c;
                                loadFile();
                            }else {
                                path = a;
                                loadFile();
                            }

                        }else {
                            Toaster.makeToast(OpenNewFile.this,"Data error",Toaster.LENGTH_LONG,Toaster.ERROR);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                this.finishAndRemoveTask();
                            }else
                                this.finish();
                        }
                    }else {
                        Toaster.makeToast(OpenNewFile.this,"Invalid File",Toaster.LENGTH_SHORT,Toaster.DEFAULT);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            this.finishAndRemoveTask();
                        }else
                            this.finish();
                    }
                }else
                    init();

            }else {
                Toaster.makeToast(OpenNewFile.this,"Data error",Toaster.LENGTH_SHORT,Toaster.ERROR);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    this.finishAndRemoveTask();
                }else
                    this.finish();
            }
        } else
            init();
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

                    Toaster.makeToast(OpenNewFile.this,"File saved",Toaster.LENGTH_LONG,Toaster.SUCCESS);

                }catch (IOException e) {
                    Toaster.makeToast(OpenNewFile.this,""+e,Toaster.LENGTH_LONG,Toaster.ERROR);
                }
            }
        }
            return super.onOptionsItemSelected(item);
        }

    @Override
    protected void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data){
            if (requestCode == 106 && resultCode == Activity.RESULT_OK) {

                if (data != null) {
                    path = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                    String header = path.substring(path.lastIndexOf("/") + 1);
                    String ext = path.substring(path.lastIndexOf(".") + 1);
                    getSupportActionBar().setTitle(header);
                    Toaster.makeToast(OpenNewFile.this,ext+" file",Toaster.LENGTH_SHORT,Toaster.DEFAULT);
                    loadFile();
                }
                super.onActivityResult(requestCode, resultCode, data);
            } else if (requestCode == 106 && resultCode == Activity.RESULT_CANCELED)
                this.finish();
        }

    @Override
    public void onRequestPermissionsResult ( int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
            if (requestCode == 105) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    MaterialFilePicker materialFilePicker = new MaterialFilePicker();
                    materialFilePicker.withActivity(OpenNewFile.this);
                    materialFilePicker.withCloseMenu(true).withPath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + fPath)
                            .withRootPath(Environment.getExternalStorageDirectory().getAbsolutePath()).withHiddenFiles(false)
                            .withFilter(Pattern.compile(".*\\.(txt|enc|xml|properties|html|java|py|cpp|c|log|md|h|conf|config|cfg)$")).withFilterDirectories(false)
                            .withTitle("Choose File")
                            .withRequestCode(106).start();
                    noFileChosen = true;

                } else {
                    Toaster.makeToast(OpenNewFile.this,"Permission Denied",Toaster.LENGTH_SHORT,Toaster.ERROR);
                    finish();
                }
            }
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    private void loadFile () {

            String text;
            if (path.contains("emulated"))
                filePath = new File(path);
            else
                filePath = new File(Environment.getExternalStorageDirectory(), path);

            if (filePath.exists()) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(filePath);
                    InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    StringBuffer stringBuffer = new StringBuffer();
                    while ((text = bufferedReader.readLine()) != null) {
                        stringBuffer.append(text + "\n");
                    }
                    editText.setText(stringBuffer.toString());
                    noFileChosen = false;
                } catch (IOException e) {
                    Toaster.makeToast(OpenNewFile.this,""+e,Toaster.LENGTH_LONG,Toaster.ERROR);
                }
            }else
                Toaster.makeToast(OpenNewFile.this,"Path not found:\n"+filePath.toString(),Toaster.LENGTH_LONG,Toaster.DEFAULT);

        }

    private void init () {

            if (ContextCompat.checkSelfPermission(OpenNewFile.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                MaterialFilePicker materialFilePicker = new MaterialFilePicker();
                materialFilePicker.withActivity(OpenNewFile.this);
                materialFilePicker.withCloseMenu(true).withPath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + fPath)
                        .withRootPath(Environment.getExternalStorageDirectory().getAbsolutePath()).withHiddenFiles(false)
                        .withFilter(Pattern.compile(".*\\.(txt|enc|xml|properties|html|java|py|cpp|c|log|md|h|conf|config|cfg)$")).withFilterDirectories(false)
                        .withTitle("Choose File")
                        .withRequestCode(106).start();
                noFileChosen = true;

            }else
                ActivityCompat.requestPermissions(OpenNewFile.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 105);
        }

    private void setTheme () {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean darkTheme = sharedPreferences.getBoolean("theme", false);
        if (darkTheme) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}