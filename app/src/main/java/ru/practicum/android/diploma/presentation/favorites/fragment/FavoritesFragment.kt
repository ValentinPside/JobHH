package ru.practicum.android.diploma.presentation.favorites.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.app.App
import ru.practicum.android.diploma.databinding.FragmentFavoritesBinding
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.presentation.Factory
import ru.practicum.android.diploma.presentation.favorites.state.FavoritesState
import ru.practicum.android.diploma.presentation.favorites.viewmodel.FavoritesViewModel
import ru.practicum.android.diploma.presentation.general.VacanciesAdapter
import ru.practicum.android.diploma.util.onTextChangeDebounce

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    companion object {
        const val VACANCY_DATA = "id"
        const val DEBAUNCE = 1000L
        fun createArgs(vacancyId: String): Bundle =
            bundleOf(VACANCY_DATA to vacancyId)
    }

    private val viewModel by viewModels<FavoritesViewModel> {
        Factory {
            (requireContext().applicationContext as App).appComponent.favoriteslComponent().viewModel()
        }
    }

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val adapter by lazy {
        VacanciesAdapter(false, {
            viewModel.showDetails(it)
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFavoritesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvFavorite.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavorite.adapter = adapter

        viewModel.getState().observe(viewLifecycleOwner) { state ->
            showListState(state)
        }

        viewModel.getShowPlayerTrigger().observe(viewLifecycleOwner) { vacancy ->
            showDetails(vacancy)
        }

        binding.searchEditText.onTextChangeDebounce().debounce(DEBAUNCE)
            .onEach {
                val query = it?.toString().orEmpty()
                viewModel.search(query)
            }.launchIn(lifecycleScope)
    }

    private fun showDetails(vacancyId: String) {
        findNavController().navigate(
            R.id.action_favoritesFragment_to_vacancyFragment,
            createArgs(vacancyId)
        )
    }

    private fun showListState(state: FavoritesState) {
        binding.ivFavorite.visibility = when (state) {
            is FavoritesState.Empty -> View.VISIBLE
            else -> View.GONE
        }

        binding.tvFavorite.visibility = when (state) {
            is FavoritesState.Empty -> View.VISIBLE
            else -> View.GONE
        }

        binding.tvFavorite.text = when (state) {
            is FavoritesState.Error -> getString(R.string.no_vacancies)
            else -> getString(R.string.favorite_empty)
        }

        binding.rvFavorite.visibility = when (state) {
            is FavoritesState.Content -> {
                adapter.submitList(state.favoritesVacancies)
                View.VISIBLE
            }

            else -> {
                View.GONE
            }
        }

        binding.pbLoading.visibility = when (state) {
            is FavoritesState.Loading -> View.VISIBLE
            else -> View.GONE
        }

        if (binding.rvFavorite.visibility == View.GONE && !(state is FavoritesState.Content)) {
            adapter.submitList(emptyList<Vacancy>())
        }

        updatePicture(state)
    }

    private fun updatePicture(state: FavoritesState) {
        val image = when (state) {
            FavoritesState.Empty -> {
                R.drawable.state_image_empty
            }

            else -> {
                R.drawable.state_image_nothing_found
            }
        }

        image?.let {
            Glide.with(requireContext())
                .load(image)
                .into(binding.ivFavorite)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadFavorites()
        activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.isVisible = true
    }
}
