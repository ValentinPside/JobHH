package ru.practicum.android.diploma.presentation.general.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.app.App
import ru.practicum.android.diploma.databinding.FragmentGeneralBinding
import ru.practicum.android.diploma.domain.general.models.ResponseState
import ru.practicum.android.diploma.presentation.Factory
import ru.practicum.android.diploma.presentation.general.VacanciesAdapter
import ru.practicum.android.diploma.presentation.general.viewmodel.FavoriteState
import ru.practicum.android.diploma.presentation.general.viewmodel.GeneralViewModel
import ru.practicum.android.diploma.util.UtilFunction
import ru.practicum.android.diploma.util.debounceFun
import ru.practicum.android.diploma.util.onTextChangeDebounce
import ru.practicum.android.diploma.util.visibleOrGone
import java.io.IOException
import kotlin.math.abs


private const val DEBOUNCE: Long = 2000

class GeneralFragment : Fragment(R.layout.fragment_general) {

    private var coordX = 0f
    private var coordY = 0f

    lateinit var touchHelper: ItemTouchHelper

    var currentHolder: VacanciesAdapter.ViewHolder? = null

    private val viewModel by viewModels<GeneralViewModel> {
        Factory {
            (requireContext().applicationContext as App).appComponent.generalComponent().viewModel()
        }
    }

    companion object {
        const val ON_FILTER_CHANGED = "on_filter_changed"
        const val SIZE_VISIBLE_LIKE_ICO = 50f
    }

    private var _binding: FragmentGeneralBinding? = null
    private val binding get() = _binding!!

    private val adapter by lazy {
        VacanciesAdapter(true, {
            val params = bundleOf("id" to it)
            findNavController().navigate(R.id.action_generalFragment_to_vacancyFragment, params)
        }) {
            lifecycleScope.launch {
                openViewHolder(it)
                delay(200L)
                closeHolder(it)
            }
        }
    }

    private val debaunceCloseItems =
        debounceFun<Boolean>(300L, lifecycleScope, true) {
            checkAndClose(it)
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGeneralBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupVacancies()
        setListeners()
        setObservers()

        setFragmentResultListener(ON_FILTER_CHANGED) { _: String, _: Bundle ->
            viewModel.searchOnFilterChanged()
        }

        setHelpers()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners() {
        binding.searchEditText.onTextChangeDebounce().debounce(DEBOUNCE)
            .onEach {
                val query = it?.toString().orEmpty()
                currentHolder = null
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

        binding.vacanciesRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    val pos = (binding.vacanciesRv.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                    val itemsCount = adapter.itemCount
                    if (pos >= itemsCount - 1) {
                        viewModel.onLastItemReached()
                    }
                }
            }
        })
        binding.filterImageView.setOnClickListener {
            findNavController().navigate(
                R.id.action_generalFragment_to_filtersMainFragment
            )
        }

        binding.vacanciesRv.setOnTouchListener { v, event ->
            onTouch(v, event)
            false
        }
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
                        ItemTouchHelper.END //or ItemTouchHelper.START
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

                    if (dX < prevPos
                        && abs(dX - prevPos) >= 5
                        && isCurrentlyActive == false
                        && viewHolder == currentHolder
                    ) {
                        closeHolder(viewHolder as VacanciesAdapter.ViewHolder)
                    }

