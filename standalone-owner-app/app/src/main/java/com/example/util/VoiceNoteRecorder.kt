package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceNoteRecorder(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDurationSeconds = MutableStateFlow(0)
    val recordingDurationSeconds: StateFlow<Int> = _recordingDurationSeconds.asStateFlow()

    private val _amplitude = MutableStateFlow(0.15f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    private val _transcribedText = MutableStateFlow("")
    val transcribedText: StateFlow<String> = _transcribedText.asStateFlow()

    private var onFinalResultCallback: ((String) -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null

    fun startRecording(
        onResult: (String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        if (_isRecording.value) return

        onFinalResultCallback = onResult
        onErrorCallback = onError
        _transcribedText.value = ""
        _recordingDurationSeconds.value = 0
        _amplitude.value = 0.2f
        _isRecording.value = true

        // Start timer
        recordingJob?.cancel()
        recordingJob = scope.launch {
            while (_isRecording.value) {
                delay(1000)
                _recordingDurationSeconds.value += 1
            }
        }

        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer?.destroy()
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _amplitude.value = 0.3f
                        }

                        override fun onBeginningOfSpeech() {
                            _amplitude.value = 0.5f
                        }

                        override fun onRmsChanged(rmsdB: Float) {
                            // Map rmsdB (-2 to 10 dB typical) to 0.1 .. 1.0 amplitude
                            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.15f, 1.0f)
                            _amplitude.value = normalized
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            _amplitude.value = 0.1f
                        }

                        override fun onError(error: Int) {
                            val errorMsg = when (error) {
                                SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                                SpeechRecognizer.ERROR_CLIENT -> "Speech recognition client error"
                                else -> "Recognition error: $error"
                            }
                            if (_transcribedText.value.isNotBlank()) {
                                onFinalResultCallback?.invoke(_transcribedText.value)
                            }
                            stopRecording()
                        }

                        override fun onResults(results: Bundle?) {
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val recognized = matches?.firstOrNull() ?: _transcribedText.value
                            if (recognized.isNotBlank()) {
                                _transcribedText.value = recognized
                                onFinalResultCallback?.invoke(recognized)
                            }
                            stopRecording()
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            matches?.firstOrNull()?.let {
                                if (it.isNotBlank()) {
                                    _transcribedText.value = it
                                }
                            }
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }

                speechRecognizer?.startListening(intent)
            } else {
                // Speech recognizer not natively installed; use simulated waveform while waiting for user action
                _isRecording.value = true
            }
        } catch (e: Exception) {
            onErrorCallback?.invoke(e.localizedMessage ?: "Voice recognition failed")
            stopRecording()
        }
    }

    fun stopAndSend(manualFallbackText: String? = null) {
        val finalText = manualFallbackText ?: _transcribedText.value
        if (finalText.isNotBlank()) {
            onFinalResultCallback?.invoke(finalText)
        }
        stopRecording()
    }

    fun cancelRecording() {
        _transcribedText.value = ""
        stopRecording()
    }

    private fun stopRecording() {
        _isRecording.value = false
        recordingJob?.cancel()
        recordingJob = null
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            // Ignore teardown errors
        }
    }

    fun destroy() {
        stopRecording()
        scope.cancel()
    }

    companion object {
        fun formatDuration(seconds: Int): String {
            val mins = seconds / 60
            val secs = seconds % 60
            return "%d:%02d".format(mins, secs)
        }
    }
}
