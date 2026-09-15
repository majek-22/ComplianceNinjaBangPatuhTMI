package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

/**
 * SoundManager provides low-latency arcade sound playback using direct in-memory AudioTrack synthesis.
 *
 * Implements the requested `playSfx(name: String)` API with pure in-memory 16-bit PCM waveforms,
 * completely avoiding disk I/O, file decoders, SoundPool and MediaCodec C2 HAL component queries.
 */
class SoundManager(private val context: Context) {

    companion object {
        private const val TAG = "SoundManager"
        private const val SAMPLE_RATE = 44100
    }

    private val soundClips = mutableMapOf<String, SoundClip>()
    var isMuted: Boolean = false

    init {
        cleanupLegacyFiles()
        initializeProceduralAudio()
    }

    private fun cleanupLegacyFiles() {
        try {
            context.cacheDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("sfx_") && file.name.endsWith(".wav")) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    /**
     * Requested API: Play a short sound effect by key name.
     * Supported names:
     * - "slice-hit"
     * - "wrong-slice"
     * - "shield-bonus"
     * - "game-over"
     * - "trap-hit"
     */
    fun playSfx(name: String) {
        if (isMuted) return
        val clip = soundClips[name]
        clip?.play(isMuted)
    }

    private fun initializeProceduralAudio() {
        try {
            // 1. "slice-hit": crisp high-frequency blade slash swoosh (110ms sweep from 1400Hz to 600Hz)
            val slicePcm = generatePcm(durationSec = 0.11f) { t, dur ->
                val freq = 1400.0 - (t / dur) * 900.0
                val env = 1.0 - (t / dur)
                (sin(2.0 * PI * freq * t) * env * 0.9).toFloat()
            }
            soundClips["slice-hit"] = SoundClip(slicePcm, SAMPLE_RATE, voiceCount = 4)

            // 2. "wrong-slice": low dissonance error buzz (180ms pulse at 130Hz)
            val wrongPcm = generatePcm(durationSec = 0.18f) { t, dur ->
                val env = (1.0 - (t / dur)).coerceAtLeast(0.0)
                val square = if (sin(2.0 * PI * 130.0 * t) > 0) 0.6 else -0.6
                val buzz = if (sin(2.0 * PI * 195.0 * t) > 0) 0.3 else -0.3
                ((square + buzz) * env).toFloat()
            }
            soundClips["wrong-slice"] = SoundClip(wrongPcm, SAMPLE_RATE, voiceCount = 2)

            // 3. "shield-bonus": ascending crystal chime arpeggio (320ms, 523Hz -> 659Hz -> 784Hz -> 1046Hz)
            val shieldPcm = generatePcm(durationSec = 0.32f) { t, dur ->
                val step = (t / 0.08f).toInt().coerceIn(0, 3)
                val noteFreq = when (step) {
                    0 -> 523.25 // C5
                    1 -> 659.25 // E5
                    2 -> 783.99 // G5
                    else -> 1046.50 // C6
                }
                val localT = t - step * 0.08f
                val env = (1.0 - (localT / 0.08f)).coerceIn(0.0, 1.0)
                (sin(2.0 * PI * noteFreq * t) * env * 0.85).toFloat()
            }
            soundClips["shield-bonus"] = SoundClip(shieldPcm, SAMPLE_RATE, voiceCount = 2)

            // 4. "game-over": retro dramatic descending chime (420ms)
            val gameOverPcm = generatePcm(durationSec = 0.42f) { t, dur ->
                val freq = 440.0 - (t / dur) * 220.0
                val env = (1.0 - (t / dur)).coerceAtLeast(0.0)
                (sin(2.0 * PI * freq * t) * env * 0.8).toFloat()
            }
            soundClips["game-over"] = SoundClip(gameOverPcm, SAMPLE_RATE, voiceCount = 1)

            // 5. "trap-hit": dull spring wobble / hollow warning boing (250ms)
            val trapPcm = generatePcm(durationSec = 0.25f) { t, dur ->
                val freq = 280.0 + sin(2.0 * PI * 24.0 * t) * 80.0
                val env = (1.0 - (t / dur)).coerceAtLeast(0.0)
                (sin(2.0 * PI * freq * t) * env * 0.75).toFloat()
            }
            soundClips["trap-hit"] = SoundClip(trapPcm, SAMPLE_RATE, voiceCount = 2)

            // 6. "freeze-start": icy crystalline chime sweep (350ms, 900Hz -> 1800Hz shimmering)
            val freezePcm = generatePcm(durationSec = 0.35f) { t, dur ->
                val sweepFreq = 900.0 + (t / dur) * 1100.0
                val shimmer = sin(2.0 * PI * 18.0 * t) * 0.25
                val env = (1.0 - (t / dur)).coerceIn(0.0, 1.0)
                ((sin(2.0 * PI * sweepFreq * t) + shimmer) * env * 0.85).toFloat()
            }
            soundClips["freeze-start"] = SoundClip(freezePcm, SAMPLE_RATE, voiceCount = 2)

            // 7. "coin-bonus": bright high-register coin ping (120ms, dual harmony 1975Hz & 2637Hz)
            val coinPcm = generatePcm(durationSec = 0.12f) { t, dur ->
                val env = (1.0 - (t / dur)).coerceIn(0.0, 1.0)
                val h1 = sin(2.0 * PI * 1975.5 * t) * 0.6
                val h2 = sin(2.0 * PI * 2637.0 * t) * 0.4
                ((h1 + h2) * env).toFloat()
            }
            soundClips["coin-bonus"] = SoundClip(coinPcm, SAMPLE_RATE, voiceCount = 4)

        } catch (e: Exception) {
            Log.w(TAG, "Procedural sound generation warning: ${e.message}", e)
        }
    }

    private fun generatePcm(
        durationSec: Float,
        sampleRate: Int = SAMPLE_RATE,
        sampleGenerator: (time: Double, duration: Double) -> Float
    ): ByteArray {
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ByteBuffer.allocate(numSamples * 2).order(ByteOrder.LITTLE_ENDIAN)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val sample = sampleGenerator(t, durationSec.toDouble()).coerceIn(-1.0f, 1.0f)
            val shortVal = (sample * 32767).toInt().toShort()
            buffer.putShort(shortVal)
        }
        return buffer.array()
    }

    fun release() {
        soundClips.values.forEach { it.release() }
        soundClips.clear()
    }

    private class SoundClip(
        pcmBytes: ByteArray,
        sampleRate: Int = SAMPLE_RATE,
        voiceCount: Int = 2
    ) {
        private val tracks: Array<AudioTrack?> = Array(voiceCount) {
            try {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(pcmBytes.size)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build().apply {
                        write(pcmBytes, 0, pcmBytes.size)
                    }
            } catch (e: Exception) {
                Log.w("SoundClip", "Failed to build AudioTrack voice: ${e.message}")
                null
            }
        }

        private var currentVoice = 0

        @Synchronized
        fun play(isMuted: Boolean) {
            if (isMuted || tracks.isEmpty()) return
            try {
                val track = tracks[currentVoice]
                currentVoice = (currentVoice + 1) % tracks.size
                if (track != null) {
                    if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                        track.stop()
                    }
                    track.reloadStaticData()
                    track.play()
                }
            } catch (e: Exception) {
                Log.w("SoundClip", "Error playing clip: ${e.message}")
            }
        }

        fun release() {
            for (track in tracks) {
                try {
                    if (track?.playState == AudioTrack.PLAYSTATE_PLAYING) {
                        track.stop()
                    }
                    track?.release()
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
    }
}
