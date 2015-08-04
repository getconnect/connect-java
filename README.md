Connect Java SDK
================

[![Join the chat at https://gitter.im/getconnect/connect-java](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/getconnect/connect-java?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

The Connect Java SDK allows you to push events to Connect from any Java or Android application.

The SDK provides a core library which provides the core functionality to push and queue batches and single events.

Both a Java library and Android-specific library are provided. The Java library depends on Jackson for JSON serialization, whereas the Android library utilizes the in-built JSON serialization functionality.

If you don't already have a Connect account, [sign up here](https://getconnect.io) - it's free!

## Installation

Installing the SDK is easy if you're using either Gradle or Maven.

### Gradle

For Java, use:

```groovy
repositories {
    mavenCentral()
}
dependencies {
    compile 'io.getconnect:connect-client-java:1.0'
}
```

For Android, use:

```groovy
repositories {
    mavenCentral()
}
dependencies {
    compile 'io.getconnect:connect-client-android:1.0@aar'
}
```

### Maven

Add the following dependency to your pom.xml:

```xml
<dependency>
  <groupId>io.getconnect</groupId>
  <artifactId>connect-client-java</artifactId>
  <version>1.0</version>
</dependency>
```

For Android, use the following dependency:

```xml
<dependency>
  <groupId>io.getconnect</groupId>
  <artifactId>connect-client-android</artifactId>
  <type>aar</type>
  <version>1.0</version>
</dependency>
```

## Usage

### Creating a client

Each library (Java or Android) provides a specific sub-class of `ConnectClient` which provides default implementations for JSON serialization.

To create an Android client:

```java
ConnectClient client = new AndroidConnectClient("PROJECT_ID", "PUSH_API_KEY");
```

To create a Java client:

```java
ConnectClient client = new JavaConnectClient("PROJECT_ID", "PUSH_API_KEY");
```

Each client is bound to a specific project.  If you wish to push to multiple projects, simply construct multiple clients.

You can construct your own `ConnectClient` and provide it with your own implementations for JSON serialization, an HTTP client or event storage.

### Pushing events

Once you have created a client, you can push events easily:

```java
// Construct the event
HashMap<String, Object> event = new HashMap<String, Object>();
event.put("product", "banana");
event.put("quantity", 5);
event.put("totalCost", 14.75);

// Push the event synchronously to Connect
client.push("productsSold", event);
```

### Queueing events

You can also queue events for pushing later to Connect.  This is useful if you are collecting many events and wish to push them periodically or on a specific trigger.

Queueing events simply pushes the event into the configured `EventStore` (see "Configuring event stores" below).

To queue an event:

```java
// Construct the event
HashMap<String, Object> event = new HashMap<String, Object>();
event.put("product", "banana");
event.put("quantity", 5);
event.put("totalCost", 14.75);

// Add the event to the queue
client.add("productsSold", event);
```

Periodically or on a specific trigger, you must call `pushPending()` which will synchronously push the queued events in a batch to Connect:

```java
// Push the queued events to Connect
client.pushPending();
```

## Configuring event stores

To queue events, the SDK uses an `EventStore` to store and retrieve events for queueing and later pushing, respectively.

By default, `AndroidConnectClient` uses `FileEventStore` to store events temporarily on the filesystem (in Android's cache directory).  This store is persistent and will guarantee delivery even in the event of app/device failure.

By default, `JavaConnectClient` uses `MemoryEventStore` to store events temporarily in memory.  This store is **not** persistent and is destroyed on application termination, therefore you should not use it for guaranteed delivery.

`JavaConnectClient` can be easily configured to store events in a `FileEventStore` on the filesystem by specifying an event store directory:

```java
ConnectClient client = new JavaConnectClient("PROJECT_ID", "PUSH_API_KEY", "/path/to/event/store");
```

### Pushing multiple events in a batch

You can also push multiple events to multiple collections in a single call:

```java
// Create the batch (collection name is the key)
HashMap<String, Map<String, Object>[]> batch = new HashMap<String, Map<String, Object>[]>();

// Construct the events
HashMap<String, Object> event1 = new HashMap<String, Object>();
event1.put("product", "banana");
event1.put("quantity", 5);
event1.put("totalCost", 14.75);

HashMap<String, Object> event2 = new HashMap<String, Object>();
event1.put("product", "carrot");
event1.put("quantity", 2);
event1.put("totalCost", 4.00);

batch.put("productsSold", new Map[] { event1, event2 });

// Push the batch to Connect
client.push(batch);
```

### Exception handling

When pushing events, exceptions are thown, so you should either ignore or handle those exceptions gracefully.

Specifically, the following exceptions can be thrown when pushing events synchronously:

* DuplicateEventException - the event being pushed already exists
* InvalidEventException - the event being pushed is invalid (e.g. invalid event properties)
* ServerException - a server-side exception occurred in Connect's API
* IOException - an exception occurred sending or receiving the request or response, respectively (e.g. a network failure)

## Building

The SDK uses Gradle to run the builds, simple run:

`./gradlew build` on *nix

`gradlew build` on Windows

This will build the JAR for the Java/core libraries and the AAR for the Android library.

To build a JAR for the Android library, run:

`gradlew :android:rawAndroidJarRelease`

## Android considerations

You must ensure that your Android app has the `INTERNET` permission to allow the SDK to push events to the Connect API.  Make sure you have specified this in your AndroidManifest.xml:

```xml
<uses-permission android:name="android.permission.INTERNET"/>
```

## Changelog

#### 1.0

+ Initial commit

## License

The SDK is released under the MIT license.

## Contributing

We love open source and welcome pull requests and issues from the community!