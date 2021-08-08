package com.github.llmaximll.wonderfulwallpaper.app.ui.images

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.llmaximll.wonderfulwallpaper.R
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.Image
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.Parameters
import com.github.llmaximll.wonderfulwallpaper.app.utils.Resource
import com.github.llmaximll.wonderfulwallpaper.databinding.FragmentImagesBinding
import com.google.android.material.chip.ChipGroup
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@AndroidEntryPoint
class ImagesFragment : Fragment() {

    interface Callbacks {
        fun onItemClicked(parameters: Parameters)
    }

    private var _binding: FragmentImagesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ImagesViewModel by viewModels()
    private lateinit var collector: FlowCollector<Resource<List<Image>>>
    private var stateRV: Parcelable? = null
    private var callbacks: Callbacks? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolBar()
        setupObservers()
        setupRecyclerView()
    }

    override fun onStart() {
        super.onStart()
        binding.clickableImageView.setOnClickListener { viewModel.toggleMainState(true) }
    }

    override fun onStop() {
        super.onStop()
        stateRV = binding.imagesRV.layoutManager?.onSaveInstanceState()
        viewModel.onSaveState(stateRV = stateRV)
    }

    private fun setupRecyclerView() {
        if (viewModel.adapter == null) viewModel.adapter = ImagesAdapter(callbacks, viewModel, requireContext())
        binding.imagesRV.layoutManager = GridLayoutManager(requireContext(), 3)
        stateRV = viewModel.onRestoreState(ImagesViewModel.KEY_SAVE_RECYCLER_VIEW)
        binding.imagesRV.layoutManager?.onRestoreInstanceState(stateRV)
        binding.imagesRV.adapter = viewModel.adapter
        binding.imagesRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!binding.imagesRV.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        viewModel.page++
                        collector.emitAll(viewModel.getImages(requireContext()))
                    }
                }
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupToolBar() {
        binding.progressBar.setVisibilityAfterHide(View.INVISIBLE)
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.adapter = ImagesAdapter(callbacks, viewModel, requireContext())
                binding.imagesRV.adapter = viewModel.adapter
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.q = binding.searchEditText.text.toString().replace(" ", "+")
                    viewModel.page = 1
                    collector.emitAll(viewModel.getImages(requireContext(), viewModel.page))
                }
            }
            false
        }
        binding.filterImageView.setOnClickListener {
            viewModel.toggleMainState()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    collector = object : FlowCollector<Resource<List<Image>>> {
                        override suspend fun emit(value: Resource<List<Image>>) {
                            when (value.status) {
                                Resource.Status.LOADING -> {
                                    withContext(Dispatchers.Main) {
                                        binding.progressBar.show()
                                    }
                                    Timber.v("loading")
                                }
                                Resource.Status.SUCCESS -> {
                                    withContext(Dispatchers.Main) {
                                        if (!value.data.isNullOrEmpty() &&
                                            value.data[1].id != viewModel.adapter?.recentItems?.getOrNull(1)?.id
                                        ) {
                                            viewModel.adapter?.apply {
                                                addItems(value.data)
                                                recentItems.clear()
                                                recentItems.addAll(value.data)
                                                Timber.v("Элементы добавлены")
                                            }
                                        }
                                        binding.progressBar.hide()
                                    }
                                    Timber.v("success | ${value.data?.size}")
                                }
                                Resource.Status.ERROR -> {
                                    withContext(Dispatchers.Main) {
                                        binding.progressBar.hide()
                                        Toast.makeText(requireContext(), "${value.message}", Toast.LENGTH_LONG).show()
                                    }
                                    Timber.v("Ошибка загрузки данных | ${value.message}")
                                }
                            }
                        }
                    }
                }
                launch(Dispatchers.Main) {
                    viewModel.mainState.collect { state ->
                        if (!state) {
                            binding.motionLayout.transitionToState(R.id.end)
                            binding.clickableImageView.animate().apply {
                                withStartAction { binding.clickableImageView.visibility = View.VISIBLE }
                                alpha(0.7f)
                            }
                        } else {
                            binding.motionLayout.transitionToState(R.id.start)
                            binding.clickableImageView.animate().apply {
                                alpha(0.0f)
                                withEndAction { binding.clickableImageView.visibility = View.GONE }
                            }
                            val chipGroupList: List<ChipGroup>
                            binding.includeFilter.run {
                                chipGroupList = listOf(
                                    imageTypeGroup,
                                    orientationGroup,
                                    categoryGroup,
                                    colorsGroup,
                                    choiceGroup
                                )
                            }
                            if (viewModel.checkChangeState(chipGroupList)) {
                                viewModel.adapter = ImagesAdapter(callbacks, viewModel, requireContext())
                                binding.imagesRV.adapter = viewModel.adapter
                                viewModel.page = 1
                                viewModel.setParamListFromTags(chipGroupList)
                                Timber.v("""
                                imageType=${viewModel.imageType}
                                orientation=${viewModel.orientation}
                                category=${viewModel.category}
                                colors=${viewModel.colors}
                                editorsChoice=${viewModel.editorsChoice}
                                """.trimIndent())
                                collector.emitAll(viewModel.getImages(requireContext()))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
        _binding = null
    }

    companion object {
        const val ARG_PARAMETERS = "arg_parameters"
    }
}