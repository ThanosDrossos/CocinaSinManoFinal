package com.google.mediapipe.examples.gesturerecognizer.fragment

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.mediapipe.examples.gesturerecognizer.GestureRecognizerHelper
import com.google.mediapipe.examples.gesturerecognizer.R
import com.google.mediapipe.examples.gesturerecognizer.Recipe
import com.google.mediapipe.examples.gesturerecognizer.databinding.FragmentRecipeDetailsBinding
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.concurrent.Executors

class RecipeDetailsFragment : Fragment(), GestureRecognizerHelper.GestureRecognizerListener {

    private var _binding: FragmentRecipeDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var recipe: Recipe
    private lateinit var gestureRecognizerHelper: GestureRecognizerHelper

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    private var currentStepIndex = 0

    private var isFragmentActive = false

    // Gesture recognition variables
    private var isGestureRecognitionInProgress = false
    private var gestureCountsFirstInterval = mutableMapOf<String, Int>()
    private var gestureCountsLastInterval = mutableMapOf<String, Int>()
    private var totalGestureCounts = mutableMapOf<String, Int>()
    private val GESTURE_RECOGNITION_INTERVAL = 3000L // Changed from 5000L to 3000L
    private var startTime: Long = 0L
    private var gestureRecognitionHandler: Handler? = null

    private val cooldownDuration = 1000L // 1 second in milliseconds

    companion object {
        private const val TAG = "RecipeDetailsFragment"
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
        private val RECOGNIZED_GESTURES = setOf("Thumb_Up", "Thumb_Down", "Closed_Fist")
    }

    private val args: RecipeDetailsFragmentArgs by navArgs()

    private val gestureRecognitionRunnable = Runnable {
        // This runs after 3 seconds
        handleGestureAction()
        resetGestureRecognition()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRecipeDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recipe = args.recipe

        // Display the first step
        displayCurrentStep()

        // Initialize GestureRecognizerHelper
        gestureRecognizerHelper = GestureRecognizerHelper(
            context = requireContext(),
            gestureRecognizerListener = this,
            runningMode = RunningMode.LIVE_STREAM,
            minHandPresenceConfidence = 0.6f,
            minHandTrackingConfidence = 0.6f,
            minHandDetectionConfidence = 0.6f
        )

        // Initialize the gesture recognition handler
        gestureRecognitionHandler = Handler(Looper.getMainLooper())

        isFragmentActive = true

        // Check for camera permission and start camera preview
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCameraPreview()
        } else {
            // Request permission if not already granted
            requestCameraPermission()
        }

        // Set up navigation arrow buttons
        binding.previousStepButton.setOnClickListener {
            if (currentStepIndex > 0) {
                currentStepIndex--
                displayCurrentStep()
            }
        }

