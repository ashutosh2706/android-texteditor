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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private EditText editText;
    private long time;
    private Boolean lastOpened = false;
    private static final String SHARED_PREFS = "preferences";
    private static final String SHARED_FILENAME = "shared_filename";
    private static final String SHARED_FOLDER = "shared_folder";
    public static final String DEFAULT_LOCATION = "Documents/Text Editor";
    private final int CHOOSE_FILE_CODE = 191;
    private final int CHOOSE_FILE_CODE_ENC = 181;
    private Toast backToast;
    EncryptionManager encryptionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            requestPerm();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        switch (id) {

            case R.id.save:
                if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    saveMethod();
                }
                break;

            case R.id.open_last:
                openLast();
                break;

            case R.id.open_new:
                startActivity(new Intent(MainActivity.this, OpenNewFile.class));
                break;

            case R.id.app_settings:
                startActivity(new Intent(MainActivity.this,SettingsActivity.class));
                break;

            case R.id.encrypt:
                encryptText();
                break;

            case R.id.decrypt:
                decryptText();
                break;

            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == CHOOSE_FILE_CODE && resultCode == Activity.RESULT_OK){
            if (data != null) {
                String path = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                decryptText2(path);
            }else {
                Toast.makeText(this, "java.lang.NullPointerException", Toast.LENGTH_SHORT).show();
            }
            super.onActivityResult(requestCode, resultCode, data);
        }else if(requestCode == CHOOSE_FILE_CODE_ENC && resultCode == Activity.RESULT_OK){
            if(data!=null) {
                String path = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                encryptText2(path);
            }else {
                Toast.makeText(this, "java.lang.NullPointerException", Toast.LENGTH_SHORT).show();
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

    private void saveEngine(File output, String tempFile, String tempFolder) {

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(output);
            fileOutputStream.write(editText.getText().toString().getBytes());
            fileOutputStream.close();

            SharedPreferences preferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(SHARED_FILENAME,tempFile + ".txt");
            editor.putString(SHARED_FOLDER,tempFolder);
            editor.apply();

            Toast.makeText(this, "Saved", Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "java.lang.IOException", Toast.LENGTH_SHORT).show();
        }

    }

    private void saveMethod(){

        if(lastOpened){

            SharedPreferences preferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
            String filename = preferences.getString(SHARED_FILENAME,"$&$");
            String folderName = preferences.getString(SHARED_FOLDER,DEFAULT_LOCATION);
            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + folderName);
            File file = new File(dir,filename);

            if(file.exists()){

                String content = editText.getText().toString();
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(content.getBytes());
                    fileOutputStream.close();
                    lastOpened = false;

                    Toast.makeText(this, "Saved", Toast.LENGTH_LONG).show();

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "java.lang.IOException", Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(this, "An Error Occurred", Toast.LENGTH_SHORT).show();
            }
        }else {

            if (TextUtils.isEmpty(editText.getText().toString().trim())) {
                Toast.makeText(this, "¯\\_(ツ)_/¯", Toast.LENGTH_SHORT).show();
            } else {

                View v = getLayoutInflater().inflate(R.layout.main_dialog, null);
                EditText fileName = v.findViewById(R.id.dialog_filename);

                new AlertDialog.Builder(this)
                        .setTitle("Enter Filename")
                        .setView(v)
                        .setCancelable(false)
                        .setPositiveButton("ok", (dialog, which) -> {

                            String tempFilename0 = fileName.getText().toString();
                            if (!(TextUtils.isEmpty(fileName.getText().toString().trim()))) {

                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                                String location = preferences.getString("dirName", "Documents/Text Editor");
                                File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + location);
                                if (!dir.exists()) {
                                    dir.mkdirs();
                                }
                                File output = new File(dir, tempFilename0 + ".txt");
                                saveEngine(output, tempFilename0, location);

                            }else {
                                Toast.makeText(MainActivity.this, "Enter Filename", Toast.LENGTH_LONG).show();
                            }
                        }).setNegativeButton("cancel", (dialog, which) -> dialog.dismiss()).create().show();
            }
        }
    }

    private void requestPerm() {

        ActivityCompat.requestPermissions(MainActivity.this,new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},101);
    }
    
    private void openLast() {

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        String fileName = sharedPreferences.getString(SHARED_FILENAME,"$&$");
        String folderName = sharedPreferences.getString(SHARED_FOLDER,DEFAULT_LOCATION);
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + folderName);
        File file = new File(dir,fileName);

        if (!file.exists()) {

            Toast.makeText(this, "Not found", Toast.LENGTH_LONG).show();

        } else {

            String text;

            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuffer stringBuffer = new StringBuffer();
                while ((text = bufferedReader.readLine()) != null) {
                    stringBuffer.append(text + "\n");
                }
                editText.setText(stringBuffer.toString());
                lastOpened = true;
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "java.lang.IOException", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void encryptText() {

        MaterialFilePicker materialFilePicker = new MaterialFilePicker();
        materialFilePicker.withActivity(MainActivity.this);
        materialFilePicker.withCloseMenu(true).withPath(Environment.getExternalStorageDirectory().getAbsolutePath())
                .withRootPath(Environment.getExternalStorageDirectory().getAbsolutePath()).withHiddenFiles(false)
                .withFilter(Pattern.compile(".*\\.(txt)$")).withFilterDirectories(false)
                .withTitle("Choose File")
                .withRequestCode(CHOOSE_FILE_CODE_ENC).start();
    }

    private void encryptText2(String path) {
        File file = new File(path);
        if(file.exists()){

            String text;

            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuffer stringBuffer = new StringBuffer();
                while ((text = bufferedReader.readLine()) != null) {
                    stringBuffer.append(text + "\n");
                }
                encryptText3(stringBuffer.toString(),path);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "java.lang.IOException", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(this, "An Error Occurred", Toast.LENGTH_SHORT).show();
        }

    }

    private void encryptText3(String data, String filepath){
        View v = getLayoutInflater().inflate(R.layout.enc_dialog, null);
        EditText passwordField = v.findViewById(R.id.dialog_enc);

        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Enter Password")
                .setView(v)
                .setCancelable(false)
                .setPositiveButton("ok", (dialog, which) -> {

                    if (!(TextUtils.isEmpty(passwordField.getText().toString()))) {
                        encryptionManager = new EncryptionManager();
                        try {
                            String key = encryptionManager.encrypt(data, passwordField.getText().toString());
                            keyOutput(key,filepath);
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "An Error Occurred", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton("cancel", (dialog, which) -> dialog.dismiss()).create().show();

    }

    private void keyOutput(String key, String filepath) {

        File output = new File(filepath + ".enc");
        try {
            FileOutputStream fos = new FileOutputStream(output);
            fos.write(key.getBytes());
            fos.close();

            Toast.makeText(MainActivity.this, "File Encrypted", Toast.LENGTH_LONG).show();
            File temp = new File(filepath);
            temp.delete();

        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "java.lang.IOException", Toast.LENGTH_SHORT).show();
        }
    }

    private void decryptText(){

        MaterialFilePicker materialFilePicker = new MaterialFilePicker();
        materialFilePicker.withActivity(MainActivity.this);
        materialFilePicker.withCloseMenu(true).withPath(Environment.getExternalStorageDirectory().getAbsolutePath())
                .withRootPath(Environment.getExternalStorageDirectory().getAbsolutePath()).withHiddenFiles(false)
                .withFilter(Pattern.compile(".*\\.(enc)$")).withFilterDirectories(false)
                .withTitle("Choose File")
                .withRequestCode(CHOOSE_FILE_CODE).start();

    }

    private void decryptText2(String path){

        File file = new File(path);
        if(file.exists()){

            String text;

            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuffer stringBuffer = new StringBuffer();
                while ((text = bufferedReader.readLine()) != null) {
                    stringBuffer.append(text + "\n");
                }

                decryptText3(stringBuffer.toString(),path);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "java.lang.IOException", Toast.LENGTH_SHORT).show();
            }

        }else {
            Toast.makeText(this, "An Error Occurred", Toast.LENGTH_SHORT).show();
        }
    }

    private void decryptText3(String key, String filepath){

        View view = getLayoutInflater().inflate(R.layout.enc_dialog,null);
        EditText passwordField = view.findViewById(R.id.dialog_enc);

        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Enter Password")
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("ok", (dialog, which) -> {

                    if(TextUtils.isEmpty(passwordField.getText().toString())){
                        Toast.makeText(MainActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
                    }else {
                        encryptionManager = new EncryptionManager();
                        try {
                            String decryptedText = encryptionManager.decrypt(key,passwordField.getText().toString());
                            String file = filepath;
                            if(filepath.contains(".txt.enc")) {
                                file = filepath.replace(".txt.enc",".txt");
                            }else if(filepath.contains(".enc")) {
                                file = filepath.replace(".enc",".txt");
                            }

                            File output = new File(file);
                            try {
                                FileOutputStream fos = new FileOutputStream(output);
                                fos.write(decryptedText.getBytes());
                                fos.close();
                                editText.setText(decryptedText);
                                File temp = new File(filepath);
                                temp.delete();
                                Toast.makeText(MainActivity.this, "File Decrypted", Toast.LENGTH_LONG).show();

                            } catch (IOException e) {
                                Toast.makeText(MainActivity.this, "java.lang.IOException", Toast.LENGTH_SHORT).show();
                            }
                        }catch (Exception e){
                            Toast.makeText(MainActivity.this, "Incorrect Password", Toast.LENGTH_LONG).show();
                        }
                    }
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