                    if (dX > prevPos
                        && abs(dX - prevPos) >= 5
                        && isCurrentlyActive == false
                        && currentHolder != viewHolder
                        && currentHolder?.isOpen == true
                    ) {
                        closeHolder(viewHolder as VacanciesAdapter.ViewHolder)
                    }
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
                        ItemTouchHelper.END -> {
                            openViewHolder(viewHolder as VacanciesAdapter.ViewHolder)
                        }
                    }
                }
            })

        touchHelper.attachToRecyclerView(binding.vacanciesRv)
    }

    private fun openViewHolder(viewHolder: VacanciesAdapter.ViewHolder) {
        if (viewHolder.isHolderOpen()) return

        if (!(currentHolder == viewHolder) && currentHolder?.isHolderOpen() == true) {
            currentHolder?.let { closeHolder(it, true) }
        }

        currentHolder = viewHolder

        val ivLikeBig = viewHolder.itemView.findViewById<ImageView>(R.id.ivLike)
        val ivLikeSmall = viewHolder.itemView.findViewById<ImageView>(R.id.ivAddToFav)
        val anim = ResizeAnimationWithAlpha(
            ivLikeBig,
            ivLikeSmall,
            UtilFunction.dpToPx(SIZE_VISIBLE_LIKE_ICO, requireContext()),
            0f
        )
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) = Unit
            override fun onAnimationEnd(animation: Animation?) {
                debaunceCloseItems(true)
            }

            override fun onAnimationRepeat(animation: Animation?) = Unit
        })
        ivLikeBig.clearAnimation()
        ivLikeBig.startAnimation(anim)
    }

    private fun setObservers() {
        viewModel.observeUi().observe(viewLifecycleOwner) { state ->
            render(state)
        }
        viewModel.observeFilters().observe(viewLifecycleOwner) { isWithFilters ->
            checkFilters(isWithFilters)
        }

        viewModel.observeIsFavorite().observe(viewLifecycleOwner) { favState ->
            renderFavState(favState)
        }
    }

    private fun checkAndClose(needUpdate: Boolean = false) {
        if (_binding == null) return
        for (i in 0 until binding.vacanciesRv.childCount) {
            val vh = binding.vacanciesRv.getChildViewHolder(binding.vacanciesRv.getChildAt(i))
            if (currentHolder != vh
                && (vh as VacanciesAdapter.ViewHolder).isHolderOpen()
            ) {
                closeHolder(vh, needUpdate)
            }
        }
    }

    class ResizeAnimationWithAlpha(
        private val viewSize: View,
        private val viewAlpha: View,
        private val targetWidth: Int,
        private val targetAlpha: Float
    ) : Animation() {

        val startSize: Int
        val startAlpha: Float

        init {
            super.setDuration(200L)
            //super.setRepeatCount(Animation.);
            startSize = viewSize.width
            startAlpha = viewAlpha.alpha
        }

        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            val newSize = (startSize + (targetWidth - startSize) * interpolatedTime).toInt()
            viewSize.layoutParams.width = newSize
            viewSize.layoutParams.height = newSize
            viewSize.requestLayout()

            viewAlpha.alpha = (startAlpha + (targetAlpha - startAlpha) * interpolatedTime)
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    private fun renderFavState(favState: FavoriteState) {
        setupIconCurrentHolder(favState)
    }

    private fun setupIconCurrentHolder(favState: FavoriteState) {
        currentHolder?.let {
            val ivLike = it.itemView.findViewById<ImageView>(R.id.ivLike)
            val progressLike = it.itemView.findViewById<ProgressBar>(R.id.progressLike)
            val image = when (favState) {
                is FavoriteState.Content -> {
                    currentHolder?.isOpen = false
                    currentHolder = null
                    debaunceCloseItems(true)
                    try {
                        val vacancy = adapter.currentList.get(favState.position)
                        if (vacancy.id == favState.id) {
                            vacancy.isFavorite = favState.isFavorite
                            when (favState.isFavorite) {
                                true -> {
                                    R.drawable.favorite_vacancy_drawable_fill
                                }

                                false -> {
                                    R.drawable.favorite_vacancy_drawable_empty
                                }
                            }
                        } else {
                            R.drawable.favorite_vacancy_drawable_empty
                        }
                    } catch (e: IOException) {
                        println(e)
                    }
                }

                is FavoriteState.Error -> {
                    R.drawable.placeholder_company_icon
                }

                else -> {
                    null
                }
            }

            if (favState is FavoriteState.Loading) {
                progressLike.isVisible = true
                ivLike.isVisible = false
            } else {
                image.let {
                    Glide.with(requireContext())
                        .load(image)
                        .into(ivLike)
                }
                progressLike.isVisible = false
                ivLike.isVisible = true
            }
        }
    }

    private fun render(state: ResponseState) {
        if (state is ResponseState.ContentVacanciesList) {
            adapter.submitList(state.listVacancy)
            updateFavIco()
        } else {
            val needClearList = when (state) {
                is ResponseState.Loading -> !state.isPagination
                is ResponseState.NetworkError -> !state.isPagination
                else -> true
            }
            if (needClearList) {
                adapter.submitList(emptyList())
                adapter.notifyDataSetChanged()
            }
        }
        updateStatus(state)
    }

    private fun updateFavIco() {
        if (_binding == null) return
        for (i in 0 until binding.vacanciesRv.childCount) {
            val vh =
                binding.vacanciesRv.getChildViewHolder(binding.vacanciesRv.getChildAt(i)) as VacanciesAdapter.ViewHolder
            if (vh.adapterPosition != -1) {
                adapter.currentList.get(vh.adapterPosition)?.let {
                    vh.updateFavIco(it)
                }
            }
        }
    }

    fun setCoords(x: Float, y: Float) {
        coordX = x
        coordY = y
    }

    private fun onTouch(v: View, event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            currentHolder?.let {
                if (!it.isHolderOpen()) return
                val rectL = Rect()
                currentHolder?.itemView?.findViewById<ImageView>(R.id.ivLike)?.let {
                    it.getGlobalVisibleRect(rectL)
                    if (coordX > rectL.left
                        && coordX < rectL.right
                        && coordY > rectL.top
                        && coordY < rectL.bottom
                    ) {
                        val holderPosition = currentHolder!!.adapterPosition
                        if (holderPosition >= 0) {
                            val vacancy = adapter.currentList.get(holderPosition)
                            viewModel.switchFavorite(vacancy.id, holderPosition)
                        }
                    }
                }
                it.isOpen = false
            }
        }
    }

    private fun closeHolder(vh: VacanciesAdapter.ViewHolder, needUpdate: Boolean = false) {
        // nowClosed = true
        if (!vh.isHolderOpen()) return
        vh.isOpen = false

        val ivLike = vh.itemView.findViewById<ImageView>(R.id.ivLike)
        val ivLikeSmall = vh.itemView.findViewById<ImageView>(R.id.ivAddToFav)
        var targetAlpha = 0f
        if (vh.adapterPosition >= 0) {
            val vacancy = adapter.currentList.get(vh.adapterPosition)
            targetAlpha = if (vacancy.isFavorite) 1f else 0f
        }

        val anim =
            ResizeAnimationWithAlpha(ivLike, ivLikeSmall, UtilFunction.dpToPx(0f, requireContext()), targetAlpha)
        //ivLike.clearAnimation()
        anim.setAnimationListener(object : Animation(), Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) = Unit
            override fun onAnimationEnd(animation: Animation?) {
                // nowClosed = false

                if (needUpdate) {
                    adapter.notifyItemChanged(vh.layoutPosition)
                    vh.updateFavIco(vacancy = adapter.currentList.get(vh.adapterPosition))
                }
            }

            override fun onAnimationRepeat(animation: Animation?) = Unit

            override fun cancel() {
                super.cancel()
                //nowClosed = false
                adapter.notifyItemChanged(vh.layoutPosition)
                if (vh.adapterPosition >= 0) {
                    vh.updateFavIco(vacancy = adapter.currentList.get(vh.adapterPosition))
                }
            }
        })
        ivLike.startAnimation(anim)
    }

    private fun updateStatus(state: ResponseState) {
        updatePreStatus(state)
        when (state) {
            ResponseState.Empty -> {
                binding.srcText.setText(R.string.no_vacancies)
                binding.foundCountText.setText(R.string.no_vacancies_lil)
            }

            ResponseState.ServerError -> binding.srcText.setText(R.string.server_error)

            ResponseState.NetworkError(false) -> binding.srcText.setText(R.string.no_internet)

            ResponseState.NetworkError(true) -> {
                binding.src.isVisible = false
                Toast.makeText(requireContext(), "Ошибка соединения", Toast.LENGTH_SHORT).show()
            }

            else -> binding.srcText.text = ""
        }
        updatePicture(state)

        binding.vacanciesProgress.isVisible = when (state) {
            is ResponseState.Loading -> state.isPagination
            else -> false
        }

        binding.foundCountText.text = when (state) {
            is ResponseState.ContentVacanciesList -> {
                if (state.found > 0) {
                    getString(R.string.found_count, state.found.toString()).plus(" ").plus(getNoun(state.found))
                } else {
                    null
                }
            }

            else -> getString(R.string.no_vacancies_lil)
        }

        binding.vacanciesLoading.isVisible = when (state) {
            is ResponseState.Loading -> !state.isPagination
            else -> false
        }
        if (state is ResponseState.Loading) hideKeyBoard()
    }

    private fun updatePreStatus(state: ResponseState) {
        binding.vacanciesRv.isVisible = when (state) {
            is ResponseState.Loading -> state.isPagination
            is ResponseState.ContentVacanciesList -> true
            is ResponseState.NetworkError -> true
            else -> false
        }
        binding.src.visibleOrGone(state !is ResponseState.Loading && state !is ResponseState.ContentVacanciesList)
        binding.srcText.visibleOrGone(binding.src.isVisible)
        binding.foundCount.visibleOrGone(state is ResponseState.ContentVacanciesList || state is ResponseState.Empty)
    }

    private fun updatePicture(status: ResponseState) {
        val image = when (status) {
            is ResponseState.Empty -> {
                R.drawable.state_image_nothing_found
            }

            is ResponseState.ServerError -> {
                R.drawable.state_image_server_error_search
            }

            is ResponseState.NetworkError -> {
                R.drawable.state_image_no_internet
            }

            else -> {
                R.drawable.state_image_start_search
            }
        }

        image.let {
            Glide.with(requireContext())
                .load(image)
                .into(binding.src)
        }
    }

    private fun checkFilters(isWithFilters: Boolean) {
        val image = when (isWithFilters) {
            true -> R.drawable.ic_filter_on
            else -> R.drawable.ic_filter
        }
        image.let {
            Glide.with(requireContext())
                .load(image)
                .into(binding.filterImageView)
        }
    }

    private fun setupVacancies() {
        binding.vacanciesRv.adapter = adapter
        binding.vacanciesRv.layoutManager = LinearLayoutManager(requireContext())
        binding.foundCount.visibleOrGone(false)

    }

    private fun hideKeyBoard() {
        if (_binding == null) return
        binding.let {
            val inputMethodManager =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
        }
    }

    private fun setupIcon(it: String) {
        if (it.isNotBlank()) {
            binding.searchEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_clear, 0)
            binding.clearButton.isEnabled = true
        } else {
            hideKeyBoard()
            binding.searchEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_search, 0)
            binding.clearButton.isEnabled = false
        }
    }

    private fun getNoun(count: Int): String {
        return when (count.toString().last()) {
            '1' -> getString(R.string.NounOne)
            '2' -> getString(R.string.NounTwo)
            '3' -> getString(R.string.NounTwo)
            '4' -> getString(R.string.NounTwo)
            else -> getString(R.string.NounThree)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        currentHolder = null
        viewModel.updateData()
        activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.isVisible = true
    }
}
