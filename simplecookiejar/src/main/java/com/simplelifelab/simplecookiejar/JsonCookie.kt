package com.simplelifelab.simplecookiejar

import okhttp3.Cookie
import org.json.JSONObject

/**
 * JsonCookie is a data class representing a cookie in JSON format. It stores the various attributes
 * of a cookie such as name, value, domain, path, expiresAt, secure, and httpOnly.
 *
 * @property name The name of the cookie.
 * @property value The value of the cookie.
 * @property domain The domain of the cookie.
 * @property path The path of the cookie.
 * @property expiresAt The expiration time of the cookie, in milliseconds since epoch. Defaults to Long.MAX_VALUE.
 * @property secure Whether the cookie should be limited to secure (HTTPS) requests. Defaults to true.
 * @property httpOnly Whether the cookie should be limited to HTTP APIs and not accessible to scripts. Defaults to true.
 */
data class JsonCookie(
    val name: String,
    val value: String,
    val domain: String,
    val path: String,
    val expiresAt: Long = Long.MAX_VALUE,
    val secure: Boolean = true,
    val httpOnly: Boolean = true
) {
    companion object {
        /**
         * Parses a JSON string representation of a JsonCookie and returns a JsonCookie object.
         *
         * @param json The JSON string representation of the JsonCookie.
         * @return The parsed JsonCookie object.
         */
        fun parseJSONString(json: String): JsonCookie {
            val jsonObject = JSONObject(json)
            val name = jsonObject.getString("name")
            val value = jsonObject.getString("value")
            val domain = jsonObject.getString("domain")
            val path = jsonObject.getString("path")
            val expiresAt = jsonObject.getLong("expiresAt")
            val secure = jsonObject.getBoolean("secure")
            val httpOnly = jsonObject.getBoolean("httpOnly")
            return JsonCookie(name, value, domain, path, expiresAt, secure, httpOnly)
        }

        /**
         * Converts an OkHttp Cookie object to a JsonCookie object.
         *
         * @param cookie The OkHttp Cookie object to be converted.
         * @return The converted JsonCookie object.
         */
        fun parseCookie(cookie: Cookie): JsonCookie {
            return JsonCookie(
                cookie.name,
                cookie.value,
                cookie.domain,
                cookie.path,
                cookie.expiresAt,
                cookie.secure,
                cookie.httpOnly
            )
        }
    }
}

/**
 * Converts a JsonCookie object to an OkHttp Cookie object.
 *
 * @return The converted OkHttp Cookie object.
 */
fun JsonCookie.toOkHttpCookie(): Cookie {
    val builder = Cookie.Builder()
    builder.name(name).expiresAt(expiresAt).domain(domain).path(path).value(value)
    if (secure) {
        builder.secure()
    }
    if (httpOnly) {
        builder.httpOnly()
    }
    return builder.build()
}

/**
 * Converts a JsonCookie object to its JSON string representation.
 *
 * @return The JSON string representation of the JsonCookie object.
 */
fun JsonCookie.toJSONString(): String {
    val jsonObject = JSONObject()
    jsonObject.put("name", name)
    jsonObject.put("value", value)
    jsonObject.put("domain", domain)
    jsonObject.put("path", path)
    jsonObject.put("expiresAt", expiresAt)
    jsonObject.put("secure", secure)
    jsonObject.put("httpOnly", httpOnly)
    return jsonObject.toString()
}