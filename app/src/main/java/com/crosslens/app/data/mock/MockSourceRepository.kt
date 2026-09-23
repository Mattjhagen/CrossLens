package com.crosslens.app.data.mock

import com.crosslens.app.core.model.Source
import com.crosslens.app.data.local.dao.SourceDao
import com.crosslens.app.data.local.toDomain
import com.crosslens.app.data.repository.SourceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockSourceRepository @Inject constructor(
    private val sourceDao: SourceDao
) : SourceRepository {

    override fun observeSources(): Flow<List<Source>> {
        return sourceDao.observeAllSources().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getSourcesByIds(sourceIds: List<String>): List<Source> {
        return sourceDao.getSourcesByIds(sourceIds).map { it.toDomain() }
    }

    override suspend fun getSource(sourceId: String): Source? {
        return getSourcesByIds(listOf(sourceId)).firstOrNull()
    }
}
