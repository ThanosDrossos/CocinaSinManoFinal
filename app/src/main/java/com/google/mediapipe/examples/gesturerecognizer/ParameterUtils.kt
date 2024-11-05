package com.google.mediapipe.examples.gesturerecognizer

import android.content.Context
import com.google.mediapipe.examples.gesturerecognizer.fragment.AboutFragment

object ParameterUtils {

    data class ConfidenceParameters(
        val minHandPresenceConfidence: Float,
        val minHandDetectionConfidence: Float,
        val minHandTrackingConfidence: Float
    )

    fun getConfidenceParameters(context: Context): ConfidenceParameters {
        val sharedPreferences = context.getSharedPreferences(
            AboutFragment.SHARED_PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val minHandPresenceConfidence = sharedPreferences.getFloat(
            AboutFragment.MIN_HAND_PRESENCE_CONFIDENCE_KEY,
            0.6f
        )
        val minHandDetectionConfidence = sharedPreferences.getFloat(
            AboutFragment.MIN_HAND_DETECTION_CONFIDENCE_KEY,
            0.6f
        )
        val minHandTrackingConfidence = sharedPreferences.getFloat(
            AboutFragment.MIN_HAND_TRACKING_CONFIDENCE_KEY,
            0.6f
        )

        return ConfidenceParameters(
            minHandPresenceConfidence,
            minHandDetectionConfidence,
            minHandTrackingConfidence
        )
    }
}
