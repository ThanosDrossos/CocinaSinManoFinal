package com.google.mediapipe.examples.gesturerecognizer.fragment

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mediapipe.examples.gesturerecognizer.GestureRecognizerHelper
import com.google.mediapipe.examples.gesturerecognizer.ParameterUtils
import com.google.mediapipe.examples.gesturerecognizer.Recipe
import com.google.mediapipe.examples.gesturerecognizer.RecipeAdapter
import com.google.mediapipe.examples.gesturerecognizer.databinding.FragmentRecipeListBinding
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.concurrent.Executors

class RecipeListFragment : Fragment(), GestureRecognizerHelper.GestureRecognizerListener {

    private var _binding: FragmentRecipeListBinding? = null
    private val binding get() = _binding!!
    private lateinit var recipes: List<Recipe>
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var gestureRecognizerHelper: GestureRecognizerHelper

    private var isFragmentActive = false

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    // Gesture recognition variables
    private var isGestureRecognitionInProgress = false
    private var gestureProgress = 0
    private var startTime: Long = 0L
    private val gestureTimestamps = mutableListOf<Pair<Long, String>>()
    private val GESTURE_RECOGNITION_INTERVAL = 2000L // 2 seconds

    private var isCooldownActive = false

    // For recipe selection
    private var currentRecipeIndex = 0

    companion object {
        private const val TAG = "RecipeListFragment"
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
        private val RECOGNIZED_GESTURES = setOf("Thumb_Up", "Thumb_Down", "Open_Palm")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRecipeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recipes = loadRecipesFromJson(requireContext())
        Log.d(TAG, "Number of recipes loaded: ${recipes.size}")

        binding.recipeRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        recipeAdapter = RecipeAdapter(recipes) { recipe ->
            // Navigate to RecipeOverviewFragment with the selected recipe
            val action =
                RecipeListFragmentDirections.actionRecipeListFragmentToRecipeOverviewFragment(recipe)
            findNavController().navigate(action)
        }

        binding.recipeRecyclerView.adapter = recipeAdapter

        val confidenceParameters = ParameterUtils.getConfidenceParameters(requireContext())

        // Initialize GestureRecognizerHelper
        gestureRecognizerHelper = GestureRecognizerHelper(
            context = requireContext(),
            gestureRecognizerListener = this,
            runningMode = RunningMode.LIVE_STREAM,
            minHandPresenceConfidence = confidenceParameters.minHandPresenceConfidence,
            minHandTrackingConfidence = confidenceParameters.minHandTrackingConfidence,
            minHandDetectionConfidence = confidenceParameters.minHandDetectionConfidence
        )

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

        // Highlight the first recipe initially
        selectRecipeAtIndex(currentRecipeIndex)

        isFragmentActive = true
    }

    private fun loadRecipesFromJson(context: Context): List<Recipe> {
        Log.d(TAG, "loadRecipesFromJson() called")
        return try {
            val jsonString = context.assets.open("recipes.json").bufferedReader().use { it.readText() }
            val gson = Gson()
            val recipeType = object : TypeToken<List<Recipe>>() {}.type
            gson.fromJson(jsonString, recipeType)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading recipes.json", e)
            emptyList()
        }
    }

