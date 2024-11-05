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
        initializeConfidenceParameters()

        // Set up SeekBar listeners
        setUpSeekBarListeners()

        // Handle Save button click
        binding.saveParametersButton.setOnClickListener {
            saveConfidenceParameters()
        }
    }

    private fun initializeConfidenceParameters() {
        // Get current values or set defaults
        val minHandPresenceConfidence = sharedPreferences.getFloat(MIN_HAND_PRESENCE_CONFIDENCE_KEY, 0.6f)
        val minHandDetectionConfidence = sharedPreferences.getFloat(MIN_HAND_DETECTION_CONFIDENCE_KEY, 0.6f)
        val minHandTrackingConfidence = sharedPreferences.getFloat(MIN_HAND_TRACKING_CONFIDENCE_KEY, 0.6f)

        // Set SeekBar positions
        binding.minHandPresenceConfidenceSeekBar.progress = (minHandPresenceConfidence * 100).toInt()
        binding.minHandDetectionConfidenceSeekBar.progress = (minHandDetectionConfidence * 100).toInt()
        binding.minHandTrackingConfidenceSeekBar.progress = (minHandTrackingConfidence * 100).toInt()

        // Set TextView values
        binding.minHandPresenceConfidenceValue.text = minHandPresenceConfidence.toString()
        binding.minHandDetectionConfidenceValue.text = minHandDetectionConfidence.toString()
        binding.minHandTrackingConfidenceValue.text = minHandTrackingConfidence.toString()
    }

    private fun setUpSeekBarListeners() {
        binding.minHandPresenceConfidenceSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress / 100f
                binding.minHandPresenceConfidenceValue.text = String.format("%.2f", value)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // No action needed
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // No action needed
            }
        })

        binding.minHandDetectionConfidenceSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress / 100f
                binding.minHandDetectionConfidenceValue.text = String.format("%.2f", value)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // No action needed
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // No action needed
            }
        })

        binding.minHandTrackingConfidenceSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress / 100f
                binding.minHandTrackingConfidenceValue.text = String.format("%.2f", value)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // No action needed
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // No action needed
            }
        })
    }

    private fun saveConfidenceParameters() {
        val minHandPresenceConfidence = binding.minHandPresenceConfidenceSeekBar.progress / 100f
        val minHandDetectionConfidence = binding.minHandDetectionConfidenceSeekBar.progress / 100f
        val minHandTrackingConfidence = binding.minHandTrackingConfidenceSeekBar.progress / 100f

        with(sharedPreferences.edit()) {
            putFloat(MIN_HAND_PRESENCE_CONFIDENCE_KEY, minHandPresenceConfidence)
            putFloat(MIN_HAND_DETECTION_CONFIDENCE_KEY, minHandDetectionConfidence)
            putFloat(MIN_HAND_TRACKING_CONFIDENCE_KEY, minHandTrackingConfidence)
            apply()
        }

        Toast.makeText(requireContext(), "Parámetros guardados", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
