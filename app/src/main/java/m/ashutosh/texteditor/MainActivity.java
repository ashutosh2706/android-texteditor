package m.ashutosh.texteditor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import m.ashutosh.toastertoast.Toaster;

public class MainActivity extends AppCompatActivity {

    private EditText editText;
    private long time;
    private Boolean lastOpened = false;
    private static final String SHARED_PREFS = "preferences";
    private static final String SHARED_FILENAME = "filename";
    public static final String DEFAULT_LOCATION = "Documents/Text Editor";
    private final int CHOOSE_FILE_CODE_DEC = 191;
    private final int CHOOSE_FILE_CODE_ENC = 181;
    private Toast backToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(MainActivity.this,new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},101);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if(id==R.id.save) {
            saveMeth();
        }else if(id==R.id.open_last)
            openLast();
        else if(id==R.id.open_new)
            startActivity(new Intent(MainActivity.this, OpenFile.class));
        else if(id==R.id.app_settings)
            startActivity(new Intent(MainActivity.this,SettingsActivity.class));
        else if(id==R.id.encrypt) {
            MaterialFilePicker materialFilePicker = new MaterialFilePicker();
            materialFilePicker.withActivity(MainActivity.this);
            materialFilePicker.withCloseMenu(false).withPath(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+DEFAULT_LOCATION)
                    .withRootPath(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+DEFAULT_LOCATION).withHiddenFiles(false)
                    .withFilter(Pattern.compile(".*\\.(txt)$")).withFilterDirectories(false)
                    .withTitle("Choose File")
                    .withRequestCode(CHOOSE_FILE_CODE_ENC).start();
        }
        else if(id == R.id.decrypt) {
            MaterialFilePicker materialFilePicker = new MaterialFilePicker();
            materialFilePicker.withActivity(MainActivity.this);
            materialFilePicker.withCloseMenu(false).withPath(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+DEFAULT_LOCATION)
                    .withRootPath(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+DEFAULT_LOCATION).withHiddenFiles(false)
                    .withFilter(Pattern.compile(".*\\.(enc)$")).withFilterDirectories(false)
                    .withTitle("Choose File")
                    .withRequestCode(CHOOSE_FILE_CODE_DEC).start();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toaster.makeToast(MainActivity.this,"Permission Denied",Toaster.LENGTH_SHORT,Toaster.ERROR);
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == CHOOSE_FILE_CODE_DEC && resultCode == Activity.RESULT_OK){
            if (data != null) {
                decryptText(data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH));
            }
            super.onActivityResult(requestCode, resultCode, data);
        }else if(requestCode == CHOOSE_FILE_CODE_ENC && resultCode == Activity.RESULT_OK){
            if(data!=null) {
                encryptText(data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH));
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (time + 2000 > System.currentTimeMillis()) {
            backToast.cancel();
            super.onBackPressed();
            return;
        } else {
            backToast = Toast.makeText(getBaseContext(),"Press back again to exit",Toast.LENGTH_SHORT);
            backToast.show();
        }
        time = System.currentTimeMillis();
    }

    private void saveFile(File output, String tempFile) {

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(output);
            fileOutputStream.write(editText.getText().toString().getBytes());
            fileOutputStream.close();

            SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE).edit();
            editor.putString(SHARED_FILENAME,tempFile + ".txt");
            editor.apply();

            Toaster.makeToast(MainActivity.this,"File Saved",Toaster.LENGTH_LONG,Toaster.SUCCESS);

        } catch (Exception e) {
            Toaster.makeToast(MainActivity.this,""+e,Toaster.LENGTH_SHORT,Toaster.ERROR);
        }

    }

    private void saveMeth(){

        if(lastOpened){

            SharedPreferences preferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
            String filename = preferences.getString(SHARED_FILENAME,"$&$");
            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + DEFAULT_LOCATION);
            File file = new File(dir,filename);

            if(file.exists()){

                String content = editText.getText().toString();
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(content.getBytes());
                    fileOutputStream.close();
                    lastOpened = false;

                    Toaster.makeToast(MainActivity.this,"File Saved",Toaster.LENGTH_LONG,Toaster.SUCCESS);

                }catch (Exception e) {
                    Toaster.makeToast(MainActivity.this,""+e,Toaster.LENGTH_SHORT,Toaster.ERROR);
                }
            }else
                Toaster.makeToast(MainActivity.this,"File not found",Toaster.LENGTH_SHORT,Toaster.ERROR);
        }else {

            if (!TextUtils.isEmpty(editText.getText().toString().trim())) {

                View v = getLayoutInflater().inflate(R.layout.main_dialog, null);
                EditText fileName = v.findViewById(R.id.dialog_filename);

                new AlertDialog.Builder(this)
                        .setTitle("Enter Filename")
                        .setView(v)
                        .setCancelable(false)
                        .setPositiveButton("ok", (dialog, which) -> {

                            String tempFilename0 = fileName.getText().toString();
                            if (!(TextUtils.isEmpty(fileName.getText().toString().trim()))) {

                                File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + DEFAULT_LOCATION);
                                if (!dir.exists()) {
                                    dir.mkdirs();
                                }
                                File output = new File(dir, tempFilename0 + ".txt");
                                saveFile(output, tempFilename0);
                            }else
                                Toaster.makeToast(MainActivity.this,"Enter Filename",Toaster.LENGTH_SHORT,Toaster.DEFAULT);

                        }).setNegativeButton("cancel", (dialog, which) -> dialog.dismiss()).create().show();
            }
        }
    }

    private void openLast() {

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        String fileName = sharedPreferences.getString(SHARED_FILENAME,"$&$");
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + DEFAULT_LOCATION);
        File file = new File(dir,fileName);

        if (!file.exists())
            Toaster.makeToast(MainActivity.this,"File not found",Toaster.LENGTH_SHORT,Toaster.ERROR);
        else {
            String text;
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
                StringBuffer stringBuffer = new StringBuffer();
                while ((text = bufferedReader.readLine()) != null) {
                    stringBuffer.append(text + "\n");
                }
                editText.setText(stringBuffer.toString());
                lastOpened = true;
            } catch (Exception e) {
                Toaster.makeToast(MainActivity.this,""+e,Toaster.LENGTH_SHORT,Toaster.ERROR);
            }
        }
    }

    private void encryptText(String path) {
        View view = getLayoutInflater().inflate(R.layout.enc_dialog,null);
        EditText passwordField = view.findViewById(R.id.dialog_enc);

        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Enter Password")
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("ok", (dialog, which) -> {

                    if(TextUtils.isEmpty(passwordField.getText().toString()))
                        Toaster.makeToast(MainActivity.this,"Enter Password",Toaster.LENGTH_SHORT,Toaster.DEFAULT);
                    else
                        new EncryptUtil(MainActivity.this).encryptFile(path,passwordField.getText().toString());

                }).setNegativeButton("cancel", (dialog, which) -> dialog.dismiss()).create().show();

    }

    private void decryptText(String path){

        View view = getLayoutInflater().inflate(R.layout.enc_dialog,null);
        EditText passwordField = view.findViewById(R.id.dialog_enc);

        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Enter Password")
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("ok", (dialog, which) -> {

                    if(TextUtils.isEmpty(passwordField.getText().toString()))
                        Toaster.makeToast(MainActivity.this,"Enter Password",Toaster.LENGTH_SHORT,Toaster.DEFAULT);
                    else
                        new DecryptUtil(MainActivity.this).decryptFile(path,passwordField.getText().toString(),editText);

                }).setNegativeButton("cancel", (dialog, which) -> dialog.dismiss()).create().show();

    }

    private void setTheme(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        boolean darkTheme = sharedPreferences.getBoolean("theme",false);
        if(darkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
