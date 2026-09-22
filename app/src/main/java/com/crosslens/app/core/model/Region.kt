package com.crosslens.app.core.model

data class Region(
    val id: String,
    val name: String,
    val countryCodes: List<String>
)

data class Country(
    val code: String, // ISO 3166-1 alpha-2
    val name: String
)

data class Topic(
    val id: String,
    val name: String
)
