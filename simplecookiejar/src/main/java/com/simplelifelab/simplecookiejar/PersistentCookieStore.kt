package com.simplelifelab.simplecookiejar

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.simplelifelab.simplecookiejar.CookieStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Cookie
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

/**
 * PersistentCookieStore is an implementation of CookieStore that persists cookies using
 * Android DataStore. It stores cookies for different hosts in separate ConcurrentHashMaps,
 * and each host's cookies are further stored as key-value pairs in DataStore.
 *
 * Storage Structure:
 *  - DataStore File: CookiePrefsFile
 *  - Key Format: "cookie_${url.host}_${cookie.name}"
 *  - Value Format: JSON representation of JsonCookie
 */
class PersistentCookieStore constructor(
    context: Context
) : CookieStore {
    companion object {
        private const val COOKIE_NAME_PREFIX = "cookie_"
        private const val LOG_TAG = "PersistentCookieStore"
        private const val COOKIE_PREFS_FILE = "CookiePrefsFile"
    }

    // DataStore instance to store cookies
    private val Context.createDataStore: DataStore<Preferences> by preferencesDataStore(
        name = COOKIE_PREFS_FILE
    )
    private val cookieDataStore = context.createDataStore

    // Cache to store cookies for different hosts
    private val cookieCache: ConcurrentHashMap<String, ConcurrentHashMap<String, Cookie>> =
        ConcurrentHashMap()

    // Initialize the PersistentCookieStore by reading cookies from DataStore
    init {
        runBlocking {
            cookieDataStore.data.first { mutablePreferences ->
                mutablePreferences.asMap().entries.forEach { entry ->
                    val host = getHostFromPrefsKey(entry.key.name)
                    if (!cookieCache.containsKey(host)) {
                        cookieCache.putIfAbsent(host, ConcurrentHashMap<String, Cookie>())
                    }
                    val cookie = JsonCookie.parseJSONString(entry.value.toString()).toOkHttpCookie()
                    cookieCache[host]?.put(cookie.name, cookie)
                }
                return@first true
            }
        }

        // TODO Clear expired cookies
    }

    /**
     * Adds cookies to the cache and persists them to DataStore.
     *
     * @param url The URL associated with the cookies.
     * @param cookies The list of cookies to be added.
     */
    override fun add(url: HttpUrl, cookies: List<Cookie>) {
        if (!cookieCache.containsKey(url.host)) {
            cookieCache.putIfAbsent(url.host, ConcurrentHashMap<String, Cookie>())
        }
        for (cookie in cookies) {
            if (cookie.persistent) {
                cookieCache[url.host]?.put(cookie.name, cookie)
            } else {
                cookieCache[url.host]?.remove(cookie.name)
            }
        }
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            cookieDataStore.edit { mutablePreferences ->
                for (cookie in cookies) {
                    val name = "${Companion.COOKIE_NAME_PREFIX}${url.host}_${cookie.name}"
                    if (cookie.persistent) {
                        mutablePreferences[stringPreferencesKey(name)] =
                            JsonCookie.parseCookie(cookie).toJSONString()
                    } else {
                        mutablePreferences.remove(stringPreferencesKey(name))
                    }
                }
            }
        }
    }

    /**
     * Returns the list of cookies associated with the given URL, excluding expired cookies.
     *
     * @param url The URL to retrieve cookies for.
     * @return List of cookies for the URL.
     */
    override fun get(url: HttpUrl): List<Cookie> {
        val cookies = cookieCache[url.host]
        return cookies?.values?.filter { cookie ->
            cookie.expiresAt > System.currentTimeMillis()
        } ?: emptyList()
    }

    /**
     * Returns all cookies in the cache, excluding expired cookies.
     *
     * @return List of all non-expired cookies.
     */
    override fun getCookies(): List<Cookie> {
        val ret = mutableListOf<Cookie>()
        for (key in cookieCache.keys) {
            cookieCache[key]?.let { ret.addAll(it.values) }
        }
        return ret
    }

    /**
     * Removes the specified cookie from the cache and also deletes it from DataStore.
     *
     * @param url The URL associated with the cookie to be removed.
     * @param cookie The cookie to be removed.
     * @return True if the cookie was successfully removed, false otherwise.
     */
    override fun remove(url: HttpUrl, cookie: Cookie): Boolean {
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            cookieDataStore.edit { mutablePreferences ->
                mutablePreferences.remove(stringPreferencesKey("${Companion.COOKIE_NAME_PREFIX}${url.host}_${cookie.name}"))
            }
        }
        return cookieCache[url.host]?.remove(cookie.name) != null
    }

    /**
     * Removes all cookies from the cache and DataStore.
     *
     * @return True if all cookies were successfully removed, false otherwise.
     */
    override fun removeAll(): Boolean {
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            cookieDataStore.edit { mutablePreferences ->
                for (key in cookieCache.keys) {
                    val cookies = cookieCache[key]?.values
                    if (cookies != null) {
                        for (cookie in cookies) {
                            mutablePreferences.remove(stringPreferencesKey("${Companion.COOKIE_NAME_PREFIX}${cookie.domain}_${cookie.name}"))
                        }
                    }
                }
            }
        }
        cookieCache.clear()
        return true
    }

    /**
     * Extracts the host name from the given DataStore key.
     *
     * @param key The DataStore key.
     * @return The host name extracted from the key.
     */
    private fun getHostFromPrefsKey(key: String): String {
        return key.split("_")[1]
    }
}

