package com.google.mediapipe.examples.gesturerecognizer

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecipeStep(
    val imagePath: String,
    val description: String
) : Parcelable