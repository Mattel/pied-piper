package com.zeroindexed.piedpiper

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.casualcoding.reedsolomon.DataTooLargeException
import com.casualcoding.reedsolomon.EncoderDecoder
import com.jraska.console.Console
import java.io.ByteArrayInputStream
import java.nio.charset.Charset

class MainActivity : AppCompatActivity(), ToneThread.ToneCallback {

    var text: EditText? = null
    var play_tone: View? = null
    var play_tone_bin_file: View? = null
    var stop_button: View? = null
    var progress: ProgressBar? = null
    var errorCorrectionBytesNumberControl: EditText? = null
    var toneDurationNumberControl: EditText? = null
    var playingTone:ToneThread? = null

    val REQUEST_IMAGE_CAPTURE = 1
    var FEC_BYTES = 15

    val gvk25 = ubyteArrayOf(0x47U, 0x56U, 0x4BU, 0x32U, 0x35U, 0x22U, 0xFFU, 0x00U, 0x00U, 0x01U,
            0x00U, 0x00U, 0x00U, 0x00U, 0x00U, 0x03U, 0x00U, 0x02U, 0x00U, 0x02U,
            0x00U, 0x01U, 0x00U, 0x01U, 0x00U, 0x02U, 0x00U, 0x01U, 0x00U, 0x00U,
            0x00U, 0x00U, 0x00U, 0x04U, 0x00U, 0x00U, 0x00U, 0x00U, 0x00U, 0x00U,
            0x00U, 0x02U, 0x00U, 0x12U, 0x00U, 0x04U, 0x00U, 0x00U, 0x00U, 0x2AU,
            0x00U, 0x01U, 0x00U, 0x00U, 0x00U, 0x09U, 0x00U, 0x01U, 0x00U, 0x00U,
            0x00U, 0x0CU, 0x00U, 0x01U, 0x00U, 0x02U, 0x00U, 0x00U, 0x00U, 0x00U,
            0x00U, 0x01U, 0x00U, 0x01U, 0x00U, 0x01U, 0x00U, 0x00U, 0x2AU, 0x09U,
            0x0CU, 0x00U, 0x03U, 0x1FU, 0x00U, 0x00U, 0x00U, 0x00U, 0x00U, 0x00U,
            0x00U, 0x00U, 0x00U, 0x00U, 0x00U, 0x00U).toByteArray()
    val gvk25s = ubyteArrayOf(0x47U, 0x56U, 0x4BU, 0x32U, 0x35U, 0x22U, 0xFFU, 0x00U, 0x00U, 0x01U,
        0x00U, 0x00U, 0x00U, 0x00U, 0x00U, 0x03U, 0x00U, 0x02U, 0x00U, 0x02U,
        0x00U, 0x01U, 0x00U, 0x01U, 0x00U, 0x02U, 0x00U, 0x01U, 0x00U, 0x00U,
        0x00U, 0x00U, 0x00U, 0x04U, 0x00U, 0x00U, 0x00U, 0x00U, 0x00U, 0x00U,
        0x00U, 0x02U, 0x00U, 0x12U, 0x00U, 0x04U, 0x00U, 0x00U, 0x00U, 0x2AU,
        0x00U, 0x01U, 0x00U, 0x00U, 0x00U, 0x09U, 0x00U, 0x01U, 0x00U, 0x00U,
        0x00U, 0x0CU, 0x00U, 0x01U, 0x00U, 0x02U, 0x00U, 0x00U, 0x00U, 0x00U,
        0x00U, 0x01U, 0x00U, 0x01U, 0x00U, 0x01U, 0x00U, 0x00U, 0x2AU, 0x09U,
        0x0CU, 0x00U, 0x03U, 0x1FU).toByteArray()

