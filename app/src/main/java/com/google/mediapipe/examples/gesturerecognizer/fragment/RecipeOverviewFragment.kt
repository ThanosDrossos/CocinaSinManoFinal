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
import androidx.activity.OnBackPressedCallback
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
import com.google.mediapipe.examples.gesturerecognizer.GestureRecognizerHelper
import com.google.mediapipe.examples.gesturerecognizer.ParameterUtils
import com.google.mediapipe.examples.gesturerecognizer.Recipe
import com.google.mediapipe.examples.gesturerecognizer.Step
import com.google.mediapipe.examples.gesturerecognizer.databinding.FragmentRecipeOverviewBinding
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.concurrent.Executors

class RecipeOverviewFragment : Fragment(), GestureRecognizerHelper.GestureRecognizerListener {

    private var _binding: FragmentRecipeOverviewBinding? = null
    private val binding get() = _binding!!

    private var isFragmentActive = false

    private val args: RecipeOverviewFragmentArgs by navArgs()
    private lateinit var recipe: Recipe

    private lateinit var gestureRecognizerHelper: GestureRecognizerHelper

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    // Gesture recognition variables
    private var isGestureRecognitionInProgress = false
    private var gestureProgress = 0
    private var startTime: Long = 0L
    private val gestureTimestamps = mutableListOf<Pair<Long, String>>()
    private val GESTURE_RECOGNITION_INTERVAL = 2000L // 2 seconds

    @Volatile
    private var isCooldownActive = false
    private val cooldownHandler = Handler(Looper.getMainLooper())
    private var cooldownRunnable: Runnable? = null
    private val COOLDOWN_PERIOD = 1000L // 3 seconds

    private fun startCooldown() {
        isCooldownActive = true
        cooldownRunnable?.let { cooldownHandler.removeCallbacks(it) }

        // Create a new cooldown runnable
        cooldownRunnable = Runnable {
            isCooldownActive = false
        }

        // Post the cooldown runnable with delay
        cooldownHandler.postDelayed(cooldownRunnable!!, COOLDOWN_PERIOD)
    }

    companion object {
        private const val TAG = "RecipeOverviewFragment"
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
        private val RECOGNIZED_GESTURES = setOf("Open_Palm", "Closed_Fist")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize recipe as early as possible
        //recipe = arguments?.getParcelable("recipe") ?: throw IllegalArgumentException("Recipe not found")

        // Handle back button
        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Navigate back to RecipeOverviewFragment with the recipe
                val action = RecipeOverviewFragmentDirections.actionRecipeOverviewFragmentToRecipeListFragment()
                findNavController().navigate(action)
            }
        })
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRecipeOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recipe = args.recipe

        // Set recipe title
        binding.recipeTitleTextView.text = recipe.title
        binding.recipeDescriptionTextView.text = recipe.subtitle

        // Display ingredients
        binding.ingredientsTextView.text = recipe.ingredients.joinToString(separator = "\n") { "- $it" }

        // Display steps
        val stepsText = recipe.steps.mapIndexed { index: Int, step: Step ->
            "${index + 1}. ${step.description}"
        }.joinToString(separator = "\n\n")
        binding.stepsTextView.text = stepsText

        // Set up Start Cooking button
        binding.startCookingButton.setOnClickListener {
            val action = RecipeOverviewFragmentDirections
                .actionRecipeOverviewFragmentToRecipeDetailsFragment(recipe)
            findNavController().navigate(action)
        }

        // Set up Back button
        binding.backButton.setOnClickListener {
            // Navigate back to RecipeListFragment
            val action = RecipeOverviewFragmentDirections.actionRecipeOverviewFragmentToRecipeListFragment()
            findNavController().navigate(action)
        }

        val confidenceParameters = ParameterUtils.getConfidenceParameters(requireContext())

        gestureRecognizerHelper = GestureRecognizerHelper(
            context = requireContext(),
            gestureRecognizerListener = this,
            runningMode = RunningMode.LIVE_STREAM,
            minHandPresenceConfidence = confidenceParameters.minHandPresenceConfidence,
            minHandTrackingConfidence = confidenceParameters.minHandTrackingConfidence,
            minHandDetectionConfidence = confidenceParameters.minHandDetectionConfidence
        )

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
        cooldownRunnable?.let { cooldownHandler.removeCallbacks(it) }
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
            if (!isFragmentActive || _binding == null) {
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
            "Open_Palm" -> "Iniciar cocinado"
            "Closed_Fist" -> "Volver a la lista"
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

        // Change progress bar color to dark green to indicate success
        setProgressBarColor(Color.parseColor("#008000"))

        startCooldown()

        // Display the action name
        val actionName = getActionName(gestureName)
        binding.detectedGestureTextView.text = actionName

        // Delay performing the action by 1 second
        Handler(Looper.getMainLooper()).postDelayed({
            // Perform the action corresponding to the gesture
            when (gestureName) {
                "Open_Palm" -> {
                    // Start cooking, navigate to RecipeDetailsFragment
                    val action = RecipeOverviewFragmentDirections
                        .actionRecipeOverviewFragmentToRecipeDetailsFragment(recipe)
                    findNavController().navigate(action)
                }
                "Closed_Fist" -> {
                    // Navigate back to RecipeListFragment
                    val action = RecipeOverviewFragmentDirections.actionRecipeOverviewFragmentToRecipeListFragment()
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
        }, 1000)
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
