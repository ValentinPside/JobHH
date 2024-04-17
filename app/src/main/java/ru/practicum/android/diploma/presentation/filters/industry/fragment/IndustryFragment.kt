package ru.practicum.android.diploma.presentation.filters.industry.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.app.App
import ru.practicum.android.diploma.databinding.FragmentFilterIndustryBinding
import ru.practicum.android.diploma.presentation.Factory
import ru.practicum.android.diploma.presentation.filters.industry.state.FiltersIndustriesState
import ru.practicum.android.diploma.presentation.filters.industry.viewmodel.IndustryViewModel
import ru.practicum.android.diploma.presentation.filters.main.fragment.FiltersMainFragment

class IndustryFragment : Fragment() {
    private var _binding: FragmentFilterIndustryBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<IndustryViewModel> {
        Factory {
            (requireContext().applicationContext as App).appComponent.industryComponent().viewModel()
        }
    }

    private var adapter = IndustriesAdapter { industry ->
        viewModel.setCurrentIndustry(industry)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFilterIndustryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.industryList.adapter = this.adapter
        binding.clearButton.setOnClickListener { binding.choosingIndustryEditText.text?.clear() }
        binding.choosingIndustryEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.filterIndustries(text.toString())
            if (text.isNullOrBlank()) {
                binding.clearButton.visibility = View.GONE
                binding.searchDrawable.visibility = View.VISIBLE
                if (adapter.checkedRadioButtonId != -1) binding.chooseIndustryButton.visibility = View.VISIBLE
            } else {
                binding.clearButton.visibility = View.VISIBLE
                binding.searchDrawable.visibility = View.GONE
            }
        }

        binding.toolbarText.setNavigationOnClickListener { findNavController().popBackStack() }

        binding.chooseIndustryButton.setOnClickListener {
            viewModel.saveCurrentIndustry()
            parentFragmentManager.setFragmentResult(FiltersMainFragment.FILTER_CHANGED, bundleOf())
            findNavController().popBackStack()
        }

        viewModel.industriesState.observe(viewLifecycleOwner) { viewState ->
            val state = viewState.stateData
            binding.chooseIndustryButton.isVisible = viewState.isSelected
            when (state) {
                is FiltersIndustriesState.Error -> {
                    binding.llPlaceholderTrouble.isVisible = true
                    setPlaceholderImage(state)
                    binding.srcText.setText(R.string.no_internet)
                }

                is FiltersIndustriesState.Empty -> {
                    showEmpty()
                    setPlaceholderImage(state)
                }

                is FiltersIndustriesState.Loading -> showLoading()

                is FiltersIndustriesState.Success -> showContent(state)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showContent(state: FiltersIndustriesState) {
        if (state !is FiltersIndustriesState.Success) return

        val data = state.data

        adapter.updateList(data)

        binding.progressBar.visibility = View.GONE
        binding.llPlaceholderTrouble.visibility = View.GONE
        binding.industryList.visibility = View.VISIBLE
    }

    private fun showEmpty() {
        binding.chooseIndustryButton.visibility = View.GONE
        binding.industryList.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.llPlaceholderTrouble.visibility = View.VISIBLE
        binding.srcText.setText(R.string.load_list_failure)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.chooseIndustryButton.visibility = View.GONE
        binding.industryList.visibility = View.GONE
        binding.llPlaceholderTrouble.visibility = View.GONE
    }

    private fun setPlaceholderImage(state: FiltersIndustriesState) {
        val image = when (state) {
            FiltersIndustriesState.Error -> {
                R.drawable.state_image_no_internet
            }

            FiltersIndustriesState.Empty -> {
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
}
