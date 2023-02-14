package com.kct.tplusmcscenter.view.camera
//
//import com.kct.tplusmcscenter.R
//import com.kct.tplusmcscenter.base.BaseCameraFragment
//import android.annotation.SuppressLint
//import android.content.ContentValues
//import android.content.Context
//import android.content.res.Configuration
//import android.hardware.display.DisplayManager
//import android.net.Uri
//import android.os.Build
//import android.os.Bundle
//import android.provider.MediaStore
//import android.util.DisplayMetrics
//import android.util.Log
//import android.view.View
//import android.widget.Toast
//import androidx.camera.core.*
//import androidx.camera.core.ImageCapture.*
//import androidx.camera.extensions.ExtensionMode
//import androidx.camera.extensions.ExtensionsManager
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.view.PreviewView
//import androidx.core.content.ContextCompat
//import androidx.core.view.WindowInsetsCompat
//import androidx.fragment.app.viewModels
//import androidx.lifecycle.lifecycleScope
//import androidx.navigation.Navigation
//import coil.fetch.VideoFrameUriFetcher
//import coil.load
//import coil.request.ImageRequest
//import coil.transform.CircleCropTransformation
//import com.kct.tplusmcscenter.model.enums.CameraTimer
//import com.kct.tplusmcscenter.model.objects.SharedPreferenceKey.KEY_FLASH
//import com.kct.tplusmcscenter.model.objects.SharedPreferenceKey.KEY_GRID
//import com.kct.tplusmcscenter.model.objects.SharedPreferenceKey.KEY_HDR
//import com.kct.tplusmcscenter.utils.extension.*
//import com.google.android.material.snackbar.Snackbar
//import com.kct.tplusmcscenter.databinding.FragmentCameraBinding
//import dagger.hilt.android.AndroidEntryPoint
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import java.io.File
//import java.util.concurrent.ExecutionException
//import kotlin.math.abs
//import kotlin.math.max
//import kotlin.math.min
//import kotlin.properties.Delegates
//
////@AndroidEntryPoint
//class CameraFragment : BaseCameraFragment<FragmentCameraBinding>(R.layout.fragment_camera) {
//    companion object {
//        private const val TAG = "[CameraFragment]"
//
//        private const val RATIO_4_3_VALUE = 4.0 / 3.0 // aspect ratio 4x3
//        private const val RATIO_16_9_VALUE = 16.0 / 9.0 // aspect ratio 16x9
//    }
//
//    private val viewModel: CameraFragmentViewModel by viewModels()
//
//    // An instance for display manager to get display change callbacks
//    private val displayManager by lazy { requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager }
//
//    private var preview: Preview? = null
//    private var cameraProvider: ProcessCameraProvider? = null
//    private var imageCapture: ImageCapture? = null
//    private var imageAnalyzer: ImageAnalysis? = null
//
//    private var displayId = -1
//
//    // Selector showing which camera is selected (front or back)
//    private var lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
//    private var hdrCameraSelector: CameraSelector? = null
//
//    // Selector showing which flash mode is selected (on, off or auto)
//    private var flashMode by Delegates.observable(FLASH_MODE_OFF) { _, _, new ->
//        binding.btnFlash.setImageResource(
//            when (new) {
//                FLASH_MODE_ON -> R.drawable.ic_flash_on
//                FLASH_MODE_AUTO -> R.drawable.ic_flash_auto
//                else -> R.drawable.ic_flash_off
//            }
//        )
//    }
//
//    // Selector showing is there any selected timer and it's value (3s or 10s)
//    private var selectedTimer = CameraTimer.OFF
//
//    /*
//     * A display listener for orientation changes that do not trigger a configuration
//     * change, for example if we choose to override config change in manifest or for 180-degree
//     * orientation changes.
//     */
//    private val displayListener = object : DisplayManager.DisplayListener {
//
//        override fun onDisplayAdded(displayId: Int) = Unit
//
//        override fun onDisplayRemoved(displayId: Int) = Unit
//
//        @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
//        override fun onDisplayChanged(displayId: Int)
//        = view?.let { view ->
//            if (displayId == this@CameraFragment.displayId) {
//                preview?.targetRotation = view.display.rotation
//                imageCapture?.targetRotation = view.display.rotation
//                imageAnalyzer?.targetRotation = view.display.rotation
//            }
//        } ?: Unit
//    }
//
//    @SuppressLint("ClickableViewAccessibility")
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        flashMode = viewModel.flashMode
//        initViews()
//
//        displayManager.registerDisplayListener(displayListener, null)
//
//        binding.run {
//            viewFinder.addOnAttachStateChangeListener(
//                object : View.OnAttachStateChangeListener {
//                    override fun onViewDetachedFromWindow(v: View) = displayManager.registerDisplayListener(displayListener, null)
//
//                    override fun onViewAttachedToWindow(v: View) = displayManager.unregisterDisplayListener(displayListener)
//                }
//            )
//
//            // This swipe gesture adds a fun gesture to switch between video and photo
////            val swipeGestures = SwipeGestureDetector().apply {
////                setSwipeCallback(right = {
////                    Navigation.findNavController(view).navigate(R.id.action_camera_to_video)
////                })
////            }
////            val gestureDetectorCompat = GestureDetector(requireContext(), swipeGestures)
////            viewFinder.setOnTouchListener { _, motionEvent ->
////                if (gestureDetectorCompat.onTouchEvent(motionEvent)) return@setOnTouchListener false
////                return@setOnTouchListener true
////            }
//            btnTakePicture.setOnClickListener { takePicture() }
//            btnGallery.setOnClickListener { openPreview() }
//            btnSwitchCamera.setOnClickListener { toggleCamera() }
//            btnTimer.setOnClickListener { selectTimer() }
//            btnGrid.setOnClickListener { toggleGrid() }
//            btnFlash.setOnClickListener { selectFlash() }
//            btnHdr.setOnClickListener { toggleHdr() }
//            btnTimerOff.setOnClickListener { closeTimerAndSelect(CameraTimer.OFF) }
//            btnTimer3.setOnClickListener { closeTimerAndSelect(CameraTimer.S3) }
//            btnTimer10.setOnClickListener { closeTimerAndSelect(CameraTimer.S10) }
//            btnFlashOff.setOnClickListener { closeFlashAndSelect(FLASH_MODE_OFF) }
//            btnFlashOn.setOnClickListener { closeFlashAndSelect(FLASH_MODE_ON) }
//            btnFlashAuto.setOnClickListener { closeFlashAndSelect(FLASH_MODE_AUTO) }
//            btnExposure.setOnClickListener { binding.flExposure.visibility = View.VISIBLE }
//            flExposure.setOnClickListener { binding.flExposure.visibility = View.GONE }
//
//        }
//    }
//
//    /**
//     * @func: initViews
//     * @param: -
//     * @return: Unit
//     * @role: initial states
//     */
//    private fun initViews() {
//        binding.btnGrid.setImageResource(if (viewModel.hasGrid) R.drawable.ic_grid_on else R.drawable.ic_grid_off)
//        binding.groupGridLines.visibility = if (viewModel.hasGrid) View.VISIBLE else View.GONE
//        adjustInsets()
//    }
//
//    /**
//     * @func: adjustInsets
//     * @param: -
//     * @return: Unit
//     * @role: Adds all necessary margins to some views based on window insets and screen orientation
//     */
//    private fun adjustInsets() {
//        activity?.window?.fitSystemWindows()
//        binding.run {
//            btnTakePicture.onWindowInsets { view, windowInsets ->
//                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
//                    view.bottomMargin = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
//                } else {
//                    view.endMargin = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).right
//                }
//            }
//            btnTimer.onWindowInsets { view, windowInsets ->
//                view.topMargin = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top
//            }
//            llTimerOptions.onWindowInsets { view, windowInsets ->
//                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
//                    view.topPadding = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top
//                } else {
//                    view.startPadding = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).left
//                }
//            }
//            llFlashOptions.onWindowInsets { view, windowInsets ->
//                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
//                    view.topPadding = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top
//                } else {
//                    view.startPadding = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).left
//                }
//            }
//        }
//    }
//
//    /**
//     * @func: toggleCamera
//     * @param: -
//     * @return: Unit
//     * @role: Change the facing of camera
//     * @etc: toggleButton() - Extension function made to animate button rotation
//     */
//    @SuppressLint("RestrictedApi")
//    fun toggleCamera() = binding.btnSwitchCamera.toggleButton(
//        flag = lensFacing == CameraSelector.DEFAULT_BACK_CAMERA,
//        rotationAngle = 180f,
//        firstIcon = R.drawable.ic_outline_camera_rear,
//        secondIcon = R.drawable.ic_outline_camera_front,
//    ) {
//        lensFacing = if (it) { CameraSelector.DEFAULT_BACK_CAMERA } else { CameraSelector.DEFAULT_FRONT_CAMERA }
//        startCamera()
//    }
//
//    /**
//     * @func: openPreview
//     * @param: -
//     * @return: Unit
//     * @role: Navigate to PreviewFragment
//     */
//    private fun openPreview() {
//        if (getMedia().isEmpty()) return
//        view?.let { Navigation.findNavController(it).navigate(R.id.action_camera_to_preview) }
//    }
//
//    /**
//     * @func: selectTimer
//     * @param: -
//     * @return: Unit
//     * @role: Show timer selection menu by circular reveal animation.
//     * @etc: circularReveal() - Extension function which is adding the circular reveal
//     */
//    private fun selectTimer() = binding.llTimerOptions.circularReveal(binding.btnTimer)
//
//    /**
//     * @func: closeTimerAndSelect
//     * @param timer: CameraTimer (possible values are OFF, S3 or S10)
//     * @return: Unit
//     * @role: This function is called from XML view via Data Binding to select a timer
//     * @etc: circularClose() - Extension function which is adding circular close
//     */
//    private fun closeTimerAndSelect(timer: CameraTimer)
//    = binding.run {
//        llTimerOptions.circularClose(btnTimer) {
//            selectedTimer = timer
//            btnTimer.setImageResource(
//                when (timer) {
//                    CameraTimer.S3 -> R.drawable.ic_timer_3
//                    CameraTimer.S10 -> R.drawable.ic_timer_10
//                    CameraTimer.OFF -> R.drawable.ic_timer_off
//                }
//            )
//        }
//    }
//
//    /**
//     * @func: selectFlash
//     * @param: -
//     * @return: Unit
//     * @role: Show flashlight selection menu by circular reveal animation.
//     * @etc: circularReveal() - Extension function which is adding the circular reveal
//     */
//    private fun selectFlash() = binding.run { llFlashOptions.circularReveal(btnFlash) }
//
//    /**
//     * @func: closeFlashAndSelect
//     * @param flash: Int (possible values are ON, OFF or AUTO)
//     * @return: Unit
//     * @role: is called from XML view via Data Binding to select a FlashMode
//     * @etc: circularClose() - Extension function which is adding circular close
//     */
//    private fun closeFlashAndSelect(@FlashMode flash: Int)
//    = binding.run {
//        llFlashOptions.circularClose(btnFlash) {
//            flashMode = flash
//            btnFlash.setImageResource(
//                when (flash) {
//                    FLASH_MODE_ON -> R.drawable.ic_flash_on
//                    FLASH_MODE_OFF -> R.drawable.ic_flash_off
//                    else -> R.drawable.ic_flash_auto
//                }
//            )
//            imageCapture?.flashMode = flashMode
//            viewModel.savesPref(KEY_FLASH, flashMode)
//        }
//    }
//
//    /**
//     * @func: toggleGrid
//     * @param: -
//     * @return: Unit
//     * @role: Turns on or off the grid on the screen
//     */
//    private fun toggleGrid() = binding.btnGrid.toggleButton(
//        flag = viewModel.hasGrid,
//        rotationAngle = 180f,
//        firstIcon = R.drawable.ic_grid_off,
//        secondIcon = R.drawable.ic_grid_on,
//    ) { flag ->
//        viewModel.savesPref(KEY_GRID, flag)
//        binding.groupGridLines.visibility = if (flag) View.VISIBLE else View.GONE
//    }
//
//    /**
//     * @func: toggleHdr
//     * @param: -
//     * @return: Unit
//     * @role: Turns on or off the HDR if available
//     */
//    private fun toggleHdr() = binding.btnHdr.toggleButton(
//        flag = viewModel.hasHdr,
//        rotationAngle = 360f,
//        firstIcon = R.drawable.ic_hdr_off,
//        secondIcon = R.drawable.ic_hdr_on,
//    ) { flag ->
//        viewModel.savesPref(KEY_HDR, flag)
//        startCamera()
//    }
//
//    /**
//     * @func: onPermissionGranted
//     * @param: -
//     * @return: Unit
//     * @role: Each time apps is coming to foreground the need permission check is being processed
//     */
//    override fun onPermissionGranted() {
//        binding.viewFinder.let { vf ->
//            vf.post {
//                // Setting current display ID
//                displayId = vf.display.displayId
//                startCamera()
//                lifecycleScope.launch(Dispatchers.IO) {
//                    // Do on IO Dispatcher
//                    setLastPictureThumbnail()
//                }
//            }
//        }
//    }
//
//    /**
//     * @func: setLastPictureThumbnail
//     * @param: -
//     * @return: Boolean
//     * @role: Check if there are any photos or videos in the app directory, And set Thumbnail
//     */
//    private fun setLastPictureThumbnail() = binding.btnGallery.post {
//        getMedia().firstOrNull()?.let { setGalleryThumbnail(it.uri) } // preview the last one
//            ?: binding.btnGallery.setImageResource(R.drawable.ic_no_picture) // or the default placeholder
//    }
//
//    /**
//     * @func: startCamera
//     * @param: -
//     * @return: Unit
//     * @role: Unbinds all the lifecycles from CameraX, then creates new with new parameters
//     */
//    private fun startCamera() {
//        // This is the CameraX PreviewView where the camera will be rendered
//        val viewFinder = binding.viewFinder
//
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
//        cameraProviderFuture.addListener(
//            {
//                try {
//                    cameraProvider = cameraProviderFuture.get()
//                } catch (e: InterruptedException) {
////                    Snackbar.make(requireView(), "Error starting camera", Snackbar.LENGTH_SHORT).show()
//                    Toast.makeText(requireContext(), "Error starting camera", Toast.LENGTH_SHORT).show()
//                    return@addListener
//                } catch (e: ExecutionException) {
//                    Snackbar.make(requireView(), "Error starting camera", Snackbar.LENGTH_SHORT).show()
////                    Toast.makeText(requireContext(), "Error starting camera", Toast.LENGTH_SHORT).show()
//                    return@addListener
//                }
//
//                // The display information
//                val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
//                // The ratio for the output image and preview
//                val aspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
//                // The display rotation
//                val rotation = viewFinder.display.rotation
//
//                val localCameraProvider = cameraProvider
//                    ?: throw IllegalStateException("Camera initialization failed.")
//
//                // The Configuration of camera preview
//                preview = Preview.Builder()
//                    .setTargetAspectRatio(aspectRatio) // set the camera aspect ratio
//                    .setTargetRotation(rotation) // set the camera rotation
//                    .build()
//
//                // The Configuration of image capture
//                imageCapture = Builder()
//                    .setCaptureMode(CAPTURE_MODE_MAXIMIZE_QUALITY) // setting to have pictures with highest quality possible (may be slow)
//                    .setFlashMode(flashMode) // set capture flash
//                    .setTargetAspectRatio(aspectRatio) // set the capture aspect ratio
//                    .setTargetRotation(rotation) // set the capture rotation
//                    .build()
//
//                checkForHdrExtensionAvailability()
//
//                // The Configuration of image analyzing
//                imageAnalyzer = ImageAnalysis.Builder()
//                    .setTargetAspectRatio(aspectRatio) // set the analyzer aspect ratio
//                    .setTargetRotation(rotation) // set the analyzer rotation
//                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // in our analysis, we care about the latest image
//                    .build()
//                    .also { viewModel.setLuminosityAnalyzer(it) }
//
//                // Unbind the use-cases before rebinding them
//                localCameraProvider.unbindAll()
//                // Bind all use cases to the camera with lifecycle
//                bindToLifecycle(localCameraProvider, viewFinder)
//            },
//            ContextCompat.getMainExecutor(requireContext())
//        )
//    }
//
//    /**
//     * @func: bindToLifecycle
//     * @param localCameraProvider: ProcessCameraProvider
//     * @param viewFinder: PreviewView
//     * @return: Unit
//     * @role: Check for Hdr Extension availability
//     */
//    private fun bindToLifecycle(
//        localCameraProvider: ProcessCameraProvider,
//        viewFinder: PreviewView
//    ) {
//        try {
//            localCameraProvider.bindToLifecycle(
//                viewLifecycleOwner, // current lifecycle owner
//                hdrCameraSelector ?: lensFacing, // either front or back facing
//                preview, // camera preview use case
//                imageCapture, // image capture use case
//                imageAnalyzer, // image analyzer use case
//            ).run {
//                // Init camera exposure control
//                cameraInfo.exposureState.run {
//                    val lower = exposureCompensationRange.lower
//                    val upper = exposureCompensationRange.upper
//
//                    binding.sliderExposure.run {
//                        valueFrom = lower.toFloat()
//                        valueTo = upper.toFloat()
//                        stepSize = 1f
//                        value = exposureCompensationIndex.toFloat()
//
//                        addOnChangeListener { _, value, _ ->
//                            cameraControl.setExposureCompensationIndex(value.toInt())
//                        }
//                    }
//                }
//            }
//
//            // Attach the viewfinder's surface provider to preview use case
//            preview?.setSurfaceProvider(viewFinder.surfaceProvider)
//        } catch (e: Exception) {
//            Log.e(TAG, "Failed to bind use cases", e)
//        }
//    }
//
//    /**
//     * @func: checkForHdrExtensionAvailability
//     * @param: -
//     * @return: Unit
//     * @role: Check for Hdr Extension availability
//     */
//    private fun checkForHdrExtensionAvailability() {
//        // Create a Vendor Extension for HDR
//        val extensionsManagerFuture = ExtensionsManager.getInstanceAsync(
//            requireContext(),
//            cameraProvider ?: return
//        )
//        extensionsManagerFuture.addListener(
//            {
//                val extensionsManager = extensionsManagerFuture.get() ?: return@addListener
//                val cameraProvider = cameraProvider ?: return@addListener
//
//                val isAvailable = extensionsManager.isExtensionAvailable(lensFacing, ExtensionMode.HDR)
//
//                // check for any extension availability
//                println("AUTO " + extensionsManager.isExtensionAvailable(lensFacing, ExtensionMode.AUTO))
//                println("HDR " + extensionsManager.isExtensionAvailable(lensFacing, ExtensionMode.HDR))
//                println("FACE RETOUCH " + extensionsManager.isExtensionAvailable(lensFacing, ExtensionMode.FACE_RETOUCH))
//                println("BOKEH " + extensionsManager.isExtensionAvailable(lensFacing, ExtensionMode.BOKEH))
//                println("NIGHT " + extensionsManager.isExtensionAvailable(lensFacing, ExtensionMode.NIGHT))
//                println("NONE " + extensionsManager.isExtensionAvailable(lensFacing, ExtensionMode.NONE))
//
//                // Check if the extension is available on the device
//                if (!isAvailable) {
//                    // If not, hide the HDR button
//                    binding.btnHdr.visibility = View.GONE
//                } else if (viewModel.hasHdr) {
//                    // If yes, turn on if the HDR is turned on by the user
//                    binding.btnHdr.visibility = View.VISIBLE
//                    hdrCameraSelector =
//                        extensionsManager.getExtensionEnabledCameraSelector(lensFacing, ExtensionMode.HDR)
//                }
//            },
//            ContextCompat.getMainExecutor(requireContext())
//        )
//    }
//
//    /**
//     * @func: aspectRatio
//     * @param width: Int
//     * @param height: Int
//     * @return: Int (suitable aspect ratio)
//     * @role: Detecting the most suitable aspect ratio for current dimensions
//     */
//    private fun aspectRatio(
//        width: Int,
//        height: Int
//    ): Int {
//        val previewRatio = max(width, height).toDouble() / min(width, height)
//        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
//            return AspectRatio.RATIO_4_3
//        }
//        return AspectRatio.RATIO_16_9
//    }
//
//    /**
//     * @func: takePicture
//     * @param: -
//     * @return: Job (LifecycleScope - Dispatchers.Main)
//     * @role: Take a Picture
//     */
//    @Suppress("NON_EXHAUSTIVE_WHEN")
//    private fun takePicture() = lifecycleScope.launch(Dispatchers.Main) {
//        // Show a timer based on user selection
//        when (selectedTimer) {
//            CameraTimer.S3 -> for (i in 3 downTo 1) {
//                binding.tvCountDown.text = i.toString()
//                delay(1000)
//            }
//            CameraTimer.S10 -> for (i in 10 downTo 1) {
//                binding.tvCountDown.text = i.toString()
//                delay(1000)
//            }
//            else -> {  }
//        }
//        binding.tvCountDown.text = ""
//        captureImage()
//    }
//
//    /**
//     * @func: captureImage
//     * @param: -
//     * @return: Unit
//     * @role: Capture a Image
//     */
//    private fun captureImage() {
//        val localImageCapture = imageCapture ?: throw IllegalStateException("Camera initialization failed.")
//
//        // Setup image capture metadata
//        val metadata = Metadata().apply {
//            // Mirror image when using the front camera
//            isReversedHorizontal = lensFacing == CameraSelector.DEFAULT_FRONT_CAMERA
//        }
//        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//        // Options fot the output image file
//        val outputOptions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            val contentValues = ContentValues().apply {
//                put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis())
//                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
//                put(MediaStore.MediaColumns.RELATIVE_PATH, outputDirectory)
//            }
//
//            val contentResolver = requireContext().contentResolver
//
//            // Create the output uri
//            val contentUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
//
//            OutputFileOptions.Builder(contentResolver, contentUri, contentValues)
//        } else {
//            File(outputDirectory).mkdirs()
//            val file = File(outputDirectory, "${System.currentTimeMillis()}.jpg")
//
//            OutputFileOptions.Builder(file)
//        }.setMetadata(metadata).build()
//
//        localImageCapture.takePicture(
//            outputOptions, // the options needed for the final image
//            requireContext().mainExecutor(), // the executor, on which the task will run
//            object : OnImageSavedCallback { // the callback, about the result of capture process
//                override fun onImageSaved(outputFileResults: OutputFileResults) {
//                    // This function is called if capture is successfully completed
//                    outputFileResults.savedUri?.let { uri ->
//                            setGalleryThumbnail(uri)
//                            Log.d(TAG, "Photo saved in $uri")
//                        } ?: setLastPictureThumbnail()
//                }
//
//                override fun onError(exception: ImageCaptureException) {
//                    // This function is called if there is an errors during capture process
//                    val msg = "Photo capture failed: ${exception.message}"
//                    Snackbar.make(requireView(), msg, Snackbar.LENGTH_SHORT).show()
////                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
//                    Log.e(TAG, msg)
//                    exception.printStackTrace()
//                }
//            }
//        )
//    }
//
//    /**
//     * @func: setGalleryThumbnail
//     * @param savedUri: Uri? (@Nullable)
//     * @return: Disposable
//     * @role: Setting Gallery Thumbnail
//     */
//    private fun setGalleryThumbnail(savedUri: Uri?) = binding.btnGallery.load(savedUri) {
//        placeholder(R.drawable.ic_no_picture)
//        transformations(CircleCropTransformation())
//        listener(object : ImageRequest.Listener {
//            override fun onError(request: ImageRequest, throwable: Throwable) {
//                super.onError(request, throwable)
//                binding.btnGallery.load(savedUri) {
//                    placeholder(R.drawable.ic_no_picture)
//                    transformations(CircleCropTransformation())
//                    fetcher(VideoFrameUriFetcher(requireContext()))
//                }
//            }
//        })
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        displayManager.unregisterDisplayListener(displayListener)
//    }
//
//    override fun onBackPressed() = when {
//        binding.llTimerOptions.visibility == View.VISIBLE -> binding.llTimerOptions.circularClose(binding.btnTimer)
//        binding.llFlashOptions.visibility == View.VISIBLE -> binding.llFlashOptions.circularClose(binding.btnFlash)
//        else -> requireActivity().finish()
//    }
//
//}