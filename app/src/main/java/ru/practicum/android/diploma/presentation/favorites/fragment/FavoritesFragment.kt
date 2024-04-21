package ru.practicum.android.diploma.presentation.favorites.fragment

import android.graphics.Canvas
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import ru.practicum.android.diploma.util.UtilFunction
import ru.practicum.android.diploma.util.onTextChangeDebounce

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    lateinit var touchHelper: ItemTouchHelper

    var currentHolder: VacanciesAdapter.ViewHolder? = null

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
        }, null)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                setupIcon(p0.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
        })

        binding.clearButton.setOnClickListener {
            binding.searchEditText.text = null
        }

        setHelpers()
    }

    private fun setHelpers() {
        touchHelper = ItemTouchHelper(
            object : ItemTouchHelper.Callback() {
                private var prevPos = 0f

                override fun getMovementFlags(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ): Int {
                    return makeMovementFlags(
                        0,
                        ItemTouchHelper.START //or ItemTouchHelper.START
                    )
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    val constParam = UtilFunction.dpToPx(20f, requireContext()).toFloat()
                    val res = if (dX >= constParam) {
                        constParam
                    } else {
                        dX
                    }
                    super.onChildDraw(c, recyclerView, viewHolder, res, dY, actionState, isCurrentlyActive)

                    if (isCurrentlyActive) prevPos = minOf(dX, res)

                }

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    when (direction) {
                        ItemTouchHelper.START -> {
                            val vacancy = adapter.currentList.get(viewHolder.adapterPosition)
                            viewModel.deleteFromFavorite(vacancy)
                        }
                    }
                }
            })

        touchHelper.attachToRecyclerView(binding.rvFavorite)
    }

    private fun showDetails(vacancyId: String) {
        findNavController().navigate(
            R.id.action_favoritesFragment_to_vacancyFragment,
            createArgs(vacancyId)
        )
    }

    private fun setupIcon(it: String) {
        if (it.isNotBlank()) {
            binding.searchEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_clear, 0)
            binding.clearButton.isEnabled = true
        } else {
            binding.searchEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_search, 0)
            binding.clearButton.isEnabled = false
        }
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

        image.let {
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
