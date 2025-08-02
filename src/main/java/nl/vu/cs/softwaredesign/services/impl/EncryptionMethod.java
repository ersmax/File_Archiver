package nl.vu.cs.softwaredesign.services.impl;

import nl.vu.cs.softwaredesign.services.Encryptable;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class EncryptionMethod implements Encryptable {
    public static String checkPasswordStrength(String password) {
        // Password must be of length of 4 or greater
        if (password.matches(".{4,}")) {
            return "ok";
        } else return "weak";
    }

    private void doEncrypt(String key, File inputFile, File outputFile) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, IOException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
        doCrypto(Cipher.ENCRYPT_MODE, key, inputFile, outputFile);
    }

    private void doDecrypt(String key, File inputFile, File outputFile) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, IOException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
        doCrypto(Cipher.DECRYPT_MODE, key, inputFile, outputFile);
    }

    private void doCrypto(int cipherMode, String key, File inputFile, File outputFile) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
        Key keySpec = getKeyFromPassword(key, "somerandomsalt");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(cipherMode, keySpec);

        FileInputStream iStream = new FileInputStream(inputFile);
        byte[] iBytes = new byte[(int) inputFile.length()];
        iStream.read(iBytes);

        byte[] oBytes = cipher.doFinal(iBytes);

        FileOutputStream oStream = new FileOutputStream(outputFile);
        oStream.write(oBytes);

        iStream.close();
        oStream.close();
    }
    @Override
    public void encrypt(String archivePath, String encryptionKey) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, IOException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
        File inputFile = new File(archivePath);

        File encryptedFile = new File(archivePath + ".encrypted");
        encryptedFile.createNewFile();

        doEncrypt(encryptionKey, inputFile, encryptedFile);

        inputFile.delete();
    }

    @Override
    public Boolean decrypt(String archivePath, String decryptionKey) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
        File encryptedFile = new File(archivePath);

        File decryptedFile = new File(archivePath.replace(".encrypted", ""));
        decryptedFile.createNewFile();

        doDecrypt(decryptionKey, encryptedFile, decryptedFile);

        return encryptedFile.delete();

    }

    public static SecretKey getKeyFromPassword(String password, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
        return new SecretKeySpec(keyFactory.generateSecret(spec).getEncoded(), "AES");
    }
}
