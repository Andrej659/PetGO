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

/**
 * Adapter za prikazivanje liste ljubimaca u RecyclerView-u.
 *
 * Ova klasa omogućuje povezivanje podataka o ljubimcima s prikazom u RecyclerView-u,
 * omogućujući prikazivanje imena ljubimaca i njihovih slika. Također omogućuje
 * interakciju s korisnikom putem klika na stavku.
 *
 * @param petList Lista objekata tipa [Animal] koji sadrže podatke o ljubimcima.
 * @param listener Listener koji se poziva kada korisnik klikne na stavku ljubimca.
 */
class PetAdapter(
    private var petList: List<Animal>,
    private val listener: OnPetClickListener
) : RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    /**
     * Interface koji definira interfejs za detektiranje klika na ljubimca.
     *
     * @param petId Identifikator ljubimca koji je kliknut.
     */
    fun interface OnPetClickListener {
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

    /**
     * ViewHolder koji predstavlja jednu stavku ljubimca u RecyclerView-u.
     *
     * Sadrži referencu na UI elemente kao što su ime ljubimca i slika ljubimca.
     */
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