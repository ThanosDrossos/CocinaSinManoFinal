package com.google.mediapipe.examples.gesturerecognizer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.mediapipe.examples.gesturerecognizer.Recipe
import com.google.mediapipe.examples.gesturerecognizer.Step
import com.google.mediapipe.examples.gesturerecognizer.databinding.FragmentRecipeOverviewBinding

class RecipeOverviewFragment : Fragment() {

    private var _binding: FragmentRecipeOverviewBinding? = null
    private val binding get() = _binding!!

    private val args: RecipeOverviewFragmentArgs by navArgs()
    private lateinit var recipe: Recipe

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRecipeOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recipe = args.recipe

        // Set recipe title
        binding.recipeTitleTextView.text = recipe.title

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
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
