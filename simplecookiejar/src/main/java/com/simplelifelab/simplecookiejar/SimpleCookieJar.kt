package com.simplelifelab.simplecookiejar

import com.simplelifelab.simplecookiejar.CookieStore
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * The SimpleCookieJar class is an implementation of the [CookieJar] interface that uses
 * a provided [CookieStore] to manage cookies for HTTP requests and responses.
 *
 * @param cookieStore The [CookieStore] instance that will be used to manage cookies.
 */
class SimpleCookieJar(private val cookieStore: CookieStore) : CookieJar {
    /**
     * Retrieves the list of cookies associated with the specified [url] from the [cookieStore].
     *
     * @param url The URL for which to retrieve the cookies.
     * @return A list of cookies associated with the URL, or an empty list if no cookies are found.
     */
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore.get(url)
    }

    /**
     * Adds a list of [cookies] to the [cookieStore] for the specified [url].
     *
     * @param url The URL to which the cookies should be associated.
     * @param cookies The list of cookies to be added.
     */
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore.add(url, cookies)
    }
}