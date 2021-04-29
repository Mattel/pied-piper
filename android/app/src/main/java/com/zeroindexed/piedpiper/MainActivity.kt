package com.zeroindexed.piedpiper

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.zeroindexed.piedpiper.BitstreamToneGenerator
import com.casualcoding.reedsolomon.EncoderDecoder
import com.zeroindexed.piedpiper.ToneThread
import java.io.ByteArrayInputStream
import java.nio.charset.Charset

class MainActivity : AppCompatActivity(), ToneThread.ToneCallback {

    var text: EditText? = null
    var play_tone: View? = null
    var progress: ProgressBar? = null

    val REQUEST_IMAGE_CAPTURE = 1
    val FEC_BYTES = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        text = findViewById(R.id.text) as EditText
        play_tone = findViewById(R.id.play_tone)
        progress = findViewById(R.id.progress) as ProgressBar
        play_tone?.setOnClickListener(View.OnClickListener {
            val message = text!!.text.toString()
            var payload = ByteArray(0)
            payload = message.toByteArray(Charset.forName("UTF-8"))
            val encoder = EncoderDecoder()
            val fec_payload: ByteArray
            fec_payload = try {
                encoder.encodeData(payload, FEC_BYTES)
            } catch (e: EncoderDecoder.DataTooLargeException) {
                return@OnClickListener
            }
            val bis = ByteArrayInputStream(fec_payload)
            play_tone?.setEnabled(false)
            val tone: ToneThread.ToneIterator = BitstreamToneGenerator(bis, 7)
            ToneThread(tone, this@MainActivity).start()
        })

    }

    override fun onProgress(current: Int, total: Int) {
        progress?.setMax(total)
        progress?.setProgress(current)
    }

    override fun onDone() {
        play_tone?.setEnabled(true)
        progress?.setProgress(0)
    }
}