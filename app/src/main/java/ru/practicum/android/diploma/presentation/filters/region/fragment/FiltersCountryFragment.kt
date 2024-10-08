package ru.practicum.android.diploma.presentation.filters.region.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.app.App
import ru.practicum.android.diploma.databinding.FragmentFiltersCountryBinding
import ru.practicum.android.diploma.domain.filters.models.FilterValue
import ru.practicum.android.diploma.domain.models.Area
import ru.practicum.android.diploma.util.Factory
import ru.practicum.android.diploma.presentation.filters.main.fragment.FiltersMainFragment
import ru.practicum.android.diploma.presentation.filters.region.state.AreaViewState
import ru.practicum.android.diploma.presentation.filters.region.viewmodel.FiltersCountryViewModel

class FiltersCountryFragment : Fragment(R.layout.fragment_filters_country) {

    private val viewModel by viewModels<FiltersCountryViewModel> {
        Factory {
            (requireContext().applicationContext as App).appComponent.countryComponent().viewModel()
        }
    }

    private var _binding: FragmentFiltersCountryBinding? = null
    private val binding get() = _binding!!

    private val adapter = FiltersAreaAdapter(emptyList<Area>()) { filterValue: Area ->
        clickListener(
            filterValue
        )
    }

    companion object {
        const val RESULT_NAME = "result_country"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFiltersCountryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.isVisible = false

        binding.rvCountries.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCountries.adapter = adapter

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        viewModel.getState().observe(viewLifecycleOwner) { state ->
            onChangeViewState(state)
        }

        viewModel.getSelectCountry().observe(viewLifecycleOwner) { filterValue ->
            selectCountry(filterValue)
        }
    }

    private fun clickListener(filterValue: Area) {
        viewModel.selectCountry(filterValue)
    }

    private fun selectCountry(filterValue: FilterValue) {
        setFragmentResult(FiltersMainFragment.FILTER_CHANGED, bundleOf())
        setFragmentResult(RESULT_NAME, bundleOf(RESULT_NAME to filterValue))
        onBackPressed()
    }

    private fun onBackPressed() {
        findNavController().popBackStack()
    }

    private fun onChangeViewState(state: AreaViewState) {
        if (state is AreaViewState.Content) adapter.setNewList(state.listAreas)

        binding.rvCountries.isVisible = when (state) {
            is AreaViewState.Content -> true
            else -> false
        }
        binding.rvCountries.isVisible = when (state) {
            is AreaViewState.Content -> true
            else -> false
        }

        binding.progressBar.isVisible = when (state) {
            is AreaViewState.Loading -> true
            else -> false
        }

        binding.llPlaceholderTrouble.isVisible = when (state) {
            is AreaViewState.Error -> true
            else -> false
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
