package com.google.mediapipe.examples.gesturerecognizer

data class Recipe(
    val title: String,
    val ingredients: List<String>,
    val steps: List<Step>,
    val imagePath: String
)