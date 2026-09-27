package com.smartdisplay.app.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/**
 * Android標準のSpeechRecognizerを使った音声操作マネージャー。
 * RECORD_AUDIO権限が許可された後にstart()を呼び出す。
 * 外部APIキーは不要（Android OS標準機能のみで動作）。
 *
 * 注意: これは「1回聞き取り→結果が返ったら再開」を繰り返す簡易的な常時リッスン実装です。
 * ネットワーク接続とマイクの継続稼働が必要になるため、バッテリー消費は大きくなります。
 * 本格的な「ウェイクワード」運用にしたい場合は、Picovoice Porcupineなど
 * オンデバイスのウェイクワードエンジンの導入を検討してください。
 */
class VoiceCommandManager(
    private val context: Context,
    private val onCommand: (String) -> Unit,
    private val onRawResult: (String) -> Unit = {}
) {
    private var recognizer: SpeechRecognizer? = null
    private var listening = false

    private val commandMap = linkedMapOf(
        "ホーム" to "home",
        "音楽" to "music",
        "ミュージック" to "music",
        "テレビ" to "tv",
        "カレンダー" to "calendar",
        "写真" to "photos",
        "フォト" to "photos",
        "ツール" to "tools",
        "再生" to "play",
        "止めて" to "pause",
        "停止" to "pause",
        "音量上げて" to "volume_up",
        "音量下げて" to "volume_down",
        "おやすみ" to "screensaver"
    )

    fun start() {
        if (listening) return
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return
        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) { restart() }
                override fun onResults(results: Bundle?) {
                    val text = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        .orEmpty()
                    onRawResult(text)
                    matchCommand(text)?.let(onCommand)
                    restart()
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
        listening = true
        listenOnce()
    }

    fun stop() {
        listening = false
        recognizer?.stopListening()
        recognizer?.destroy()
        recognizer = null
    }

    private fun restart() {
        if (listening) listenOnce()
    }

    private fun listenOnce() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ja-JP")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        recognizer?.startListening(intent)
    }

    private fun matchCommand(text: String): String? {
        val normalized = text.trim()
        return commandMap.entries.firstOrNull { normalized.contains(it.key) }?.value
    }
}