    /*

    6/22 : 2:30
    GVK25"\x00\x00\x00\x01
    \x00\x00\x00\x00\x00\x03\x00\x02\x00\x02
    \x00\x01\x00\x01\x00\x02\x00\x01\x00\x00
    \x00\x00\x00\x04\x00\x00\x00\x00\x00\x00
    \x00\x02\x00\x12\x00\x04\x00\x00\x00  *
    \x00\x01\x00\x00\x00\t\x00\x01\x00\x00\x00\x0c\x00\x01\x00\x02\x00\x00\x00\x00\x00\x01\x00\x01\x00\x01\x00\x00*\t\x0c\x00\x03\x1f\xb6\xde\xf4\x15\xba\x9f\xf5\xa2\x04\x8f'

    Pre 6/22
    DECODED:
        47 56 4b 32 35 22 ff 00 00 01
        00 00 00 00 00 03 00 02 00 02
        00 01 00 01 00 02 00 01 00 00
        00 00 00 04 00 00 00 00 00 00
        00 02 00 12 00 04 00 00 00 2a
        00 01 00 00 00 09 00 01 00 00
        00 0c 00 01 00 02 00 00 00 00
        00 01 00 01 00 01 00 00 2a 09
        0c 00 03 1f 00 00 00 00 00 00
        00 00 00 00 00 00 4d eb f6 20

        47 56 4b 32 35 22 ff 00 00 01
        00 00 00 00 00 03 00 02 00 02
        00 01 00 01 00 02 00 01 00 00
        00 00 00 04 00 00 00 00 00 00
        00 02 00 12 00 04 00 00 00 2a
        00 01 00 00 00 09 00 01 00 00
        00 0c 00 01 00 02 00 00 00 00
        00 01 00 01 00 01 00 00 2a 09
        0c 00 03 1f     32 97 a4 71

        47 56 4b 32 35 22 ff 00 00 01
        00 00 00 00 00 03 00 02 00 02
        00 01 00 01 00 02 00 01 00 00
        00 00 00 04 00 00 00 00 00 00
        00 02 00 12 00 04 00 00 00 2a
        00 01 00 00 00 00 00 10 00 00
        00 c0 00 10 00 20 00 00 00 00
        00 10 00 10 00 10 00 02 a0 0c
        00 03 1f 32 7a 47
     */


