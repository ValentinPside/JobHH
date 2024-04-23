package ru.practicum.android.diploma.presentation.general

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.VacancyItemBinding
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.util.SalaryUtil

class VacanciesAdapter(
    private val needPadding: Boolean = false,
    private val onClick: (String) -> Unit
) : ListAdapter<Vacancy, VacanciesAdapter.ViewHolder>(DiffUtil()) {

    companion object {
        const val FIRST_ELEMENT_PADDING_TOP = 32f
        const val ELEMENT_PADDING_TOP = 9f
    }

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        private val binding by viewBinding { VacancyItemBinding.bind(view) }

        fun bind(vacancy: Vacancy, firstElement: Boolean) {
            val padding = if (needPadding && firstElement) {
                FIRST_ELEMENT_PADDING_TOP
            } else {
                ELEMENT_PADDING_TOP
            }

            if (!(binding.rootItem.paddingTop.toFloat() == padding)) {
                binding.rootItem.updatePadding(
                    top = dpToPx(
                        padding,
                        binding.root.context
                    )
                )
            }
            binding.tvVacancyName.text = vacancy.name.plus(", ").plus(vacancy.area)
            binding.salary.text = SalaryUtil.formatSalary(view.context, vacancy.salary)
            Glide.with(view.context)
                .load(vacancy.img)
                .placeholder(R.drawable.ic_placeholder)
                .into(binding.ivCompany)
            binding.department.text = vacancy.employer
            binding.root.setOnClickListener { onClick.invoke(vacancy.id) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.vacancy_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = currentList[position]
        holder.bind(item, position == 0)
    }
}

fun dpToPx(dp: Float, context: Context): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        context.resources.displayMetrics
    ).toInt()
}

class DiffUtil : DiffUtil.ItemCallback<Vacancy>() {
    override fun areItemsTheSame(oldItem: Vacancy, newItem: Vacancy): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Vacancy, newItem: Vacancy): Boolean {
        return oldItem == newItem
    }

}
