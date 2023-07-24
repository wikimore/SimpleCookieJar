package com.simplelifelab.simplecookiejar

import okhttp3.Cookie
import okhttp3.HttpUrl

/**
 * The CookieStore interface defines methods for managing cookies associated with URLs.
 * Implementations of this interface can store and retrieve cookies for specific URLs.
 */
interface CookieStore {
    /**
     * Adds a list of cookies to the store for the specified URL.
     *
     * @param url The URL to which the cookies should be associated.
     * @param cookies The list of cookies to be added.
     */
    fun add(url: HttpUrl, cookies: List<Cookie>)

    /**
     * Retrieves a list of cookies associated with the specified URL.
     *
     * @param url The URL for which to retrieve the cookies.
     * @return A list of cookies associated with the URL, or an empty list if no cookies are found.
     */
    fun get(url: HttpUrl): List<Cookie>

    /**
     * Retrieves all cookies stored in the cookie store.
     *
     * @return A list of all cookies stored in the cookie store.
     */
    fun getCookies(): List<Cookie>

    /**
     * Removes a specific cookie associated with the given URL from the store.
     *
     * @param url The URL for which to remove the cookie.
     * @param cookie The cookie to be removed.
     * @return true if the cookie was removed successfully, false otherwise.
     */
    fun remove(url: HttpUrl, cookie: Cookie): Boolean

    /**
     * Removes all cookies from the cookie store.
     *
     * @return true if all cookies were removed successfully, false otherwise.
     */
    fun removeAll(): Boolean
}
