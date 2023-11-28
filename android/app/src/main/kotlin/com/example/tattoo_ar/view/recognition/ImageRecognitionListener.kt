package com.example.tattoo_ar.view.recognition

interface ImageRecognitionListener {
    fun onRecognitionStarted()
    fun onError(errorCode: ErrorCode)
    fun onDetected(detectedImage: String)
}