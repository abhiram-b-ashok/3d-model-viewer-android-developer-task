package com.example.androiddevelopertask.model

data class ModelKind(
    val id: String,
    val displayName: String,
    val assetPath: String,
)

object ModelCatalog {
    val all: List<ModelKind> = listOf(
        ModelKind("kangaroo", "Kangaroo", "models/tiny_planet_friends_3d-kangaroo-4186.glb"),
        ModelKind("mouse", "Mouse", "models/tiny_planet_friends_3d-mouse-4188.glb"),
        ModelKind("sloth", "Sloth", "models/tiny_planet_friends_3d-sloth-4187.glb"),
    )
}

enum class InteractionMode { Normal, Interact }

data class ModelEntry(
    val id: Long,
    val kind: ModelKind,
    val initialX: Float,
    val initialY: Float,
)
