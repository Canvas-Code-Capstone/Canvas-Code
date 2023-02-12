package com.canvas.service.helperServices;

import com.canvas.exceptions.CanvasAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

/**
 * AESCryptoService class that provides methods to encrypt and decrypt Access tokens.
 */
@Component
public class AESCryptoService {
    private static SecretKeySpec secretKey;

    /**
     * Constructor that sets the secret key using the environment property "canvas.secretKey".
     *
     * @param env environment instance
     */
    @Autowired
    public AESCryptoService(Environment env) {
        this.setKey(env.getProperty("canvas.secretKey"));
    }

    /**
     * Helper method to set the secret key.
     *
     * @param myKey key to be set as secret key
     */
    private void setKey(String myKey) {
        MessageDigest sha = null;
        try {
            byte[] key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Encrypts the input string using AES algorithm.
     *
     * @param strToEncrypt string to be encrypted
     * @param secret secret to use for encryption
     * @return encrypted string
     * @throws CanvasAPIException if error occurs during encryption
     */
    public String encrypt(String strToEncrypt, String secret) throws CanvasAPIException {
        try {
            //this.setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
            throw new CanvasAPIException(e.getMessage());
        }
    }

    /**
     * Decrypts the input string using AES algorithm.
     *
     * @param strToDecrypt string to be decrypted
     * @param secret secret to use for decryption
     * @return decrypted string
     * @throws CanvasAPIException if error occurs during decryption
     */
    public String decrypt(String strToDecrypt, String secret) throws CanvasAPIException {
        try {
            //this.setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            throw new CanvasAPIException(e.getMessage());
        }
    }
}
