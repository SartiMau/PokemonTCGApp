package com.globant.pokemontcgapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.globant.domain.entity.SecondaryTypes
import com.globant.pokemontcgapp.R
import com.globant.pokemontcgapp.databinding.PokemonSecondaryTypeElementBinding

interface PokemonSecondaryTypeSelected {
    fun onPokemonSecondaryTypeSelected(secondaryTypeSelected: SecondaryTypes, sharedView: View)
}

class PokemonSecondaryTypesAdapter(
    private val pokemonSecondaryTypes: List<SecondaryTypes>,
    private val onSecondaryTypeClicked: PokemonSecondaryTypeSelected
) :
    RecyclerView.Adapter<PokemonSecondaryTypesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.pokemon_secondary_type_element,
                parent,
                false
            ),
            onSecondaryTypeClicked
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(pokemonSecondaryTypes[position])
    }

    override fun getItemCount(): Int = pokemonSecondaryTypes.size

    class ViewHolder(itemView: View, private val onSecondaryTypeClicked: PokemonSecondaryTypeSelected) : RecyclerView.ViewHolder(itemView) {
        private val binding = PokemonSecondaryTypeElementBinding.bind(itemView)

        fun bind(item: SecondaryTypes) = with(itemView) {
            val pokemonSecondaryTypeCardView = binding.pokemonSecondaryTypeCardView
            pokemonSecondaryTypeCardView.transitionName = item.name
            setOnClickListener { onSecondaryTypeClicked.onPokemonSecondaryTypeSelected(item, pokemonSecondaryTypeCardView) }
            binding.pokemonSecondaryTypeTextViewName.text = item.name
            binding.pokemonSecondaryTypeCardView.setCardBackgroundColor(ContextCompat.getColor(context, item.bgColor))
        }
    }
}
