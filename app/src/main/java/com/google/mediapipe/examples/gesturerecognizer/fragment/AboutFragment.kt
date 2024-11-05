package com.google.mediapipe.examples.gesturerecognizer.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.mediapipe.examples.gesturerecognizer.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        const val SHARED_PREFS_NAME = "GestureRecognizerPrefs"
        const val MIN_HAND_PRESENCE_CONFIDENCE_KEY = "minHandPresenceConfidence"
        const val MIN_HAND_DETECTION_CONFIDENCE_KEY = "minHandDetectionConfidence"
        const val MIN_HAND_TRACKING_CONFIDENCE_KEY = "minHandTrackingConfidence"
        const val COOLDOWN_PERIOD_KEY = "cooldownPeriod"
        const val GESTURE_RECOGNITION_INTERVAL_KEY = "gestureRecognitionInterval"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appVersionTextView.text = "Versión de la Aplicación: 0.1.2"
        binding.developerInfoTextView.text = "Desarrollado por Thanos Drossos"

        sharedPreferences = requireContext().getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)

        // Initialize SeekBars and TextViews with current values
        initializeGestureParameters()

        // Set up SeekBar listeners
        setUpSeekBarListeners()

        // Handle Save button click
        binding.saveParametersButton.setOnClickListener {
            saveGestureParameters()
        }
    }

    private fun initializeGestureParameters() {
        // Get current values or set defaults
        val minHandPresenceConfidence = sharedPreferences.getFloat(MIN_HAND_PRESENCE_CONFIDENCE_KEY, 0.6f)
        val minHandDetectionConfidence = sharedPreferences.getFloat(MIN_HAND_DETECTION_CONFIDENCE_KEY, 0.6f)
        val minHandTrackingConfidence = sharedPreferences.getFloat(MIN_HAND_TRACKING_CONFIDENCE_KEY, 0.6f)
        val cooldownPeriod = sharedPreferences.getLong(COOLDOWN_PERIOD_KEY, 1000L)
        val gestureRecognitionInterval = sharedPreferences.getLong(GESTURE_RECOGNITION_INTERVAL_KEY, 2000L)

        // Set SeekBar positions
        binding.minHandPresenceConfidenceSeekBar.progress = (minHandPresenceConfidence * 100).toInt()
        binding.minHandDetectionConfidenceSeekBar.progress = (minHandDetectionConfidence * 100).toInt()
        binding.minHandTrackingConfidenceSeekBar.progress = (minHandTrackingConfidence * 100).toInt()
        binding.cooldownPeriodSeekBar.progress = (cooldownPeriod / 100).toInt() // Assuming max is 5000ms
        binding.gestureRecognitionIntervalSeekBar.progress = (gestureRecognitionInterval / 100).toInt() // Assuming max is 5000ms

        // Set TextView values
        binding.minHandPresenceConfidenceValue.text = String.format("%.2f", minHandPresenceConfidence)
        binding.minHandDetectionConfidenceValue.text = String.format("%.2f", minHandDetectionConfidence)
        binding.minHandTrackingConfidenceValue.text = String.format("%.2f", minHandTrackingConfidence)
        binding.cooldownPeriodValue.text = "${cooldownPeriod}ms"
        binding.gestureRecognitionIntervalValue.text = "${gestureRecognitionInterval}ms"
    }

    private fun setUpSeekBarListeners() {
        binding.minHandPresenceConfidenceSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress / 100f
                binding.minHandPresenceConfidenceValue.text = String.format("%.2f", value)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        })

        binding.minHandDetectionConfidenceSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress / 100f
                binding.minHandDetectionConfidenceValue.text = String.format("%.2f", value)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        })

        binding.minHandTrackingConfidenceSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress / 100f
                binding.minHandTrackingConfidenceValue.text = String.format("%.2f", value)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        })

        binding.cooldownPeriodSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress * 100L
                binding.cooldownPeriodValue.text = "${value}ms"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        })

        binding.gestureRecognitionIntervalSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress * 100L
                binding.gestureRecognitionIntervalValue.text = "${value}ms"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        })
    }

    private fun saveGestureParameters() {
        val minHandPresenceConfidence = binding.minHandPresenceConfidenceSeekBar.progress / 100f
        val minHandDetectionConfidence = binding.minHandDetectionConfidenceSeekBar.progress / 100f
        val minHandTrackingConfidence = binding.minHandTrackingConfidenceSeekBar.progress / 100f
        val cooldownPeriod = binding.cooldownPeriodSeekBar.progress * 100L
        val gestureRecognitionInterval = binding.gestureRecognitionIntervalSeekBar.progress * 100L

        with(sharedPreferences.edit()) {
            putFloat(MIN_HAND_PRESENCE_CONFIDENCE_KEY, minHandPresenceConfidence)
            putFloat(MIN_HAND_DETECTION_CONFIDENCE_KEY, minHandDetectionConfidence)
            putFloat(MIN_HAND_TRACKING_CONFIDENCE_KEY, minHandTrackingConfidence)
            putLong(COOLDOWN_PERIOD_KEY, cooldownPeriod)
            putLong(GESTURE_RECOGNITION_INTERVAL_KEY, gestureRecognitionInterval)
            apply()
        }

        Toast.makeText(requireContext(), "Parámetros guardados", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
