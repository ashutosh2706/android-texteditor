package m.ashutosh.texteditor;

import android.content.Context;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import m.ashutosh.toastertoast.Toaster;

public class DecryptUtil {
    private final Context context;
    private String filePath = null;
    private String password = null;

    public DecryptUtil(Context context) {
        this.context = context;
    }

    public void decryptFile(String path, String pass, EditText editText) {
        File file = new File(path);
        if (file.exists()) {

            filePath = path;
            password = pass;

            String text;

            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
                StringBuffer stringBuffer = new StringBuffer();
                while ((text = bufferedReader.readLine()) != null) {
                    stringBuffer.append(text + "\n");
                }

                decryptText(stringBuffer.toString(),editText);

            } catch (IOException e) {
                Toaster.makeToast(context,""+e,Toaster.LENGTH_SHORT,Toaster.ERROR);
            }

        }else
            Toaster.makeToast(context,"File does not exist",Toaster.LENGTH_SHORT,Toaster.ERROR);
    }

    private void decryptText(String key, EditText editText) {

        try {
            String plainText = Encryptor.decrypt(key, password);

            String file = filePath;
            if(filePath.contains(".txt.enc")) {
                file = filePath.replace(".txt.enc",".txt");
            }else if(filePath.contains(".enc")) {
                file = filePath.replace(".enc",".txt");
            }

            FileOutputStream fos = new FileOutputStream(new File(file));
            fos.write(plainText.getBytes());
            fos.close();

            editText.setText(plainText);
            Toaster.makeToast(context,"File Decrypted",Toaster.LENGTH_SHORT,Toaster.SUCCESS);

            if(!(new File(filePath).delete()))
                Toaster.makeToast(context,"File Error",Toaster.LENGTH_SHORT,Toaster.DEFAULT);

        }catch (Exception e) {
            Toaster.makeToast(context,"Wrong Password",Toaster.LENGTH_SHORT,Toaster.ERROR);
        }

    }
}
