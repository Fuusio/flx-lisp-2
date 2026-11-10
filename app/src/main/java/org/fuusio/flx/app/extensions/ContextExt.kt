package org.fuusio.flx.app.extensions

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri

/**
 * Starts [intent], and says so rather than crashing when nothing on the device can handle it.
 *
 * A phone with no browser or no mail client — or a managed profile that has hidden one — throws
 * `ActivityNotFoundException` out of `startActivity`, which would take the whole screen down for
 * the sake of a tap on a link. A target activity guarded by an `android:permission` throws
 * `SecurityException` instead; both mean the same thing to the user, so both say the same thing.
 *
 * @param whenMissing Shown as a toast when the intent goes unanswered. Say what is missing, not
 *  that something failed — "no browser is available" tells the user what to do about it.
 */
fun Context.openExternally(intent: Intent, whenMissing: String) {
    try {
        // `startActivity` refuses outright from a context that is not an Activity, and does it by
        // throwing `AndroidRuntimeException` — which neither catch below would have covered. The
        // receiver is normally the Activity; the flag is what keeps that from being load-bearing.
        if (this !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, whenMissing, Toast.LENGTH_SHORT).show()
    } catch (_: SecurityException) {
        Toast.makeText(this, whenMissing, Toast.LENGTH_SHORT).show()
    }
}

/** Opens [url] in whatever the device uses for web pages. */
fun Context.openUrl(
    url: String,
    whenMissing: String = "No browser is available on this device.",
) = openExternally(Intent(Intent.ACTION_VIEW, url.toUri()), whenMissing)

/**
 * Opens the dialer with [number] already entered.
 *
 * `ACTION_DIAL`, never `ACTION_CALL`. Dialling places the number in front of the user and leaves
 * the decision to press call with them; `ACTION_CALL` would place the call itself and requires the
 * `CALL_PHONE` permission — a sensitive one that would have to be declared, justified on the Play
 * Data Safety form, and asked for at runtime, all to save one tap.
 *
 * `Uri.fromParts` rather than string concatenation: a number carries `+`, spaces, dashes and
 * brackets, and this encodes the scheme-specific part correctly without having to know which of
 * those are significant.
 */
fun Context.openDial(
    number: String,
    whenMissing: String = "No phone app is available on this device.",
) = openExternally(
    intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number.trim(), null)),
    whenMissing = whenMissing,
)

/**
 * Opens a new message to [address] in whatever the device uses for mail.
 *
 * `ACTION_SENDTO` with a `mailto:` URI rather than `ACTION_SEND`, so the chooser offers mail apps
 * alone instead of every share target on the device. [subject] and [body] are given twice, in the
 * URI and as extras, because clients disagree about where to read them: some parse the query,
 * others populate the compose form from extras alone and drop the query silently.
 */
fun Context.openEmail(
    address: String,
    subject: String? = null,
    body: String? = null,
    whenMissing: String = "No email app is available on this device.",
) {
    val uri = buildString {
        append("mailto:").append(address)
        val params = mutableListOf<String>()
        if (subject != null) params.add("subject=${Uri.encode(subject)}")
        if (body != null) params.add("body=${Uri.encode(body)}")
        if (params.isNotEmpty()) {
            append("?").append(params.joinToString("&"))
        }
    }.toUri()

    openExternally(
        intent = Intent(Intent.ACTION_SENDTO, uri).apply {
            if (subject != null) putExtra(Intent.EXTRA_SUBJECT, subject)
            if (body != null) putExtra(Intent.EXTRA_TEXT, body)
        },
        whenMissing = whenMissing,
    )
}
