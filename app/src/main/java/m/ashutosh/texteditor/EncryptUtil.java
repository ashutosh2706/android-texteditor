package m.ashutosh.texteditor;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import m.ashutosh.toastertoast.Toaster;

public class EncryptUtil {
    private final Context context;
    private String filePath = null;
    private String password = null;
    public EncryptUtil(Context context) {
        this.context = context;
    }

    public void encryptFile(String path, String pass) {
        File file = new File(path);
        if(file.exists()) {

            filePath = path;
            password = pass;
            String text;

            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuffer stringBuffer = new StringBuffer();
                while ((text = bufferedReader.readLine()) != null) {
                    stringBuffer.append(text + "\n");
                }
                encryptText(stringBuffer.toString());

            } catch (Exception e) {
                Toaster.makeToast(context,""+e,Toaster.LENGTH_SHORT,Toaster.ERROR);
            }

        }else
            Toaster.makeToast(context,"File does not exist",Toaster.LENGTH_SHORT,Toaster.ERROR);
    }

    private void encryptText(String data){

        try {

            String key = new EncryptionManager().encrypt(data, password);
            File output = new File(filePath + ".enc");
            FileOutputStream fos = new FileOutputStream(output);
            fos.write(key.getBytes());
            fos.close();
            Toaster.makeToast(context,"File Encrypted",Toaster.LENGTH_LONG,Toaster.SUCCESS);

            if(!(new File(filePath).delete()))
                Toaster.makeToast(context,"File Error",Toaster.LENGTH_SHORT,Toaster.DEFAULT);

        }catch (Exception e) {
            Toaster.makeToast(context,""+e,Toaster.LENGTH_SHORT,Toaster.ERROR);
        }
    }
}
