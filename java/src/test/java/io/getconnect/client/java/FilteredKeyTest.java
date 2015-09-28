package io.getconnect.client.java;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by chadedrupt on 25/09/15.
 */

public class FilteredKeyTest {

    @Test
    public void testEncryptionAndDecryption() throws FilteredKeyException {
        String apiKey = "80ce00d60d6443118017340c42d1cfaf";

        HashMap<String, Object> filters = new HashMap<String, Object>();
        filters.put("type", "cycling");

        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put("filters", filters);
        options.put("canQuery", true);
        options.put("canPush", false);

        String filteredKey = FilteredKey.encrypt(options, apiKey);
        Map<String, Object> decryptedOptions = FilteredKey.decrypt(filteredKey, apiKey);

        assertEquals(options, decryptedOptions);
    }

}
