package com.google.mediapipe.examples.gesturerecognizer

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Step(
    val description: String,
    val imagePath: String
) : Parcelable