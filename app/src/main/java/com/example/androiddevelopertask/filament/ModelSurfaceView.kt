package com.example.androiddevelopertask.filament

import android.content.Context
import android.util.AttributeSet
import android.view.Choreographer
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.google.android.filament.Camera
import com.google.android.filament.EntityManager
import com.google.android.filament.LightManager
import com.google.android.filament.Scene
import com.google.android.filament.SwapChain
import com.google.android.filament.View
import com.google.android.filament.Viewport
import com.google.android.filament.gltfio.FilamentAsset
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.tan

class ModelSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : SurfaceView(context, attrs), SurfaceHolder.Callback, Choreographer.FrameCallback {

    private val engine = FilamentEngineHolder.ensureInitialized()
    private val renderer = engine.createRenderer()
    private val view: View = engine.createView()
    private val scene: Scene = engine.createScene()

    private val cameraEntity = EntityManager.get().create()
    private val camera: Camera = engine.createCamera(cameraEntity)

    private val keyLightEntity = EntityManager.get().create()
    private val fillLightEntity = EntityManager.get().create()

    private var swapChain: SwapChain? = null
    private var asset: FilamentAsset? = null
    private val choreographer = Choreographer.getInstance()
    private var frameCallbackPosted = false
    private var attachedToWindow = false

    private val target = floatArrayOf(0f, 0f, 0f)
    private var distance = 4f
    private var initialDistance = 4f
    private var yaw = 0f
    private var pitch = 0f
    private var dirty = true

    init {

        holder.addCallback(this)

        view.scene = scene
        view.camera = camera

        view.antiAliasing = View.AntiAliasing.NONE
        view.dithering = View.Dithering.NONE
        view.setShadowingEnabled(false)
        view.bloomOptions = view.bloomOptions.apply { enabled = false }
        view.ambientOcclusionOptions =
            view.ambientOcclusionOptions.apply { enabled = false }
        view.temporalAntiAliasingOptions =
            view.temporalAntiAliasingOptions.apply { enabled = false }
        view.screenSpaceReflectionsOptions =
            view.screenSpaceReflectionsOptions.apply { enabled = false }

        view.dynamicResolutionOptions = view.dynamicResolutionOptions.apply {
            enabled = true
            quality = View.QualityLevel.LOW
            minScale = 0.6f
            maxScale = 1.0f
        }

        renderer.clearOptions = renderer.clearOptions.apply {
            clear = true

            clearColor = floatArrayOf(0.10f, 0.10f, 0.12f, 1.0f)
        }


        camera.setExposure(16f, 1f / 125f, 100f)

        addLights()
    }

    private fun addLights() {
        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(1.0f, 0.96f, 0.90f)
            .intensity(80_000f)
            .direction(-0.4f, -1f, -0.6f)
            .castShadows(false)
            .build(engine, keyLightEntity)
        scene.addEntity(keyLightEntity)

        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(0.75f, 0.85f, 1.0f)
            .intensity(35_000f)
            .direction(0.6f, 0.4f, 0.6f)
            .castShadows(false)
            .build(engine, fillLightEntity)
        scene.addEntity(fillLightEntity)
    }

    fun loadModel(assetPath: String) {
        clearAsset()
        val newAsset = FilamentEngineHolder.loadGlb(context, assetPath)
        scene.addEntities(newAsset.entities)
        asset = newAsset

        val box = newAsset.boundingBox
        val center = box.center
        val half = box.halfExtent
        target[0] = center[0]; target[1] = center[1]; target[2] = center[2]
        val maxExtent = max(max(half[0], half[1]), half[2])

        val fitDistance = (maxExtent / tan(Math.toRadians(22.5).toFloat())) * 1.2f
        distance = fitDistance
        initialDistance = fitDistance
        yaw = 0f
        pitch = 0f
        dirty = true
        scheduleFrame()
    }

    private fun clearAsset() {
        asset?.let {
            scene.removeEntities(it.entities)
            FilamentEngineHolder.destroyAsset(it)
        }
        asset = null
    }

    fun rotateBy(deltaXPx: Float, deltaYPx: Float) {
        val sensitivity = 0.005f
        yaw -= deltaXPx * sensitivity
        pitch = (pitch + (deltaYPx * sensitivity)).coerceIn(-1.4f, 1.4f)
        dirty = true
        scheduleFrame()
    }

    fun zoomBy(zoomFactor: Float) {
        if (zoomFactor <= 0f) return
        val clamped = zoomFactor.coerceIn(0.5f, 2.0f)
        val minDist = initialDistance * 0.25f
        val maxDist = initialDistance * 8f
        distance = (distance / clamped).coerceIn(minDist, maxDist)
        dirty = true
        scheduleFrame()
    }


    private fun updateCamera() {
        val w = width
        val h = height
        if (w == 0 || h == 0) return
        camera.setProjection(
            45.0,
            w.toDouble() / h.toDouble(),
            0.05,
            1000.0,
            Camera.Fov.VERTICAL,
        )
        val cp = cos(pitch.toDouble())
        val sp = sin(pitch.toDouble())
        val cy = cos(yaw.toDouble())
        val sy = sin(yaw.toDouble())
        val eyeX = target[0] + distance * cp * sy
        val eyeY = target[1] + distance * sp
        val eyeZ = target[2] + distance * cp * cy
        camera.lookAt(
            eyeX, eyeY, eyeZ,
            target[0].toDouble(), target[1].toDouble(), target[2].toDouble(),
            0.0, 1.0, 0.0
        )
    }

    private fun scheduleFrame() {
        if (!frameCallbackPosted && attachedToWindow) {
            choreographer.postFrameCallback(this)
            frameCallbackPosted = true
        }
    }


    override fun surfaceCreated(holder: SurfaceHolder) {
        swapChain = engine.createSwapChain(holder.surface)
        dirty = true
        scheduleFrame()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        view.viewport = Viewport(0, 0, w, h)
        dirty = true
        scheduleFrame()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        swapChain?.let {
            engine.destroySwapChain(it)
            engine.flushAndWait()
        }
        swapChain = null
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        attachedToWindow = true
        scheduleFrame()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        attachedToWindow = false
        if (frameCallbackPosted) {
            choreographer.removeFrameCallback(this)
            frameCallbackPosted = false
        }

        clearAsset()
        val lightManager = engine.lightManager
        scene.removeEntity(keyLightEntity)
        scene.removeEntity(fillLightEntity)
        lightManager.destroy(keyLightEntity)
        lightManager.destroy(fillLightEntity)
        EntityManager.get().destroy(keyLightEntity)
        EntityManager.get().destroy(fillLightEntity)
        engine.destroyView(view)
        engine.destroyScene(scene)
        engine.destroyCameraComponent(cameraEntity)
        EntityManager.get().destroy(cameraEntity)
        engine.destroyRenderer(renderer)

    }


    override fun doFrame(frameTimeNanos: Long) {
        frameCallbackPosted = false
        if (!attachedToWindow) return
        if (!dirty) {

            return
        }
        val sc = swapChain ?: return
        if (width == 0 || height == 0) return

        updateCamera()

        if (renderer.beginFrame(sc, frameTimeNanos)) {
            renderer.render(view)
            renderer.endFrame()
        }
        dirty = false

    }

}
