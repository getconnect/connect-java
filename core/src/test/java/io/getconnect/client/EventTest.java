package io.getconnect.client;

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertSame;
import static org.hamcrest.CoreMatchers.containsString;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.getconnect.client.exceptions.InvalidEventException;

public class EventTest {

    @Test(expected = InvalidEventException.class)
    public void testThatAnEmptyEventThrowsAnError() {
        Map<String, Object> eventData = new HashMap<String, Object>();
        Event event = new Event(eventData);
    }

    @Test(expected = InvalidEventException.class)
    public void testThatAReservedPropertyNameThrowsAnError() {
        Map<String, Object> eventData = new HashMap<String, Object>();
        eventData.put("tp_reserved", 200);
        Event event = new Event(eventData);
    }

    @Test
    public void testThatAnIdIsAddedIfNotProvided()  {
        Map<String, Object> eventData = new HashMap<String, Object>();
        eventData.put("speed", 200);

        Event event = new Event(eventData);

        Map<String, Object> processedEventData = event.getEventData();

        assertTrue(processedEventData.containsKey("id"));
    }

    @Test
    public void testThatAnIdIsNotOverriddenIfProvided()  {
        String id = "1234567";
        Map<String, Object> eventData = new HashMap<String, Object>();
        eventData.put("id", id);

        Event event = new Event(eventData);

        Map<String, Object> processedEventData = event.getEventData();

        assertSame(id, processedEventData.get("id"));
    }

    @Test
    public void testThatATimestampIsAddedIfNotProvided()  {
        Map<String, Object> eventData = new HashMap<String, Object>();
        eventData.put("speed", 200);

        Event event = new Event(eventData);

        Map<String, Object> processedEventData = event.getEventData();

        assertTrue(processedEventData.containsKey("timestamp"));
    }

    @Test
    public void testThatATimestampIsNotOverriddenIfProvided()  {
        Date now = new Date();
        Map<String, Object> eventData = new HashMap<String, Object>();
        eventData.put("timestamp", now);

        Event event = new Event(eventData);

        Map<String, Object> processedEventData = event.getEventData();

        assertSame(now, processedEventData.get("timestamp"));
    }

    @Test
    public void testThatDatesAreSerializedAsISO8601Strings()  {
        String isoDate = "2015-07-01T04:07:57.000Z";
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH);
        Date date = null;
        try {
            date = format.parse("2015-07-01T05:07:57+01:00");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Map<String, Object> eventData = new HashMap<String, Object>();
        eventData.put("timestamp", date);

        Event event = new Event(eventData);

        Map<String, Object> processedEventData = event.getEventData();
        String eventJSON = event.getEventJSON();

        assertThat(eventJSON, containsString(isoDate));
    }
}
