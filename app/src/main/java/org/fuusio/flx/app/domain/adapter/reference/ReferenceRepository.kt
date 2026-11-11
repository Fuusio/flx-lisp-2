package org.fuusio.flx.app.domain.adapter.reference

interface ReferenceRepository {

    /** The FLX reference document as a complete HTML page. */
    suspend fun getReferenceHtml(): String
}
