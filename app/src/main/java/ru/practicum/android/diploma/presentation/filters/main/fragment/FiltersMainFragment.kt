package ru.practicum.android.diploma.presentation.filters.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.app.App
import ru.practicum.android.diploma.databinding.FragmentFiltersMainBinding
import ru.practicum.android.diploma.presentation.Factory
import ru.practicum.android.diploma.presentation.filters.CustomViewPropertysSetter.setViewPropertys
import ru.practicum.android.diploma.presentation.filters.main.state.FiltersMainViewState
import ru.practicum.android.diploma.presentation.filters.main.viewmodel.FiltersMainViewModel
import ru.practicum.android.diploma.presentation.general.fragment.GeneralFragment
import ru.practicum.android.diploma.util.onTextChangeDebounce

class FiltersMainFragment : Fragment(R.layout.fragment_filters_main) {

    private val viewModel by viewModels<FiltersMainViewModel> {
        Factory {
            (requireContext().applicationContext as App).appComponent.filtersMainComponent().viewModel()
        }
    }

    private var _binding: FragmentFiltersMainBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val DEBOUNCE = 1000L
        const val FILTER_CHANGED = "filter_changed"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFiltersMainBinding.inflate(layoutInflater)
        binding.llWorkPlace.smallTextBlock.setText(getString(R.string.filter_workplace))
        binding.llIndustries.smallTextBlock.setText(getString(R.string.filter_industries))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCancel.isSelected = true
        activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.isVisible = false

        binding.tietSalary.onTextChangeDebounce().debounce(DEBOUNCE)
            .onEach {
                viewModel.setSalaryFilter(it?.toString().orEmpty())
            }.launchIn(lifecycleScope)

        binding.tietSalary.addTextChangedListener(
            onTextChanged = { charSequence, _, _, _ ->
                onChangeSalary(charSequence.toString())
            }
        )

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.btnAccept.setOnClickListener {
            setFragmentResult(GeneralFragment.ON_FILTER_CHANGED, bundleOf())
            onBackPressed()
        }

        binding.ibClear.setOnClickListener {
            binding.tietSalary.text = null
            viewModel.setSalaryFilter("")
        }

        binding.cbOnlyWithSalary.setOnClickListener {
            viewModel.setOnlySalaryFilter(binding.cbOnlyWithSalary.isChecked)
        }

        binding.btnCancel.setOnClickListener {
            viewModel.clearAllFilters()
        }

        binding.llWorkPlace.root.setOnClickListener {
            findNavController().navigate(
                R.id.action_filtersMainFragment_to_filtersWorkPlaceFragment
            )
        }

        viewModel.getState().observe(viewLifecycleOwner) { state ->
            onChangeViewState(state)
        }

        setFragmentResultListener(FILTER_CHANGED) { s: String, bundle: Bundle ->
            viewModel.loadCurrentFilters(true)
        }

        binding.llWorkPlace.ivBtnClear.setOnClickListener {
            viewModel.clearWorkPlace()
        }
    }

    private fun onChangeAcceptAvaiable(acceptAvaiable: Boolean) {
        binding.btnCancel.isVisible = acceptAvaiable
        binding.btnAccept.isVisible = acceptAvaiable
    }

    private fun onBackPressed() {
        findNavController().popBackStack()
    }

    private fun onChangeSalary(salaryText: String) {
        binding.tvSalaryHint.isEnabled = salaryText.isNotBlank()
        binding.ibClear.isVisible = salaryText.isNotBlank()
    }

    private fun onChangeViewState(state: FiltersMainViewState) {
        when (state) {
            is FiltersMainViewState.Empty -> {
                setViewPropertys(binding.llWorkPlace, "")
                setViewPropertys(binding.llIndustries, "")
                binding.tietSalary.setText("")
                binding.cbOnlyWithSalary.isChecked = false
                binding.btnCancel.isVisible = false
                binding.btnAccept.isVisible = false
            }

            is FiltersMainViewState.Content -> {
                setViewPropertys(binding.llWorkPlace, state.workPlace)
                setViewPropertys(binding.llIndustries, state.industries)
                binding.tietSalary.setText(state.salary)
                binding.cbOnlyWithSalary.isChecked = state.onlyWithSalary
                binding.btnCancel.isVisible = state.filtresAvailable
                binding.btnAccept.isVisible = state.filterChanged
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
