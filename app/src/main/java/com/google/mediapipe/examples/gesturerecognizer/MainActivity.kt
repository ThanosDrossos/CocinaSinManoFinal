package com.google.mediapipe.examples.gesturerecognizer

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.mediapipe.examples.gesturerecognizer.databinding.ActivityMainBinding
import com.google.mediapipe.examples.gesturerecognizer.fragment.RecipeDetailsFragment
import com.google.mediapipe.examples.gesturerecognizer.fragment.RecipeDetailsFragmentDirections
import com.google.mediapipe.examples.gesturerecognizer.fragment.RecipeOverviewFragmentDirections

class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        // Remove automatic setup
        // activityMainBinding.navigation.setupWithNavController(navController)

        // Set up custom navigation handling
        activityMainBinding.navigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.recipeListFragment -> {
                    // Navigate to RecipeListFragment
                    val action = NavGraphDirections.actionGlobalRecipeListFragment()
                    navController.navigate(action)
                    true
                }
                R.id.aboutFragment -> {
                    // Navigate to AboutFragment
                    val action = NavGraphDirections.actionGlobalAboutFragment()
                    navController.navigate(action)
                    true
                }
                else -> false
            }
        }

        activityMainBinding.navigation.setOnNavigationItemReselectedListener {
            // ignore the reselection
        }
    }

    override fun onBackPressed() {
        val currentDestinationId = navController.currentDestination?.id

        when (currentDestinationId) {
            R.id.recipeDetailsFragment -> {
                // Navigate back to RecipeOverviewFragment
                val recipe = RecipeDetailsFragment().getCurrentRecipe()
                val action = RecipeDetailsFragmentDirections.actionRecipeDetailsFragmentToRecipeOverviewFragment(recipe)
                navController.navigate(action)
            }
            R.id.recipeOverviewFragment -> {
                // Navigate back to RecipeListFragment
                val action = RecipeOverviewFragmentDirections.actionRecipeOverviewFragmentToRecipeListFragment()
                navController.navigate(action)
            }
            else -> {
                super.onBackPressed()
            }
        }
    }
}
