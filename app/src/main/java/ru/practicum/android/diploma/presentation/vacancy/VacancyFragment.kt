package ru.practicum.android.diploma.presentation.vacancy

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.noties.markwon.Markwon
import io.noties.markwon.html.HtmlPlugin
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.app.App
import ru.practicum.android.diploma.databinding.FragmentVacancyBinding
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.models.VacancyDetail
import ru.practicum.android.diploma.presentation.Factory
import ru.practicum.android.diploma.util.SalaryUtil
import ru.practicum.android.diploma.util.visibleOrGone

class VacancyFragment : Fragment(R.layout.fragment_vacancy) {
    private val vacancyId: String? by lazy { requireArguments().getString("id") }

    private var _binding: FragmentVacancyBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<VacancyViewModel> {
        Factory {
            App.appComponent.vacancyComponent().create(requireNotNull(vacancyId)).viewModel()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentVacancyBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.observeUi().collect() { state ->
                    binding.progressBar.visibleOrGone(state.isLoading)
                    binding.fragmentNotifications.visibleOrGone(!state.isLoading)
                    state.vacancy?.let {
                        updateVacancy(it)
                    }
                }
            }
        }
        binding.vacancyToolbars.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.buttonShare.setOnClickListener {
            viewModel.shareVacancy()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateVacancy(vacancy: VacancyDetail) {
        with(binding) {
            jobName.text = vacancy.name
            jobSalary.text = SalaryUtil.formatSalary(requireContext(), vacancy.salary)

            Glide.with(requireContext())
                .load(vacancy.employer?.logoUrls) // false
                .placeholder(R.drawable.placeholder_company_icon)
                .fitCenter()
                .transform(RoundedCorners(8))
                .into(ivCompany)

            companyName.text = vacancy.area
            contactPersonData.text = vacancy.contacts?.name
            contactPersonEmailData.text = vacancy.contacts?.email
            contactPersonPhoneData.text = vacancy.contacts?.phones.toString()
            neededExperience.text = vacancy.experience
            jobTime.text = vacancy.employment

            vacancy.description?.let {
                val markwon = Markwon.builder(requireContext())
                    .usePlugin(HtmlPlugin.create())
                    .build()
                markwon.setMarkdown(vacancyDescription, it)
            }
        }
    }
}
