package com.globant.pokemontcgapp.fragment

import android.app.ActivityOptions
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.globant.domain.entity.SecondaryTypes
import com.globant.pokemontcgapp.activity.PokemonCardListActivity
import com.globant.pokemontcgapp.adapter.PokemonSecondaryTypeSelected
import com.globant.pokemontcgapp.adapter.PokemonSecondaryTypesAdapter
import com.globant.pokemontcgapp.databinding.FragmentPokemonAlltypesLayoutBinding
import com.globant.pokemontcgapp.util.Event
import com.globant.pokemontcgapp.util.getColumnsByOrientation
import com.globant.pokemontcgapp.util.pokemonSupertypesResources
import com.globant.pokemontcgapp.viewmodel.PokemonSupertypeViewModel
import com.globant.pokemontcgapp.viewmodel.PokemonSupertypeViewModel.Data
import com.globant.pokemontcgapp.viewmodel.PokemonSupertypeViewModel.Status
import org.koin.androidx.viewmodel.ext.android.viewModel

class PokemonSupertypeFragment : Fragment(), PokemonSecondaryTypeSelected {

    private val pokemonSupertypeViewModel by viewModel<PokemonSupertypeViewModel>()
    private lateinit var binding: FragmentPokemonAlltypesLayoutBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPokemonAlltypesLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pokemonSupertypeViewModel.getPokemonSupertypesLiveData().observe(::getLifecycle, ::updateUI)
    }

    override fun onResume() {
        super.onResume()
        binding.pokemonAlltypesLoading.visibility = View.GONE
        pokemonSupertypeViewModel.getPokemonSupertypes(pokemonSupertypesResources)
    }

    private fun updateUI(data: Event<Data>) {
        val pokemonSupertypesData = data.getContentIfNotHandled()
        when (pokemonSupertypesData?.status) {
            Status.LOADING -> binding.pokemonAlltypesLoading.visibility = View.VISIBLE
            Status.SUCCESS -> showPokemonSupertypes(pokemonSupertypesData.data)
            Status.ERROR -> showPokemonSupertypesError(pokemonSupertypesData.error?.message)
            Status.ON_SUPERTYPE_CLICKED -> pokemonSupertypesData.let { onSupertypeClicked(it.pokemonSupertype, it.sharedView) }
        }
    }

    private fun showPokemonSupertypes(pokemonSupertypes: List<SecondaryTypes>) {
        binding.pokemonAlltypesLoading.visibility = View.GONE
        pokemonSupertypes.let {
            val pokemonSupertypesAdapter = PokemonSecondaryTypesAdapter(pokemonSupertypes, this)
            binding.pokemonAlltypesRecyclerView.apply {
                layoutManager =
                    GridLayoutManager(
                        context,
                        resources.configuration.getColumnsByOrientation(
                            COLUMNS_PORTRAIT,
                            COLUMNS_LANDSCAPE
                        )
                    )
                adapter = pokemonSupertypesAdapter
                visibility = View.VISIBLE
            }
        }
    }

    private fun showPokemonSupertypesError(error: String?) {
        binding.pokemonAlltypesLoading.visibility = View.GONE
        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
    }

    override fun onPokemonSecondaryTypeSelected(secondaryTypeSelected: SecondaryTypes, sharedView: View) {
        pokemonSupertypeViewModel.onPokemonSupertypeSelected(secondaryTypeSelected, sharedView)
    }

    private fun onSupertypeClicked(supertypeSelected: SecondaryTypes?, sharedView: View?) {
        supertypeSelected?.let {
            startActivity(
                PokemonCardListActivity.getIntent(
                    requireContext(),
                    Triple(SUPERTYPE, supertypeSelected.name, supertypeSelected.bgColor)
                ),
                ActivityOptions.makeSceneTransitionAnimation(activity, sharedView, sharedView?.let { ViewCompat.getTransitionName(it) })
                    .toBundle()
            )
        }
    }

    companion object {
        private const val SUPERTYPE = "supertype"
        private const val COLUMNS_PORTRAIT = 1
        private const val COLUMNS_LANDSCAPE = 3
    }
}