    private fun selectRecipeAtIndex(index: Int) {
        if (index in recipes.indices) {
            currentRecipeIndex = index
            recipeAdapter.setSelectedPosition(currentRecipeIndex)
            binding.recipeRecyclerView.scrollToPosition(currentRecipeIndex)
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
        imageAnalyzer?.clearAnalyzer()
        imageAnalyzer = null
        cameraExecutor.shutdownNow()
        gestureRecognizerHelper.clearGestureRecognizer()
        gestureTimestamps.clear()
        isGestureRecognitionInProgress = false
        isCooldownActive = false
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
        if (!isFragmentActive || isCooldownActive) return
        val gestureResults = resultBundle.results.firstOrNull()
        val gestureClassifierResult = gestureResults?.gestures()?.firstOrNull()
        val category = gestureClassifierResult?.firstOrNull()
        val gestureName = category?.categoryName() ?: "None"

        val currentTime = System.currentTimeMillis()

        // Update the gesture timestamps list
        gestureTimestamps.add(Pair(currentTime, gestureName))

        // Remove old entries beyond 2 seconds
        gestureTimestamps.removeAll { it.first < currentTime - GESTURE_RECOGNITION_INTERVAL }

        // Get the most recognized gesture in the last 100 ms
        val mostRecognizedGesture = getMostRecognizedGestureInLast100ms(currentTime)

        Log.d(TAG, "Gesto Reconocido: $mostRecognizedGesture")

        activity?.runOnUiThread {
            if (_binding == null) {
                Log.d(TAG, "Binding is null")
                return@runOnUiThread
            }

            // Update the UI with the action name
            val actionName = getActionName(mostRecognizedGesture)
            binding.detectedGestureTextView.text = actionName

            // Update progress bar
            if (!isGestureRecognitionInProgress && mostRecognizedGesture in RECOGNIZED_GESTURES) {
                // Start gesture recognition
                isGestureRecognitionInProgress = true
                startTime = currentTime
                gestureProgress = 0

                // Initialize the progress bar
                binding.gestureProgressBar.max = GESTURE_RECOGNITION_INTERVAL.toInt()
                setProgressBarColor(Color.GREEN)
            }

            if (isGestureRecognitionInProgress) {
                if (mostRecognizedGesture in RECOGNIZED_GESTURES) {
                    // Increase the progress
                    gestureProgress += 100
                    if (gestureProgress > GESTURE_RECOGNITION_INTERVAL) {
                        gestureProgress = GESTURE_RECOGNITION_INTERVAL.toInt()
                    }
                } else if (mostRecognizedGesture == "None") {
                    // Decrease the progress
                    gestureProgress -= 100
                    if (gestureProgress <= 0) {
                        gestureProgress = 0
                        resetGestureRecognition()
                    }
                }
                binding.gestureProgressBar.progress = gestureProgress

                // If gesture is recognized sufficiently
                if (gestureProgress >= GESTURE_RECOGNITION_INTERVAL / 2) {
                    // Gesture recognized sufficiently
                    performGestureAction(mostRecognizedGesture)
                }
            } else {
                // If not in recognition progress, set progress bar color to grey
                setProgressBarColor(Color.GRAY)
            }
        }
    }

    private fun getMostRecognizedGestureInLast100ms(currentTime: Long): String {
        // Remove old entries beyond 100 ms
        gestureTimestamps.removeAll { it.first < currentTime - 100 }

        // Count the occurrences
        val counts = gestureTimestamps.groupingBy { it.second }.eachCount()

        // Return the gesture with the highest count
        return counts.maxByOrNull { it.value }?.key ?: "None"
    }

    private fun getActionName(gestureName: String): String {
        return when (gestureName) {
            "Thumb_Up" -> "Receta anterior"
            "Thumb_Down" -> "Siguiente receta"
            "Open_Palm" -> "Seleccionar receta"
            else -> "Gesto no reconocido"
        }
    }

    private fun performGestureAction(gestureName: String) {
        isGestureRecognitionInProgress = false
        isCooldownActive = true

        // Animate the progress bar to fill quickly
        val progressAnimator = ObjectAnimator.ofInt(
            binding.gestureProgressBar,
            "progress",
            gestureProgress,
            GESTURE_RECOGNITION_INTERVAL.toInt()
        )
        progressAnimator.duration = 300 // Animate quickly
        progressAnimator.start()

        // Change progress bar color to indicate success
        setProgressBarColor(Color.BLUE)

        // Display the action name
        val actionName = getActionName(gestureName)
        binding.detectedGestureTextView.text = actionName

        // Delay performing the action by 1 second
        Handler(Looper.getMainLooper()).postDelayed({
            // Perform the action corresponding to the gesture
            when (gestureName) {
                "Thumb_Down" -> {
                    // Move to next recipe
                    if (currentRecipeIndex < recipes.size - 1) {
                        currentRecipeIndex += 1
                        selectRecipeAtIndex(currentRecipeIndex)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Ya es la última receta.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                "Thumb_Up" -> {
                    // Move to previous recipe
                    if (currentRecipeIndex > 0) {
                        currentRecipeIndex -= 1
                        selectRecipeAtIndex(currentRecipeIndex)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Ya es la primera receta.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                "Open_Palm" -> {
                    // Select current recipe and navigate to RecipeOverviewFragment
                    val selectedRecipe = recipes[currentRecipeIndex]
                    val action =
                        RecipeListFragmentDirections.actionRecipeListFragmentToRecipeOverviewFragment(
                            selectedRecipe
                        )
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

            // After performing the action, reset gesture recognition
            resetGestureRecognition()
            isCooldownActive = false
        }, 1000) // Delay of 1 second
    }

    private fun resetGestureRecognition() {
        isGestureRecognitionInProgress = false
        startTime = 0L
        gestureTimestamps.clear()
        gestureProgress = 0

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
