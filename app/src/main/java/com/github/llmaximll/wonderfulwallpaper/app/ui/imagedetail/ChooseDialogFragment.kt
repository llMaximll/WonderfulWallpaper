package com.github.llmaximll.wonderfulwallpaper.app.ui.imagedetail

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.github.llmaximll.wonderfulwallpaper.databinding.DialogChooseModeWallpaperBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChooseDialogFragment : DialogFragment() {

    private var _binding: DialogChooseModeWallpaperBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogChooseModeWallpaperBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val manager: WallpaperManager = WallpaperManager.getInstance(context?.applicationContext)
        binding.lockButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val wallpaper = downloadWallpaper()
                    withContext(Dispatchers.Main) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            manager.setBitmap(
                                wallpaper,
                                null,
                                false,
                                WallpaperManager.FLAG_LOCK
                            )
                            Toast.makeText(requireContext(), "Установлено", Toast.LENGTH_SHORT).show()
                            dismiss()
                        } else {
                            Toast.makeText(requireContext(), "Не поддерживается", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        binding.desktopButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val wallpaper = downloadWallpaper()
                    withContext(Dispatchers.Main) {
                        manager.setBitmap(wallpaper)
                        Toast.makeText(requireContext(), "Установлено", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun downloadWallpaper(): Bitmap? =
        try {
            Glide.with(this@ChooseDialogFragment).load(arguments?.getString(KEY_IMAGE_URL))
                .submit().get().toBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    override fun onDetach() {
        super.onDetach()
        _binding = null
    }

    companion object {
        fun newInstance(imageURL: String): ChooseDialogFragment {
            val args = Bundle().apply {
                putString(KEY_IMAGE_URL, imageURL)
            }
            return ChooseDialogFragment().apply { arguments = args }
        }
        private const val KEY_IMAGE_URL = "key_image_url"
    }
}