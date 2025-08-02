package nl.vu.cs.softwaredesign.services;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public interface Encryptable {
    void encrypt(String archivePath, String encryptionKey) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, IOException, BadPaddingException, InvalidKeyException, InterruptedException, InvalidKeySpecException;
    Boolean decrypt(String archivePath, String decryptionKey) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException;
}
