package com.github.llmaximll.wonderfulwallpaper.app.ui.images

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.llmaximll.wonderfulwallpaper.app.data.entities.Image
import com.github.llmaximll.wonderfulwallpaper.app.utils.Resource
import com.github.llmaximll.wonderfulwallpaper.databinding.FragmentImagesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@AndroidEntryPoint
class ImagesFragment : Fragment() {

    private var _binding: FragmentImagesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ImagesViewModel by viewModels()
    private lateinit var collector: FlowCollector<Resource<List<Image>>>
    private var stateRV: Parcelable? = null

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

        setupObservers()
        setupRecyclerView()
    }

    override fun onStop() {
        super.onStop()
        stateRV = binding.imagesRV.layoutManager?.onSaveInstanceState()
        viewModel.onSaveState(stateRV = stateRV)
    }

    private fun setupRecyclerView() {
        if (viewModel.adapter == null) viewModel.adapter = ImagesAdapter()
        binding.imagesRV.layoutManager = GridLayoutManager(requireContext(), 3)
        stateRV = viewModel.onRestoreState(ImagesViewModel.KEY_SAVE_RECYCLER_VIEW)
        binding.imagesRV.layoutManager?.onRestoreInstanceState(stateRV)
        binding.imagesRV.adapter = viewModel.adapter
        binding.imagesRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!binding.imagesRV.canScrollVertically(1)) {
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        viewModel.page++
                        collector.emitAll(viewModel.getImages(requireContext(), page = viewModel.page))
                    }
                    Toast.makeText(requireContext(), "refresh | page=${viewModel.page}", Toast.LENGTH_SHORT).show()
                }
            }
        })
        if (this::collector.isInitialized) {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                collector.emitAll(viewModel.getImages(requireContext()))
            }
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                collector = object : FlowCollector<Resource<List<Image>>> {
                    override suspend fun emit(value: Resource<List<Image>>) {
                        when (value.status) {
                            Resource.Status.LOADING -> {
                                withContext(Dispatchers.Main) {
                                    binding.progressBar.visibility = View.VISIBLE
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
                                    binding.progressBar.visibility = View.GONE
                                }
                                Timber.v("success | ${value.data}")
                            }
                            Resource.Status.ERROR -> {
                                Toast.makeText(requireContext(), "Ошибка загрузки данных", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
                viewModel.getImages(requireContext()).collect { collector.emit(it) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}