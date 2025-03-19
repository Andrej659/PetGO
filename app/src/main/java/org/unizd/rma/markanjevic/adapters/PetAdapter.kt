package org.unizd.rma.markanjevic.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.unizd.rma.markanjevic.R
import org.unizd.rma.markanjevic.models.Animal

class PetAdapter(
    private var petList: List<Animal>,
    private val listener: OnPetClickListener
) : RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    interface OnPetClickListener {
        fun onPetClick(petId: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pet, parent, false)
        return PetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        val pet = petList[position]
        holder.bind(pet)
    }

    override fun getItemCount(): Int = petList.size

    inner class PetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val petName: TextView = itemView.findViewById(R.id.petName)
        private val petImage: ImageView = itemView.findViewById(R.id.petImage)

        fun bind(pet: Animal) {
            petName.text = pet.name

            Glide.with(itemView.context)
                .load(pet.image)
                .into(petImage)


            itemView.setOnClickListener {
                listener.onPetClick(pet.id)
            }
        }
    }
}