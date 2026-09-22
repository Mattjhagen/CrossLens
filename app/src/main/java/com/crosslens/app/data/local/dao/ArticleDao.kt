package com.crosslens.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.crosslens.app.data.local.entity.ArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles WHERE storyId = :storyId")
    fun observeArticlesByStory(storyId: String): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE id IN (:articleIds)")
    suspend fun getArticlesByIds(articleIds: List<String>): List<ArticleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<ArticleEntity>)

    @Query("DELETE FROM articles")
    suspend fun deleteAllArticles()
}
