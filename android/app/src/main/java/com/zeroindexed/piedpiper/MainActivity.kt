package com.zeroindexed.piedpiper

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.casualcoding.reedsolomon.EncoderDecoder
import com.casualcoding.reedsolomon.EncoderDecoder.DataTooLargeException
import com.zeroindexed.piedpiper.MainActivity
import com.zeroindexed.piedpiper.ToneThread.ToneCallback
import com.zeroindexed.piedpiper.ToneThread.ToneIterator
import java.io.ByteArrayInputStream
import java.nio.charset.Charset

class MainActivity : AppCompatActivity(), ToneCallback {
    var text: EditText? = null
    var play_tone: View? = null
    var progress: ProgressBar? = null

    fun parse(data: ByteArray?): String {
        if (data == null || data.size == 0) return ""
        val sb = StringBuilder()
        for (b in data) {
            sb.append(String.format("%02X", b))
        }
        return sb.toString()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        text = findViewById<View>(R.id.text) as EditText
        play_tone = findViewById(R.id.play_tone)
        progress = findViewById<View>(R.id.progress) as ProgressBar
        play_tone?.setOnClickListener(View.OnClickListener {
            val message = text!!.text.toString()
            var payload = ByteArray(0)
            payload = message.toByteArray()  //message.toByteArray(Charset.forName("UTF-8"))
            Log.i("TAG", parse(payload))
            val encoder = EncoderDecoder()
            val fec_payload: ByteArray
            fec_payload = try {
                encoder.encodeData(payload, FEC_BYTES)
            } catch (e: DataTooLargeException) {
                return@OnClickListener
            }
            Log.i("TAG-FEC", parse(fec_payload))
            val bis = ByteArrayInputStream(fec_payload)
            play_tone?.setEnabled(false)
            val tone: ToneIterator = BitstreamToneGenerator(bis, 7)
            ToneThread(tone, this@MainActivity).start()
        })
    }

    override fun onProgress(current: Int, total: Int) {
        progress!!.max = total
        progress!!.progress = current
    }

    override fun onDone() {
        play_tone!!.isEnabled = true
        progress!!.progress = 0
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val FEC_BYTES = 4
    }
}