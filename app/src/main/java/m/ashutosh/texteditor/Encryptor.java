package m.ashutosh.texteditor;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Encryptor {
    private static String AES = "AES";

    public static String encrypt(String text,String password) throws Exception{
        SecretKeySpec keySpec = generateKey(password);
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.ENCRYPT_MODE,keySpec);
        byte[] encryptionValue = cipher.doFinal(text.getBytes());
        return  Base64.encodeToString(encryptionValue, Base64.DEFAULT);
    }

    public static String decrypt(String key, String password) throws Exception{
        SecretKeySpec keySpec = generateKey(password);
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.DECRYPT_MODE,keySpec);
        byte[] decryptionValue = Base64.decode(key,Base64.DEFAULT);
        byte[] value = cipher.doFinal(decryptionValue);
        return new String(value);
    }

    public static SecretKeySpec generateKey(String password) throws Exception{
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = password.getBytes(StandardCharsets.UTF_8);
        digest.update(bytes,0,bytes.length);
        byte[] key = digest.digest();
        return new SecretKeySpec(key,"AES");
    }
}