    /*
            G      V      K     2       5      "      x00    \x00   \x00   \x01
            \x00   \x00   \x00  \x00   \x00    \x03   \x00   \x02   \x00   \x02
            \x00   \x01   \x00  \x01   \x00    \x02   \x00   \x01   \x00   \x00
            \x00   \x00   \x00  \x04   \x00    \x00   \x00   \x00   \x00   \x00
            \x00   \x02   \x00  \x12   \x00    \x04   \x00   \x00   \x00    *
            \x00   \x01   \x00  \x00   \x00    \t     \x00   \x01   \x00   \x00
            \x00   \x0c   \x00  \x01   \x00    \x02   \x00   \x00   \x00   \x00
            \x00   \x01   \x00  \x01   \x00    \x01   \x00   \x00    *     \t
            \x0c   \x00   \x03  \x1f   \x00    \x00   \x00   \x00   \x00   \x00
            \x00   \x00   \x00  \x00   \x00    \x00   }      \x96   \xdf   \x07\\G\xdf\x05\x19\x9b')
bs{} type: {} - {} 1 <class 'bytearray'> bytearray(b'\x06')

     */
    /*
    82 + 2
0x47U, 0x56U, 0x4BU, 0x32U, 0x35U, 0x22U, 0xFFU, 0x00U, 0x00U, 0x01U,
0x00U, 0x00U, 0x00U, 0x00U, 0x00U, 0x03U, 0x00U, 0x02U, 0x00U, 0x02U,

0x00U, 0x01U, 0x00U, 0x01U, 0x00U, 0x02U, 0x00U, 0x01U, 0x00U, 0x00U,
0x00U, 0x00U, 0x00U, 0x04U, 0x00U, 0x00U, 0x00U, 0x00U, 0x00U, 0x00U,

0x00U, 0x02U, 0x00U, 0x12U, 0x00U, 0x04U, 0x00U, 0x00U, 0x00U, 0x2AU,
0x00U, 0x01U, 0x00U, 0x00U, 0x00U, 0x09U, 0x00U, 0x01U, 0x00U, 0x00U,

0x00U, 0x0CU, 0x00U, 0x01U, 0x00U, 0x02U, 0x00U, 0x00U, 0x00U, 0x00U,
0x00U, 0x01U, 0x00U, 0x01U, 0x00U, 0x01U, 0x00U, 0x00U, 0x2AU, 0x09U,

0x0CU, 0x00U, 0x03U, 0x1FU, 0x00U, 0x00U, 0x00U, 0x00U, 0x00U, 0x00U,
0x00U, 0x00U, 0x00U, 0x00U, 0x00U, 0x00U


bytearray(
b'GVK25"\xff\x00\x00\x01
\x00\x00\x00\x00\x00\x03\x00\x02\x00\x02

\x00\x01\x00\x01\x00\x02\x00\x01\x00\x00
\x00\x00\x00\x04\x00\x00\x00\x00\x00\x00

\x00\x02\x00\x12\x00\x04\x00\x00\x00*(2A)
\x00\x01\x00\x00\x00\t(09)\x00\x01\x00\x00

\x00\x0c\x00\x01\x00\x02\x00\x00\x00\x00
\x00\x01\x00\x01\x00\x01\x00\x00*(2A)\t(09)

\x0c\x00\x03\x1f\x00\x00\x00\x00\x00\x00
\x00\x00\x00\x00\x00\x00M\xeb\xf6

*/
/*
// from wav file
b'GVK25"\x00\x00\x00\x01
\x00\x00\x00\x00\x00\x03\x00\x02\x00\x02

\x00\x01\x00\x01\x00\x02\x00\x01\x00\x00
\x00\x00\x00\x04\x00\x00\x00\x00\x00\x00

\x00\x02\x00\x12\x00\x04\x00\x00\x00*(2a)
\x00\x01\x00\x00\x00\t(09)\x00\x01\x00\x00

\x00\x0c\x00\x01\x00\x02\x00\x00\x00\x00
\x00\x01\x00\x01\x00\x01\x00\x00*(2a)\t(09)

\x0c\x00\x03\x1f\x00\x00\x00\x00\x00\x00
\x00\x00\x00\x00\x00\x00M\xeb\xf6'
*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        text = findViewById(R.id.text) as EditText
        play_tone = findViewById(R.id.play_tone)
        play_tone_bin_file = findViewById(R.id.play_tone_bin_file)
        errorCorrectionBytesNumberControl = findViewById(R.id.editNumber_ErrorCorrectionBytes)
        toneDurationNumberControl = findViewById(R.id.editNumber_ToneDurationNumber)

        stop_button = findViewById(R.id.stop_playing)
        stop_button?.setOnClickListener(View.OnClickListener {
            Console.writeLine(String.format("Stop Playing"))
            hideKeyboard(this)
            playingTone?.stopAudio()
        })

        progress = findViewById<ProgressBar>(R.id.progress)
        play_tone?.setOnClickListener(View.OnClickListener {
            val message = text!!.text.toString()
            encodeBytes(message.toByteArray(Charset.forName("UTF-8")))
            play_tone?.isEnabled = false
        })

        play_tone_bin_file?.setOnClickListener(View.OnClickListener {
            encodeBytes(gvk25)
            play_tone_bin_file?.isEnabled = false
        })

        // Setup listener
    }
    fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

    private fun encodeBytes(payload: ByteArray) {
        val tempFecCount = errorCorrectionBytesNumberControl?.text.toString()
        FEC_BYTES = Integer.parseInt(tempFecCount);

        val tempToneDuration = toneDurationNumberControl?.text.toString()
        var toneDuration:Float = tempToneDuration.toFloat()

        hideKeyboard(this)
        //
        Console.writeLine(String.format("Error Correction Bytes: %d, %s", FEC_BYTES, payload?.toHexString()))

        Log.d("PIPER", payload?.toHexString())
        val encoder = EncoderDecoder()
        val fecPayload: ByteArray = try {
            encoder.encodeData(payload, FEC_BYTES)!!
        } catch (e: DataTooLargeException) {
            return
        }
        Log.d("PIPER", String.format("    payload: %d: %s", payload.size, payload?.toHexString()))
        Log.d("PIPER", String.format("fec payload: %d: %s", fecPayload.size, fecPayload?.toHexString()))
        val bis = ByteArrayInputStream(fecPayload)
        Console.writeLine(String.format("fec-payload: correction bytes: %d, tone len: %f, %d/%d", FEC_BYTES, toneDuration, payload.size, fecPayload.size))
        Console.writeLine(String.format("%s", payload?.toHexString()))
        Console.writeLine(String.format("%s", fecPayload?.toHexString()))
        val tone: ToneThread.ToneIterator = BitstreamToneGenerator(bis, fecPayload.size)
        playingTone = ToneThread(tone, toneDuration,this@MainActivity)
        playingTone!!.start()
    }


    private fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun decode()
    {
        val encoder = EncoderDecoder()
        var payload = ByteArray(0)
        encoder.decodeData(payload, FEC_BYTES)


    }


    override fun onProgress(current: Int, total: Int) {
        progress?.max = total
        progress?.progress = current
    }

    override fun onDone() {
        play_tone?.isEnabled = true
        play_tone_bin_file?.isEnabled = true
        progress?.progress = 0
    }
}