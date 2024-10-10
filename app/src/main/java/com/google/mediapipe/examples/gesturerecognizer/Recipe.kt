package com.google.mediapipe.examples.gesturerecognizer

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Recipe(
    val title: String,
    val ingredients: List<String>,
    val steps: List<Step>,
    val imagePath: String
) : Parcelable