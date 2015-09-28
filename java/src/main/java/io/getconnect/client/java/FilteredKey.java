package io.getconnect.client.java;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.security.AlgorithmParameters;
import java.security.SecureRandom;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import io.getconnect.client.GsonUTCDateAdapter;

/**
 * Provides a way to generate filtered keys for authenticating with the Connect API.
 */
public class FilteredKey {
    protected static final Gson gson = GsonUTCDateAdapter.createSerializer();

    public static String encrypt(final Map<String, Object> key, String masterKey) throws FilteredKeyException {
        try {
            SecureRandom rnd = new SecureRandom();
            byte[] iv = rnd.generateSeed(16);
            byte[] secret = masterKey.getBytes("UTF-8");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(secret, "AES"), new IvParameterSpec(iv));

            byte[] serializedKey = gson.toJson(key).getBytes("UTF-8");
            byte[] encryptedKey = cipher.doFinal(serializedKey);

            return toHex(iv) + "-" + toHex(encryptedKey);
        } catch (Exception ex) {
            throw new FilteredKeyException("An error occurred trying to generate the filtered key.", ex);
        }
    }

    public static Map<String, Object> decrypt(String filteredKey, String masterKey) throws FilteredKeyException {
        try {
            String hexedIv = filteredKey.substring(0, 32);
            String hexedCipherText = filteredKey.substring(32 + 1);

            byte[] iv = hexStringToByteArray(hexedIv);
            byte[] cipherText = hexStringToByteArray(hexedCipherText);

            SecretKey secret = new SecretKeySpec(masterKey.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));

            String decrypted = new String(cipher.doFinal(cipherText), "UTF-8");

            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            return gson.fromJson(decrypted, type);
        } catch (Exception ex) {
            throw new FilteredKeyException("An error occurred trying to decrypt the filtered key.", ex);
        }
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private static String toHex(byte[] value) {
        return DatatypeConverter.printHexBinary(value);
    }
}