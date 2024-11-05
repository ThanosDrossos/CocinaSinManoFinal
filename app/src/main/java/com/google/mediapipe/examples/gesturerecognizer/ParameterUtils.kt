package com.google.mediapipe.examples.gesturerecognizer

import android.content.Context
import com.google.mediapipe.examples.gesturerecognizer.fragment.AboutFragment

object ParameterUtils {

    data class GestureParameters(
        val minHandPresenceConfidence: Float,
        val minHandDetectionConfidence: Float,
        val minHandTrackingConfidence: Float,
        val cooldownPeriod: Long,
        val gestureRecognitionInterval: Long
    )

    fun getGestureParameters(context: Context): GestureParameters {
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
        val cooldownPeriod = sharedPreferences.getLong(
            AboutFragment.COOLDOWN_PERIOD_KEY,
            1000L
        )
        val gestureRecognitionInterval = sharedPreferences.getLong(
            AboutFragment.GESTURE_RECOGNITION_INTERVAL_KEY,
            2000L
        )

        return GestureParameters(
            minHandPresenceConfidence,
            minHandDetectionConfidence,
            minHandTrackingConfidence,
            cooldownPeriod,
            gestureRecognitionInterval
        )
    }
}
