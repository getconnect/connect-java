package io.getconnect.client.java;

import com.google.gson.Gson;

import java.security.SecureRandom;
import java.util.Map;

import javax.crypto.Cipher;
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
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecureRandom rnd = new SecureRandom();
            byte[] iv = rnd.generateSeed(16);
            byte[] secret = masterKey.getBytes("UTF-8");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(secret, "AES"), new IvParameterSpec(iv));

            byte[] serializedKey = gson.toJson(key).getBytes("UTF-8");
            byte[] encryptedKey = cipher.doFinal(serializedKey);

            return toHex(iv) + "-" + toHex(encryptedKey);
        } catch (Exception ex) {
            throw new FilteredKeyException("An error occurred trying to generate the filtered key.", ex);
        }
    }

    private static String toHex(byte[] value) {
        return DatatypeConverter.printHexBinary(value);
    }
}