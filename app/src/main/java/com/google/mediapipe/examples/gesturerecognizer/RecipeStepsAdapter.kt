package com.google.mediapipe.examples.gesturerecognizer

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException

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
        val context = holder.itemView.context
        try {
            val inputStream = context.assets.open(step.imagePath)
            val drawable = Drawable.createFromStream(inputStream, null)
            holder.stepImageView.setImageDrawable(drawable)
        } catch (e: IOException) {
            // Handle exception or set a placeholder image
            holder.stepImageView.setImageResource(R.drawable.placeholder_image)
        }
    }

    override fun getItemCount(): Int = steps.size
}