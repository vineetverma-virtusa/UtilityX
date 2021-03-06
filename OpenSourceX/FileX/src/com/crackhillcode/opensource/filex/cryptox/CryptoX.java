package com.crackhillcode.opensource.filex.cryptox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

public class CryptoX {
    static String pwd = "";

    private static String compressPassword(String pass) {
        char[] tmp = new char[8];
        if (pass.length() > 8) {
            for (int i = 0; i < pass.toCharArray().length; i++) {
                tmp[i % 7] += pass.toCharArray()[i];
            }
            pass = new String(tmp);
        }
        return pass;
    }

    public void beginEncrypt(String in, String pass, String outputPath) throws Exception {
        try {
            pass = compressPassword(pass);
            processEncrypt(in, outputPath, (pass));
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private String encryptName(String str) {
        String encrypted = "";
        int keyLength = pwd.length();
        for (int i = 0; i < str.length(); i++) {
            int c = str.charAt(i);

            if (Character.isUpperCase(c)) {
                // 26 letters of the alphabet so mod by 26
                c = c + (keyLength % 26);
                if (c > 'Z')
                    c = c - 26;
            } else if (Character.isLowerCase(c)) {
                c = c + (keyLength % 26);
                if (c > 'z')
                    c = c - 26;
            }
            encrypted += (char) c;
        }
        return encrypted;
    }

    private void encrypt(File fileIn, File fileOut, String pass) throws Exception {

        fileIn.setReadable(true);
        fileOut.setWritable(true);
        fileOut.setReadable(true);
        FileInputStream fis = new FileInputStream(fileIn);
        FileOutputStream fos = new FileOutputStream(fileOut);
        byte k[] = pass.getBytes();
        final String algo = "DES/ECB/PKCS5Padding";
        SecretKeySpec key = new SecretKeySpec(k, algo.split("/")[0]);
        Cipher encrypt = Cipher.getInstance(algo);
        encrypt.init(Cipher.ENCRYPT_MODE, key);
        CipherOutputStream cout = new CipherOutputStream(fos, encrypt);

        byte[] buf = new byte[1024];
        int read;
        while ((read = fis.read(buf)) != -1) {
            cout.write(buf, 0, read);
            //            System.out.print(".");

        }

        fis.close();
        cout.flush();
        cout.close();
    }

    private void processEncrypt(String in, String out, String pass) throws IOException {

        File fin = new File(in);
        File fout = new File(out);
        fout.setWritable(true);
        fout.setReadable(true);
        fin.setReadable(true);

        if (fin.getName().startsWith(".")) {
            return;
        }

        if (!fout.exists() && fin.isDirectory() && !fin.getName().equals("Encrypt")) {
            fout.mkdirs();
            fout.setWritable(true);
        } else if (!fin.isDirectory()) {
            try {
                encrypt(fin, new File(out), pass);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (fin.isDirectory() && !fin.getName().equals("Encrypt")) {
            for (int i = 0; i < fin.list().length; i++) {
                processEncrypt(in + "/" + fin.list()[i], out + "/" + encryptName(fin.list()[i]), pass);
            }
        }
    }

    public void beginDecrypt(String in, String pass, String output) throws Exception {
        try {
            processDecrypt(in, output, compressPassword(pass));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private String decryptName(String str) {
        String decrypted = "";

        int keyLength = pwd.length();
        for (int i = 0; i < str.length(); i++) {
            int c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                c = c - (keyLength % 26);
                if (c < 'A')
                    c = c + 26;
            } else if (Character.isLowerCase(c)) {
                c = c - (keyLength % 26);
                if (c < 'a')
                    c = c + 26;
            }
            decrypted += (char) c;
        }
        return decrypted;
    }

    private void processDecrypt(String in, String out, String pass) throws Exception {

        File fin = new File(in);
        File fout = new File(out);

        if (!fin.exists()) {
            return;
        }

        if (fin.getName().startsWith(".")) {
            return;
        }

        if (!fout.exists() && fin.isDirectory()) {
            fout.mkdirs();
            fout.setWritable(true);
        } else if (!fin.isDirectory()) {
            try {
                // out = out.substring(0, out.length()
                // - encryptName(".enc").length());

                decrypt(fin, new File(out), pass);

            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }

        if (fin.isDirectory()) {
            for (int i = 0; i < fin.list().length; i++) {
                processDecrypt(in + "/" + fin.list()[i], out + "/" + decryptName(fin.list()[i]), pass);
            }
        }
    }

    private void decrypt(File fileIn, File fileOut, String pass) throws Exception {
        fileOut.setWritable(true);
        fileOut.setReadable(true);
        fileIn.setReadable(true);
        final String algo = "DES/ECB/PKCS5Padding";
        FileInputStream fis = new FileInputStream(fileIn);
        FileOutputStream fos = new FileOutputStream(fileOut);
        byte k[] = pass.getBytes();
        SecretKeySpec key = new SecretKeySpec(k, algo.split("/")[0]);
        Cipher decrypt = Cipher.getInstance(algo);
        decrypt.init(Cipher.DECRYPT_MODE, key);
        CipherInputStream cin = new CipherInputStream(fis, decrypt);

        byte[] buf = new byte[1024];
        int read = 0;
        while ((read = cin.read(buf)) != -1) {
            fos.write(buf, 0, read);
        }
        cin.close();
        fos.flush();
        fos.close();
    }

    public CryptoX() {
        super();
    }

    public static void main(String[] args) {
    }
}
