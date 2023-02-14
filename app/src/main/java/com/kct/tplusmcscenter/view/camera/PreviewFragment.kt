package com.kct.tplusmcscenter.view.camera
//
//import android.content.Intent
//import com.kct.tplusmcscenter.R
//import com.kct.tplusmcscenter.base.BaseCameraFragment
//import com.kct.tplusmcscenter.databinding.FragmentPreviewBinding
//import android.os.Bundle
//import android.view.View
//import androidx.core.view.WindowInsetsCompat
//import androidx.navigation.Navigation
//import com.kct.tplusmcscenter.adapter.MediaAdapter
//import com.kct.tplusmcscenter.utils.extension.*
//import dagger.hilt.android.AndroidEntryPoint
//
//@AndroidEntryPoint
//class PreviewFragment : BaseCameraFragment<FragmentPreviewBinding>(R.layout.fragment_preview) {
//
//    private val mediaAdapter = MediaAdapter(
//        onItemClick = { isVideo, uri ->
//            if (!isVideo) {
//                val visibility = if (binding.groupPreviewActions.visibility == View.VISIBLE) View.GONE else View.VISIBLE
//                binding.groupPreviewActions.visibility = visibility
//            } else {
//                val play = Intent(Intent.ACTION_VIEW, uri).apply { setDataAndType(uri, "video/mp4") }
//                startActivity(play)
//            }
//        },
//        onDeleteClick = { isEmpty, uri ->
//            if (isEmpty) onBackPressed()
//
//            val resolver = requireContext().applicationContext.contentResolver
//            resolver.delete(uri, null, null)
//        },
//    )
//    private var currentPage = 0
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        adjustInsets()
//
//        // Check for the permissions and show files
//        if (allPermissionsGranted()) {
//            binding.pagerPhotos.apply {
//                adapter = mediaAdapter.apply { submitList(getMedia()) }
//                onPageSelected { page -> currentPage = page }
//            }
//        }
//
//        binding.btnBack.setOnClickListener { onBackPressed() }
//        binding.btnShare.setOnClickListener { shareImage() }
//        binding.btnDelete.setOnClickListener { deleteImage() }
//    }
//
//    /**
//     * This methods adds all necessary margins to some views based on window insets and screen orientation
//     * */
//    private fun adjustInsets() {
//        activity?.window?.fitSystemWindows()
//        binding.btnBack.onWindowInsets { view, windowInsets ->
//            view.topMargin = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top
//        }
//        binding.btnShare.onWindowInsets { view, windowInsets ->
//            view.bottomMargin = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
//        }
//    }
//
//    private fun shareImage() {
//        mediaAdapter.shareImage(currentPage) { share(it) }
//    }
//
//    private fun deleteImage() {
//        mediaAdapter.deleteImage(currentPage)
//    }
//
//    override fun onBackPressed() {
//        view?.let { Navigation.findNavController(it).popBackStack() }
//    }
//
//}