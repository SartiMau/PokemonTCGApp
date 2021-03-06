package com.globant.pokemontcgapp.activity

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.globant.domain.entity.PokemonCard
import com.globant.pokemontcgapp.adapter.PokemonCardListAdapter
import com.globant.pokemontcgapp.adapter.PokemonCardSelected
import com.globant.pokemontcgapp.databinding.ActivityPokemonCardListBinding
import com.globant.pokemontcgapp.util.Constant.POKEMON_GROUP
import com.globant.pokemontcgapp.util.Constant.SELECTION
import com.globant.pokemontcgapp.util.Constant.SELECTION_COLOR
import com.globant.pokemontcgapp.util.Event
import com.globant.pokemontcgapp.util.getColumnsByOrientation
import com.globant.pokemontcgapp.viewmodel.PokemonCardListViewModel
import com.globant.pokemontcgapp.viewmodel.PokemonCardListViewModel.Data
import com.globant.pokemontcgapp.viewmodel.PokemonCardListViewModel.Status
import org.koin.androidx.viewmodel.ext.android.viewModel

class PokemonCardListActivity : AppCompatActivity(), PokemonCardSelected {

    private val pokemonCardListViewModel by viewModel<PokemonCardListViewModel>()
    private lateinit var binding: ActivityPokemonCardListBinding
    private lateinit var pokemonCardGroup: String
    private lateinit var pokemonCardGroupSelected: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPokemonCardListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pokemonCardGroup = intent.getStringExtra(POKEMON_GROUP)
        pokemonCardGroupSelected = intent.getStringExtra(SELECTION)
        val groupSelectedColor = intent.getIntExtra(SELECTION_COLOR, DEFAULT_VALUE)
        initUi(pokemonCardGroupSelected, groupSelectedColor)
        pokemonCardListViewModel.getPokemonCardListLiveData().observe(::getLifecycle, ::updateUI)
    }

    override fun onResume() {
        super.onResume()
        binding.activityPokemonCardListLoading.visibility = View.GONE
        pokemonCardListViewModel.getPokemonCardList(pokemonCardGroup, pokemonCardGroupSelected)
    }

    private fun initUi(selection: String?, selectionColor: Int) {
        val pokemonCardListCardView = binding.activityPokemonCardListCardView
        supportPostponeEnterTransition()

        binding.activityPokemonCardListCardViewTitle.text = selection
        pokemonCardListCardView.setBackgroundColor(ContextCompat.getColor(this, selectionColor))
        pokemonCardListCardView.transitionName = pokemonCardGroupSelected

        supportStartPostponedEnterTransition()
    }

    private fun updateUI(data: Event<Data>) {
        val pokemonCardListData = data.getContentIfNotHandled()
        when (pokemonCardListData?.status) {
            Status.LOADING -> binding.activityPokemonCardListLoading.visibility = View.VISIBLE
            Status.SUCCESS -> showPokemonCardList(pokemonCardListData.data)
            Status.ERROR -> showPokemonCardListError(pokemonCardListData.error?.message)
            Status.ON_CARD_CLICKED -> onCardClicked(pokemonCardListData.pokemonCard, pokemonCardListData.sharedView)
        }
    }

    private fun showPokemonCardList(pokemonCardList: List<PokemonCard>) {
        binding.activityPokemonCardListLoading.visibility = View.GONE
        pokemonCardList.let {
            val pokemonCardListAdapter = PokemonCardListAdapter(pokemonCardList, this)
            binding.activityPokemonCardListRecyclerView.apply {
                layoutManager =
                    GridLayoutManager(
                        context,
                        resources.configuration.getColumnsByOrientation(
                            COLUMNS_PORTRAIT,
                            COLUMNS_LANDSCAPE
                        )
                    )
                adapter = pokemonCardListAdapter
                visibility = View.VISIBLE
            }
        }
    }

    private fun showPokemonCardListError(error: String?) {
        binding.activityPokemonCardListLoading.visibility = View.GONE
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

    override fun onPokemonCardSelected(pokemonCardSelected: PokemonCard, sharedView: View) {
        pokemonCardListViewModel.onPokemonCardSelected(pokemonCardSelected, sharedView)
    }

    private fun onCardClicked(pokemonCardSelected: PokemonCard?, sharedView: View?) {
        pokemonCardSelected?.let {
            startActivity(
                PokemonCardDetailActivity.getIntent(
                    this,
                    Triple(pokemonCardSelected.id, pokemonCardSelected.name, pokemonCardSelected.image)
                ),
                ActivityOptions.makeSceneTransitionAnimation(this, sharedView, sharedView?.let { ViewCompat.getTransitionName(it) })
                    .toBundle()
            )
        }
    }

    companion object {
        private const val DEFAULT_VALUE = -1
        private const val COLUMNS_PORTRAIT = 1
        private const val COLUMNS_LANDSCAPE = 2
        fun getIntent(context: Context, data: Triple<String, String, Int>): Intent =
            Intent(context, PokemonCardListActivity::class.java).apply {
                putExtra(POKEMON_GROUP, data.first)
                putExtra(SELECTION, data.second)
                putExtra(SELECTION_COLOR, data.third)
            }
    }
}
