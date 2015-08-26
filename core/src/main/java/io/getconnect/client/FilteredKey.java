package io.getconnect.client;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.StringWriter;
import java.security.SecureRandom;
import java.util.Map;

/**
 * Provides a way to generate filtered keys for authenticating with the Connect API.
 */
public class FilteredKey {
    public static String encrypt(final JsonSerializer serializer, final Map<String, Object> key, String masterKey) throws FilteredKeyException {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecureRandom rnd = new SecureRandom();
            byte[] iv = rnd.generateSeed(16);
            byte[] secret = masterKey.getBytes("UTF-8");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(secret, "AES"), new IvParameterSpec(iv));

            StringWriter writer = new StringWriter();
            serializer.serialize(writer, key);

            byte[] serializedKey = writer.toString().getBytes("UTF-8");
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