package com.crosslens.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.crosslens.app.data.local.entity.TranslationEntity

@Dao
interface TranslationDao {
    @Query("SELECT * FROM translations WHERE articleId = :articleId AND targetLanguage = :targetLanguage")
    suspend fun getTranslation(articleId: String, targetLanguage: String): TranslationEntity?

    @Query("SELECT * FROM translations WHERE articleId IN (:articleIds)")
    suspend fun getTranslationsByArticleIds(articleIds: List<String>): List<TranslationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslations(translations: List<TranslationEntity>)

    @Query("DELETE FROM translations")
    suspend fun deleteAllTranslations()
}
