package org.fuusio.flx.app.adapter.reference

interface ReferenceDataSource {

    suspend fun readReferenceHtml(): String
}