        binding.nextStepButton.setOnClickListener {
            if (currentStepIndex < recipe.steps.size - 1) {
                currentStepIndex++
                displayCurrentStep()
            }
        }

    }

    private fun displayCurrentStep() {
        val currentStep = recipe.steps[currentStepIndex]

        // Set step description
        binding.stepDescriptionTextView.text = currentStep.description

        // Update step number
        binding.stepNumberTextView.text = "Paso ${currentStepIndex + 1} de ${recipe.steps.size}"

        // Load step image using Glide
        Glide.with(this)
            .load("file:///android_asset/${currentStep.imagePath}")
            .into(binding.stepImageView)

        updateNavigationArrows()
    }

    private fun updateNavigationArrows() {
        val enabledColor = ContextCompat.getColor(requireContext(), R.color.black)
        val disabledColor = ContextCompat.getColor(requireContext(), R.color.gray)

        // Previous Step Button
        if (currentStepIndex > 0) {
            binding.previousStepButton.isEnabled = true
            binding.previousStepButton.setColorFilter(enabledColor)
        } else {
            binding.previousStepButton.isEnabled = false
            binding.previousStepButton.setColorFilter(disabledColor)
        }

        // Next Step Button
        if (currentStepIndex < recipe.steps.size - 1) {
            binding.nextStepButton.isEnabled = true
            binding.nextStepButton.setColorFilter(enabledColor)
        } else {
            binding.nextStepButton.isEnabled = false
            binding.nextStepButton.setColorFilter(disabledColor)
        }
    }

    private fun startCameraPreview() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: return

        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

        // Preview
        val preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .build()
            .also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor) { imageProxy ->
                    gestureRecognizerHelper.recognizeLiveStream(imageProxy)
                }
            }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                viewLifecycleOwner, cameraSelector, preview, imageAnalyzer
            )
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun requestCameraPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray,
    ) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty()) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission is granted. Continue the action or workflow in your app.
                startCameraPreview()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Se requiere permiso de cámara para el reconocimiento de gestos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isFragmentActive = false
        // Shut down our background executor
        cameraProvider?.unbindAll()
        cameraExecutor.shutdownNow()
        gestureRecognizerHelper.clearGestureRecognizer()
        gestureRecognitionHandler?.removeCallbacksAndMessages(null)
        gestureRecognitionHandler = null
        imageAnalyzer?.clearAnalyzer()
        imageAnalyzer = null
        _binding = null
    }

    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            Toast.makeText(
                requireContext(),
                "Error de reconocimiento de gestos: $error",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onResults(resultBundle: GestureRecognizerHelper.ResultBundle) {
        if (!isFragmentActive) return
        val gestureResults = resultBundle.results.firstOrNull()
        val gestureClassifierResult = gestureResults?.gestures()?.firstOrNull()
        val category = gestureClassifierResult?.firstOrNull()
        val gestureName = category?.categoryName() ?: "None"

        Log.d(TAG, "Gesto Reconocido: $gestureName")

        activity?.runOnUiThread {
            if(_binding==null){
                Log.d(TAG, "Binding is null")
                return@runOnUiThread
            }
            // Update the UI with the detected gesture
            binding.detectedGestureTextView.text = "Gesto detectado: $gestureName"

            // Only proceed if recognized gesture is in our set
            if (gestureName in RECOGNIZED_GESTURES) {
                // Handle gesture recognition logic
                if (!isGestureRecognitionInProgress) {
                    // Start gesture recognition
                    isGestureRecognitionInProgress = true
                    startTime = System.currentTimeMillis()

                    // Initialize counts
                    gestureCountsFirstInterval.clear()
                    gestureCountsLastInterval.clear()
                    totalGestureCounts.clear()

                    // Start the 3-second timer
                    gestureRecognitionHandler?.postDelayed(
                        gestureRecognitionRunnable,
                        GESTURE_RECOGNITION_INTERVAL
                    )

                    // Update progress bar color to green
                    setProgressBarColor(Color.GREEN)

                    // Update progress bar
                    binding.gestureProgressBar.max = GESTURE_RECOGNITION_INTERVAL.toInt()
                    val progressAnimator = ObjectAnimator.ofInt(
                        binding.gestureProgressBar,
                        "progress",
                        0,
                        GESTURE_RECOGNITION_INTERVAL.toInt()
                    )
                    progressAnimator.duration = GESTURE_RECOGNITION_INTERVAL
                    progressAnimator.start()
                }

                if (isGestureRecognitionInProgress) {
                    // We are within the 3-second interval
                    val elapsedTime = System.currentTimeMillis() - startTime

                    // Update total gesture counts
                    totalGestureCounts[gestureName] =
                        totalGestureCounts.getOrDefault(gestureName, 0) + 1

                    // Update counts for first 1 second and last 1 second
                    if (elapsedTime <= 1000L) {
                        // First 1 second
                        gestureCountsFirstInterval[gestureName] =
                            gestureCountsFirstInterval.getOrDefault(gestureName, 0) + 1
                    } else if (elapsedTime >= 2000L) {
                        // Last 1 second
                        gestureCountsLastInterval[gestureName] =
                            gestureCountsLastInterval.getOrDefault(gestureName, 0) + 1
                    }
                }
            } else {
                // If gesture is not recognized or "None", reset the progress bar color to grey
                if (!isGestureRecognitionInProgress) {
                    setProgressBarColor(Color.GRAY)
                }
            }
        }
    }

    private fun handleGestureAction() {
        // Determine the gesture recognized most over the 3 seconds
        val mostRecognizedGesture = totalGestureCounts.maxByOrNull { it.value }?.key ?: "None"

        // Get counts for 'None' and the most recognized gesture
        val noneCount = totalGestureCounts["None"] ?: 0
        val maxGestureCount = totalGestureCounts[mostRecognizedGesture] ?: 0

        // If 'None' was recognized more than any other gesture, abort
        if (mostRecognizedGesture == "None" || noneCount >= maxGestureCount) {
            Toast.makeText(
                requireContext(),
                "Gesto no reconocido.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Check if the most recognized gesture was detected at least 3 times in both intervals
        val countFirstInterval = gestureCountsFirstInterval[mostRecognizedGesture] ?: 0
        val countLastInterval = gestureCountsLastInterval[mostRecognizedGesture] ?: 0

        if (countFirstInterval >= 3 && countLastInterval >= 3) {
            when (mostRecognizedGesture) {
                "Thumb_Up" -> {
                    // Move to next step
                    if (currentStepIndex < recipe.steps.size - 1) {
                        currentStepIndex += 1
                        displayCurrentStep()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Ya es el último paso.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                "Thumb_Down" -> {
                    // Move to previous step
                    if (currentStepIndex > 0) {
                        currentStepIndex -= 1
                        displayCurrentStep()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Ya es el primer paso.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                "Closed_Fist" -> {
                    // Navigate back to recipe overview fragment
                    val action = RecipeDetailsFragmentDirections.actionRecipeDetailsFragmentToRecipeOverviewFragment(recipe)
                    findNavController().navigate(action)
                }
                else -> {
                    Toast.makeText(
                        requireContext(),
                        "Acción de gesto no reconocida.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            // Not enough gestures detected in both intervals
            Toast.makeText(
                requireContext(),
                "Gesto reconocido muy pocas veces. Intenta de nuevo.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun resetGestureRecognition() {
        isGestureRecognitionInProgress = false
        startTime = 0L
        gestureCountsFirstInterval.clear()
        gestureCountsLastInterval.clear()
        totalGestureCounts.clear()

        // Reset progress bar and detected gesture text
        binding.gestureProgressBar.progress = 0
        binding.detectedGestureTextView.text = ""
        setProgressBarColor(Color.GRAY)
    }

    private fun setProgressBarColor(color: Int) {
        val drawable = binding.gestureProgressBar.progressDrawable
        drawable?.let {
            DrawableCompat.setTint(it, color)
            binding.gestureProgressBar.progressDrawable = it
        }
    }
}
