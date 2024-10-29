package com.google.mediapipe.examples.gesturerecognizer

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RecipeAdapter(
    private val recipes: List<Recipe>,
    private val onItemClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeNameTextView: TextView = itemView.findViewById(R.id.recipeNameTextView)
        val recipeImageView: ImageView = itemView.findViewById(R.id.recipeImageView)
    }

    private var selectedPosition = RecyclerView.NO_POSITION

    fun setSelectedPosition(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(previousPosition)
        notifyItemChanged(selectedPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.recipeNameTextView.text = recipe.title

        Log.d("RecipeAdapter", "Binding recipe at position $position: ${recipe.title}")

        // Highlight selected item
        if (position == selectedPosition) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.selected_item))
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }

        // Load image from assets using Glide
        Glide.with(holder.itemView.context)
            .load("file:///android_asset/${recipe.imagePath}")
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error_image)
            .into(holder.recipeImageView)

        holder.itemView.setOnClickListener {
            onItemClick(recipe)
        }
    }

    override fun getItemCount(): Int {
        Log.d("RecipeAdapter", "getItemCount: ${recipes.size}")
        return recipes.size
    }

    init {
        Log.d("RecipeAdapter", "Adapter initialized with ${recipes.size} recipes.")
    }


}
