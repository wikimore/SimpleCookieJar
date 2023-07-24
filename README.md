# SimpleCookieJar for Android

## Overview
The SimpleCookieJar is an Android library that provides a robust and efficient cookie management system for HTTP requests and responses.It allows Android applications to store and manage HTTP cookies persistently across multiple sessions, ensuring seamless user experiences and improved security.

## Key Features

- **Persistence**: The library leverages Android Datastore to store cookies persistently. This means that cookies remain valid even after the application is closed and can be retrieved and used for subsequent HTTP requests.

- **Easy Integration**: The library is designed to seamlessly integrate with OkHttp's CookieJar interface, making it effortless to manage cookies in OkHttp-powered applications.

- **Efficient Storage**: Cookies for different hosts are efficiently stored using ConcurrentHashMap instances, providing quick and easy access to cookies for specific URLs.

- **Expiration Handling**: The PersistentCookieJar includes a mechanism to handle expired cookies. Expired cookies are automatically removed from the store, ensuring that only valid cookies are used for requests.

## Usage Example

```
// build a PersistentCookieStore with your android.content.Context
val cookieStore = PersistentCookieStore(context) 

// build a SimpleCookieJar for okhttp
val simpleCookieJar = SimpleCookieJar(cookieStore) //

// integration with okhttp
Call.Factory = OkHttpClient.Builder().cookieJar(simpleCookieJar).build()

```