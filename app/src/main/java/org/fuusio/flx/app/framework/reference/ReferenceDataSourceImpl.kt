package org.fuusio.flx.app.framework.reference

import android.content.Context
import org.fuusio.flx.app.adapter.reference.ReferenceDataSource
import org.fuusio.kide.framework.AbstractDataSource

/**
 * Reads the reference page from the app's assets. The page is made from `docs/reference/flx-reference.md`
 * by `docs/reference/build_reference_html.py`.
 */
class ReferenceDataSourceImpl(private val context: Context) : AbstractDataSource(), ReferenceDataSource {

    override suspend fun readReferenceHtml(): String = dispatch {
        context.assets.open(ASSET_NAME).bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    companion object {
        const val ASSET_NAME = "flx-reference.html"
    }
}
