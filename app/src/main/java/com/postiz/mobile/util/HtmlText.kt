package com.postiz.mobile.util

import android.text.Html

/**
 * Post content comes from Postiz's rich-text editor as HTML (`<p>...</p>`,
 * `<strong>`, etc.), not plain text -- the web app strips it before display
 * too (stripHtmlValidation). Renders entities correctly and drops tags
 * instead of showing them literally.
 */
fun stripHtml(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    return Html.fromHtml(raw, Html.FROM_HTML_MODE_COMPACT)
        .toString()
        .replace(Regex("\n{2,}"), "\n")
        .trim()
}
