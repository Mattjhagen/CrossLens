package com.crosslens.app.feature.editorial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crosslens.app.data.repository.EditorialReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditorialReviewViewModel @Inject constructor(
    private val reviewRepository: EditorialReviewRepository
) : ViewModel() {

    // Mock candidates for demo (in production, these would come from ingestion service)
    private val mockCandidates = createMockCandidates()

    private val _uiState = MutableStateFlow(EditorialReviewUiState())
    val uiState: StateFlow<EditorialReviewUiState> = _uiState.asStateFlow()

    init {
        loadReviews()
    }

    private fun loadReviews() {
        viewModelScope.launch {
            reviewRepository.observeAllReviews()
                .combine(mockCandidates) { reviews, candidates ->
                    val reviewedIds = reviews.map { it.candidateId }.toSet()
                    val pending = candidates.filterNot { it.id in reviewedIds }
                    Triple(pending, reviews, reviewRepository.getStats())
                }
                .collect { (pending, history, stats) ->
                    _uiState.update {
                        it.copy(
                            pendingCandidates = pending,
                            reviewHistory = history,
                            stats = stats
                        )
                    }
                }
        }
    }

    fun submitReview(candidate: ReviewCandidate, decision: ReviewDecision, note: String) {
        viewModelScope.launch {
            reviewRepository.submitReview(
                candidateId = candidate.id,
                candidateType = candidate.type.name,
                decision = decision.name,
                note = note,
                articleIds = candidate.articleIds,
                sourceIds = candidate.sourceIds,
                languages = candidate.languages,
                sharedEntities = candidate.sharedEntities,
                confidence = candidate.confidence,
                method = candidate.method,
                rationale = candidate.rationale,
                uncertaintyReasons = candidate.uncertaintyReasons
            )
        }
    }

    fun resetReviews() {
        viewModelScope.launch {
            reviewRepository.resetAllReviews()
        }
    }

    private fun createMockCandidates(): Flow<List<ReviewCandidate>> = flow {
        emit(listOf(
            ReviewCandidate(
                id = "cross-lang-1",
                type = CandidateType.CROSS_LANGUAGE,
                title = "Geneva Climate Summit Coverage",
                articleIds = listOf("article-bbc", "article-lemonde", "article-aljazeera"),
                sourceIds = listOf("bbc-demo", "lemonde-demo", "aljazeera-demo"),
                languages = listOf("en", "fr", "ar"),
                sharedEntities = listOf("event:geneva-climate-summit-2026", "location:geneva", "org:un"),
                confidence = "HIGH",
                method = "entity-overlap-v1",
                rationale = "High confidence candidate: 4 shared entities including event identifier across en, fr, ar. Likely same event.",
                uncertaintyReasons = listOf("Cross-language match requires editorial verification")
            ),
            ReviewCandidate(
                id = "syndication-1",
                type = CandidateType.SYNDICATION,
                title = "Wire Service Reprint",
                articleIds = listOf("article-bbc-wire", "article-guardian-wire"),
                sourceIds = listOf("bbc-demo", "guardian-demo"),
                languages = listOf("en", "en"),
                sharedEntities = null,
                confidence = "HIGH",
                method = "excerpt-fingerprint-v1",
                rationale = "High confidence suspected syndication: Exact title match and 95% excerpt overlap.",
                uncertaintyReasons = listOf("Suspected syndication requires editorial review to confirm wire copy")
            ),
            ReviewCandidate(
                id = "cluster-1",
                type = CandidateType.CLUSTER,
                title = "AI Regulation Framework Proposal",
                articleIds = listOf("article-nyt-ai", "article-ft-ai", "article-wired-ai"),
                sourceIds = listOf("nyt-demo", "ft-demo", "wired-demo"),
                languages = listOf("en", "en", "en"),
                sharedEntities = listOf("org:eu-commission", "org:us-congress", "topic:ai-regulation"),
                confidence = "MEDIUM",
                method = "title-similarity-v1",
                rationale = "Medium confidence cluster: Similar headlines and timing across 3 sources covering AI regulation proposals.",
                uncertaintyReasons = listOf("Similar timing may be coincidental", "Requires verification that articles cover the same specific proposal")
            )
        ))
    }
}

data class EditorialReviewUiState(
    val pendingCandidates: List<ReviewCandidate> = emptyList(),
    val reviewHistory: List<com.crosslens.app.data.local.entity.EditorialReviewEntity> = emptyList(),
    val stats: com.crosslens.app.data.repository.ReviewStats = com.crosslens.app.data.repository.ReviewStats(0, 0, 0)
)

data class ReviewCandidate(
    val id: String,
    val type: CandidateType,
    val title: String,
    val articleIds: List<String>,
    val sourceIds: List<String>,
    val languages: List<String>,
    val sharedEntities: List<String>?,
    val confidence: String?,
    val method: String,
    val rationale: String,
    val uncertaintyReasons: List<String>
)

enum class CandidateType {
    CROSS_LANGUAGE,
    SYNDICATION,
    CLUSTER
}

enum class ReviewDecision {
    APPROVED,
    REJECTED,
    DEFERRED
}
