package ru.practicum.android.diploma.presentation.general

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import ru.practicum.android.diploma.util.UtilFunction

class VacanciesAdapter(
    private val needPadding: Boolean = false,
    private val onClick: (String) -> Unit,
    private val onClickFavorite: ((String, Int) -> Unit?)?
) : ListAdapter<Vacancy, VacanciesAdapter.ViewHolder>(DiffUtil()) {

    companion object {
        const val FIRST_ELEMENT_PADDING_TOP = 32f
        const val ELEMENT_PADDING_TOP = 9f
    }

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        private val binding by viewBinding { VacancyItemBinding.bind(view) }

        var isOpen = false

        fun isHolderOpen(): Boolean {
            if (isOpen) return true
            return binding.ivLike.layoutParams.width > 0
        }

        fun bind(vacancy: Vacancy, position: Int) {
            val padding = if (needPadding && position == 0) {
                FIRST_ELEMENT_PADDING_TOP
            } else {
                ELEMENT_PADDING_TOP
            }

            if (!(binding.rootItem.paddingTop.toFloat() == padding)) {
                binding.rootItem.updatePadding(
                    top = UtilFunction.dpToPx(
                        padding,
                        binding.root.context
                    )
                )
            }
            binding.tvVacancyName.text = vacancy.name
            binding.salary.text = SalaryUtil.formatSalary(view.context, vacancy.salary)
            Glide.with(view.context)
                .load(vacancy.img)
                .placeholder(R.drawable.placeholder_company_icon)
                .into(binding.ivCompany)
            binding.department.text = vacancy.area
            binding.root.setOnClickListener { onClick.invoke(vacancy.id) }

            if (binding.ivLike.layoutParams.width > 0) {
                isOpen = false
                binding.ivLike.layoutParams.width = 0
                binding.ivLike.layoutParams.height = 0
                binding.ivLike.requestLayout()
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.vacancy_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = currentList[position]
        val v = holder.itemView.findViewById<ImageView>(R.id.ivLike)
        v.setOnClickListener { onClickFavorite?.invoke(item.id, position) }
        holder.bind(item, position)
    }
}


class DiffUtil : DiffUtil.ItemCallback<Vacancy>() {
    override fun areItemsTheSame(oldItem: Vacancy, newItem: Vacancy): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Vacancy, newItem: Vacancy): Boolean {
        return oldItem == newItem
    }

}
