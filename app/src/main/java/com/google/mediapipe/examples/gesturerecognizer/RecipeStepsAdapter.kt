package com.google.mediapipe.examples.gesturerecognizer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RecipeStepsAdapter(private val steps: List<Step>) :
    RecyclerView.Adapter<RecipeStepsAdapter.RecipeStepViewHolder>() {

    class RecipeStepViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val stepImageView: ImageView = itemView.findViewById(R.id.stepImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeStepViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_step, parent, false)
        return RecipeStepViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecipeStepViewHolder, position: Int) {
        val step = steps[position]
        holder.descriptionTextView.text = step.description

        // Load image from assets or drawable
        // Load image from assets using Glide
        Glide.with(holder.itemView.context)
            .load("file:///android_asset/${step.imagePath}")
            .into(holder.stepImageView)
    }

    override fun getItemCount(): Int = steps.size
}