package com.google.mediapipe.examples.gesturerecognizer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class RecipeListFragment : Fragment() {

    private lateinit var binding: FragmentRecipeListBinding
    private lateinit var recipes: List<Recipe>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecipeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recipes = loadRecipesFromJson(requireContext()) // Load recipes from JSON

        val recipeAdapter = RecipeAdapter(recipes) { recipe ->
            // Navigate to RecipeDetailsFragment with the selected recipe
            val action =
                RecipeListFragmentDirections.actionRecipeListFragmentToRecipeDetailsFragment(recipe)
            findNavController().navigate(action)
        }
        binding.recipeRecyclerView.adapter = recipeAdapter
    }
}