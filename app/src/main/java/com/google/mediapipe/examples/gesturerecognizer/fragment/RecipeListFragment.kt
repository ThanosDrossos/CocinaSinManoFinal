package com.google.mediapipe.examples.gesturerecognizer.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mediapipe.examples.gesturerecognizer.Recipe
import com.google.mediapipe.examples.gesturerecognizer.RecipeAdapter
import com.google.mediapipe.examples.gesturerecognizer.databinding.FragmentRecipeListBinding

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

    private fun loadRecipesFromJson(context: Context): List<Recipe> {
        val jsonString = context.assets.open("recipes.json").bufferedReader().use { it.readText() }
        val gson = Gson()
        val recipeType = object : TypeToken<List<Recipe>>() {}.type
        return gson.fromJson(jsonString, recipeType)
    }

}