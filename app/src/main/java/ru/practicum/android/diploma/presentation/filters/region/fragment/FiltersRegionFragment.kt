package ru.practicum.android.diploma.presentation.filters.region.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
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
import ru.practicum.android.diploma.databinding.FragmentFiltersRegionBinding
import ru.practicum.android.diploma.domain.models.Area
import ru.practicum.android.diploma.presentation.Factory
import ru.practicum.android.diploma.presentation.filters.main.fragment.FiltersMainFragment
import ru.practicum.android.diploma.presentation.filters.region.state.AreaViewState
import ru.practicum.android.diploma.presentation.filters.region.state.RegionSelectResult
import ru.practicum.android.diploma.presentation.filters.region.viewmodel.FiltersRegionViewModel
import ru.practicum.android.diploma.util.onTextChangeDebounce

class FiltersRegionFragment : Fragment(R.layout.fragment_filters_region) {

    private val viewModel by viewModels<FiltersRegionViewModel> {
        Factory {
            (requireContext().applicationContext as App).appComponent.regionComponent()
                .create(requireNotNull(requireArguments().getString("countryId"))).viewModel()
        }
    }

    private var _binding: FragmentFiltersRegionBinding? = null
    private val binding get() = _binding!!

    private val adapter = FiltersAreaAdapter(emptyList<Area>()) { filterValue: Area ->
        clickListener(
            filterValue
        )
    }

    companion object {
        const val DEBOUNCE = 1000L
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFiltersRegionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.isVisible = false

        binding.rvAreas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAreas.adapter = adapter

        binding.searchEditText.onTextChangeDebounce().debounce(DEBOUNCE)
            .onEach {
                val query = it?.toString().orEmpty()
                viewModel.search(query)

            }.launchIn(lifecycleScope)

        binding.clearButton.setOnClickListener {
            binding.searchEditText.text = null
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                setupIcon(p0.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
        })

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

        viewModel.getSelectRegion().observe(viewLifecycleOwner) { selectResult ->
            selectRegion(selectResult)
        }
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

    private fun selectRegion(selectResult: RegionSelectResult) {
        setFragmentResult(FiltersMainFragment.FILTER_CHANGED, bundleOf())

        setFragmentResult(
            FiltersWorkPlaceFragment.RESULT_NAME_REGION,
            bundleOf(FiltersWorkPlaceFragment.RESULT_NAME_REGION to selectResult.filterRegion)
        )
        setFragmentResult(
            FiltersWorkPlaceFragment.RESULT_NAME_COUNTRY,
            bundleOf(FiltersWorkPlaceFragment.RESULT_NAME_COUNTRY to selectResult.filterCountry)
        )
        onBackPressed()
    }

    private fun clickListener(area: Area) {
        viewModel.selectRegion(area)
    }

    private fun onBackPressed() {
        findNavController().popBackStack()
    }

    private fun onChangeViewState(state: AreaViewState) {
        if (state is AreaViewState.Content) adapter.setNewList(state.listAreas)

        binding.rvAreas.isVisible = when (state) {
            is AreaViewState.Content -> true
            else -> false
        }
        binding.rvAreas.isVisible = when (state) {
            is AreaViewState.Content -> true
            else -> false
        }

        binding.progressBar.isVisible = when (state) {
            is AreaViewState.Loading -> true
            else -> false
        }

        binding.llPlaceholderTrouble.isVisible = when (state) {
            is AreaViewState.Error, AreaViewState.Empty -> {
                setPlaceholder(state)
                true
            }

            else -> false
        }
    }

    private fun setPlaceholder(state: AreaViewState) {
        when (state) {
            AreaViewState.Empty -> {
                binding.srcText.setText(R.string.no_region)
            }

            AreaViewState.Error -> {
                binding.srcText.setText(R.string.load_list_failure)
            }

            else -> {
                binding.srcText.text = ""
            }
        }

        val image = when (state) {
            AreaViewState.Empty -> {
                R.drawable.state_image_nothing_found
            }

            AreaViewState.Error -> {
                R.drawable.state_image_failure
            }

            else -> {
                null
            }
        }

        image?.let {
            Glide.with(requireContext())
                .load(image)
                .fitCenter()
                .into(binding.src)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
