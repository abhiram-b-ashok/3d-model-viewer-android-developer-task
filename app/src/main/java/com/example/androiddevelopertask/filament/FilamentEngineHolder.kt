package com.example.androiddevelopertask.filament

import android.content.Context
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.gltfio.ResourceLoader
import com.google.android.filament.gltfio.UbershaderProvider
import com.google.android.filament.utils.Utils
import java.nio.ByteBuffer

object FilamentEngineHolder {

    init {
        Utils.init()
    }

    @Volatile private var initialized = false
    private var _engine: Engine? = null
    private var _materialProvider: UbershaderProvider? = null
    private var _assetLoader: AssetLoader? = null
    private var _resourceLoader: ResourceLoader? = null

    private val sourceBytes = HashMap<String, ByteBuffer>()

    val engine: Engine
        get() = checkNotNull(_engine) { "FilamentEngineHolder not initialized" }

    @Synchronized
    fun ensureInitialized(): Engine {
        if (!initialized) {
            val engine = Engine.create()
            val materialProvider = UbershaderProvider(engine)
            _engine = engine
            _materialProvider = materialProvider
            _assetLoader = AssetLoader(engine, materialProvider, EntityManager.get())
            _resourceLoader = ResourceLoader(engine)
            initialized = true
        }
        return _engine!!
    }

    @Synchronized
    fun loadGlb(context: Context, assetPath: String): FilamentAsset {
        ensureInitialized()
        val loader = checkNotNull(_assetLoader)
        val resources = checkNotNull(_resourceLoader)

        val cached = sourceBytes[assetPath] ?: run {
            val raw = context.assets.open(assetPath).use { it.readBytes() }
            val buf = ByteBuffer.wrap(raw)
            sourceBytes[assetPath] = buf
            buf
        }

        val view = cached.duplicate()
        view.rewind()

        val asset = loader.createAsset(view)
            ?: error("Failed to parse glTF asset at $assetPath")
        resources.loadResources(asset)
        asset.releaseSourceData()
        return asset
    }

    @Synchronized
    fun destroyAsset(asset: FilamentAsset) {
        _assetLoader?.destroyAsset(asset)
    }
}
