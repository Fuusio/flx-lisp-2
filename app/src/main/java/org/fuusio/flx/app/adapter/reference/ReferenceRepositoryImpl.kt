package org.fuusio.flx.app.adapter.reference

import org.fuusio.flx.app.domain.adapter.reference.ReferenceRepository
import org.fuusio.kide.adapter.AbstractRepository

class ReferenceRepositoryImpl(private val dataSource: ReferenceDataSource) :
    AbstractRepository(), ReferenceRepository {

    override suspend fun getReferenceHtml() = dataSource.readReferenceHtml()
}
