package com.example.tattoo_ar

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import cn.easyar.Engine
import com.example.tattoo_ar.view.AugmentedViewFactory
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine

class MainActivity : FlutterActivity() {
    companion object {
        private const val EASY_AR_API_KEY_PROPERTY_NAME = "easyAR.API_KEY"
        private const val AUGMENTED_VIEW = "com.example.tattoo_ar/augmented_view"
        val TAG = MainActivity::class.simpleName
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        val registry = flutterEngine
            .platformViewsController
            .registry

        System.loadLibrary("EasyAR")

        val apiKey = getApiKey(this)

        if (!Engine.initialize(this, apiKey)) {
            Log.e(TAG, "Could not initialize EasyAR Engine")
            finish()
        }

        registry.registerViewFactory(AUGMENTED_VIEW, AugmentedViewFactory(flutterEngine.dartExecutor.binaryMessenger));
    }

    private fun getApiKey(activity: Activity): String {
        val packageManager = activity.packageManager
        val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val flags = PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
            packageManager.getApplicationInfo(activity.packageName, flags)
        } else {
            @Suppress("DEPRECATION")
            packageManager.getApplicationInfo(activity.packageName, PackageManager.GET_META_DATA)
        }
        val metaData = appInfo.metaData

        return metaData.getString(EASY_AR_API_KEY_PROPERTY_NAME)
            ?: throw Error("No API_KEY found for EasyAR Sense")
    }
}