package com.crosslens.app.data.repository

import java.net.URI
import java.security.MessageDigest
import java.util.Locale

/** Conservative identity normalization. Preserve meaningful queries and original publisher links. */
object NewsIdentity {
    fun canonicalUrl(url: String): String = runCatching {
        val uri = URI(url)
        require(uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank())
        val query = uri.rawQuery?.split('&')?.filterNot {
            val key = it.substringBefore('=').lowercase(Locale.ROOT)
            key.startsWith("utm_") || key in setOf("fbclid", "gclid")
        }?.joinToString("&")?.takeIf { it.isNotEmpty() }
        buildString {
            append("https://")
            append(uri.host.lowercase(Locale.ROOT))
            if (uri.port != -1 && uri.port != 443) append(":${uri.port}")
            append(uri.rawPath.ifEmpty { "/" })
            if (query != null) append("?$query")
        }
    }.getOrDefault(url.substringBefore('#'))

    fun storyId(urls: List<String>): String =
        "live_feed_${digest(urls.map(::canonicalUrl).distinct().sorted().joinToString("\n"))}"

    // Namespace article rows by story so archived saved clusters retain their article relationships.
    fun articleId(url: String, storyId: String = ""): String =
        "article_${digest("$storyId\n${canonicalUrl(url)}")}"

    private fun digest(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
}
