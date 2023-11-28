package com.example.tattoo_ar.view

import android.content.Context
import com.example.tattoo_ar.view.recognition.AugmentedImageRecognizer
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class AugmentedViewFactory(private val messenger: BinaryMessenger): PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    companion object {
        const val METHOD_CHANNEL_NAME = "com.example.tattoo_ar/augmented_method_channel"
    }

    override fun create(context: Context?, viewId: Int, args: Any?): PlatformView {
        val creationParams = args as Map<*, *>?
        val referenceImageNames = creationParams?.get("referenceImageNames") as List<*>
        val imageRecognizer = AugmentedImageRecognizer(context, referenceImageNames.filterIsInstance<String>())
        val methodChannel = MethodChannel(messenger, METHOD_CHANNEL_NAME)
        return AugmentedView(context, viewId, imageRecognizer, methodChannel)
    }
}