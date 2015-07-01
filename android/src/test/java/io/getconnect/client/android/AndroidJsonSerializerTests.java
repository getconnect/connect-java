package io.getconnect.client.android;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import io.getconnect.BuildConfig;
import io.getconnect.client.JsonSerializer;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19)
public class AndroidJsonSerializerTests {
    @Test
    public void testDeserialize() throws IOException {
        JsonSerializer serializer = new AndroidJsonSerializer();

        String json = "{'id':'1234','timestamp':'2015-01-01T00:00:00Z','product':'blah','addons':[1,2,3],'nested':{'blah':1}}";

        StringReader reader = new StringReader(json);
        try {
            Map<String, Object> map = serializer.deserialize(reader);
            Assert.assertEquals("1234", map.get("id"));
            Assert.assertEquals("blah", map.get("product"));
            Integer[] expected = { 1, 2, 3 };
            ArrayList<Integer> actual = (ArrayList<Integer>) map.get("addons");
            Assert.assertArrayEquals(expected, actual.toArray(new Integer[actual.size()]));
            Assert.assertEquals(1, ((Map<String, Object>) map.get("nested")).get("blah"));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testSerialize() throws IOException {
        JsonSerializer serializer = new AndroidJsonSerializer();

        HashMap<String, Object> map = new HashMap<String, Object>();

        HashMap<String, Object> nested = new HashMap<String, Object>();
        nested.put("blah", 1);

        ArrayList<Integer> addons = new ArrayList<Integer>();
        addons.add(1);
        addons.add(2);
        addons.add(3);

        Calendar timestamp = GregorianCalendar.getInstance();
        timestamp.setTimeZone(TimeZone.getTimeZone("UTC"));
        timestamp.set(2015, 1, 1, 0, 0, 0);
        timestamp.set(Calendar.MILLISECOND, 0);

        map.put("id", "1234");
        map.put("timestamp", timestamp.getTime());
        map.put("product", "blah");
        map.put("addons", addons);
        map.put("nested", nested);

        StringWriter writer = new StringWriter();
        try {
            serializer.serialize(writer, map);
        } finally {
            writer.close();
        }

        String json = writer.toString();

        Assert.assertTrue(json.contains("\"product\":\"blah\""));
        Assert.assertTrue(json.contains("\"timestamp\":\"2015-02-01T00:00:00.000Z\""));
        Assert.assertTrue(json.contains("\"nested\":{\"blah\":1}"));
        Assert.assertTrue(json.contains("\"addons\":[1,2,3]"));
    }
}
