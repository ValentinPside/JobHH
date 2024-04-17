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

        setListeners()
        setObservers()
        setResultListeners()
    }

    private fun setObservers() {
        viewModel.getState().observe(viewLifecycleOwner) { state ->
            onChangeViewState(state)
        }
    }

    private fun setResultListeners() {
        setFragmentResultListener(FILTER_CHANGED) { s: String, bundle: Bundle ->
            viewModel.loadCurrentFilters(true)
        }
    }

    private fun setListeners() {
        binding.llWorkPlace.ivBtnClear.setOnClickListener { viewModel.clearWorkPlace() }

        binding.tietSalary.onTextChangeDebounce().debounce(DEBOUNCE)
            .onEach {
                viewModel.setSalaryFilter(it?.toString().orEmpty())
            }.launchIn(lifecycleScope)
        binding.tietSalary.addTextChangedListener(
            onTextChanged = { charSequence, _, _, _ -> onChangeSalary(charSequence.toString()) }
        )

        binding.tietSalary.setOnFocusChangeListener { v, hasFocus ->
            val salaryString = binding.tietSalary.text.toString()
            if (!hasFocus && binding.tvSalaryHint.isEnabled) viewModel.setSalaryFilter(salaryString)
            onChangeSalary(salaryString)
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        binding.llIndustries.root.setOnClickListener {
            findNavController().navigate(R.id.action_filtersMainFragment_to_industryFragment)
        }
        binding.llIndustries.ivBtnClear.setOnClickListener { viewModel.clearIndustry() }

        binding.btnAccept.setOnClickListener {
            setFragmentResult(GeneralFragment.ON_FILTER_CHANGED, bundleOf())
            onBackPressed()
        }

        binding.ibClear.setOnClickListener {
            binding.tietSalary.text = null
            viewModel.setSalaryFilter("")
        }
        binding.cbOnlyWithSalary.setOnClickListener {
            setOnlyWithSalayFilter()
        }
        binding.btnCancel.setOnClickListener { viewModel.clearAllFilters() }

        binding.llWorkPlace.root.setOnClickListener {
            findNavController().navigate(R.id.action_filtersMainFragment_to_filtersWorkPlaceFragment)
        }
    }

    private fun checkAndSaveEditText() {
        if (binding.tvSalaryHint.isEnabled) {
            viewModel.setSalaryFilter(binding.tietSalary.text.toString())
        }
    }

    private fun setOnlyWithSalayFilter() {
        checkAndSaveEditText()
        viewModel.setOnlySalaryFilter(binding.cbOnlyWithSalary.isChecked)
    }

    private fun onBackPressed() {
        checkAndSaveEditText()
        findNavController().popBackStack()
    }

    private fun onChangeSalary(salaryText: String) {
        binding.tvSalaryHint.isEnabled = binding.tietSalary.hasFocus()
        binding.tvSalaryHint.isSelected = salaryText.isNotEmpty()
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
        if (binding.tvSalaryHint.isEnabled) {
            binding.tietSalary.setSelection(binding.tietSalary.text?.length ?: 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
