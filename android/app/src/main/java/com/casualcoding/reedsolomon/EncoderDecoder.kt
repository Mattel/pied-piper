package com.casualcoding.reedsolomon

import android.util.Log
import com.google.zxing.common.reedsolomon.GenericGF
import com.google.zxing.common.reedsolomon.ReedSolomonDecoder
import com.google.zxing.common.reedsolomon.ReedSolomonEncoder
import com.google.zxing.common.reedsolomon.ReedSolomonException
import java.io.ByteArrayOutputStream
import kotlin.experimental.and
import kotlin.jvm.Throws

/**
 *
 * Purpose: to provide a simple class for encoding and decoding/repairing
 * data using Reed-Solomon forward error correction codes.
 * Data can only be encoded/decoded in 256 byte chunks, which is a limitation
 * of the Reed-Solomon classes that this class wraps
 *
 * @author Blake Hamilton <blake.a.hamilton></blake.a.hamilton>@gmail.com>
 * http://www.casual-coding.blogspot.com/
 * @version 1.0
 */


class EncoderDecoder {

    companion object {
        private val TAG = EncoderDecoder::class.java.simpleName
    }
    private val decoder: ReedSolomonDecoder
    private val encoder: ReedSolomonEncoder


    fun List<Byte>.toHexString() = joinToString("") { "%02x".format(it) }
    fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

    /**
     * Encodes the supplied byte array, generating and appending the supplied number of error correction bytes to be used.
     *
     *
     * @param data the bytes to be encoded
     * @param numErrorCorrectionBytes The number of error correction bytes to be generated from the user supplied data
     * @return The encoded bytes, where the size of the encoded data is the original size + the number of bytes used for error correction
     * @throws DataTooLargeException if total size of data supplied by user to be encoded, plus the number of error
     * correction bytes is greater than 256 bytes
     */
    @Throws(DataTooLargeException::class)
    fun encodeData(data: ByteArray?, numErrorCorrectionBytes: Int): ByteArray? {
        Log.d(TAG, "[encodeData] correct bytes: " + numErrorCorrectionBytes + ", Data: " + data?.toHexString())
        if (data == null || data.size == 0) {
            return null
        }
        if (data.size + numErrorCorrectionBytes > 256) {
            throw DataTooLargeException("Data Length + Number or error correction bytes cannot exceed 256 bytes")
        }
        Log.d(TAG, String.format("Encode Data Size: %d", data.size))
        val totalBytes = numErrorCorrectionBytes + data.size
        val dataInts = IntArray(totalBytes)
        for (i in data.indices) {
            dataInts[i] = (data[i] and 0xFF.toByte()).toInt()
        }
        Log.d(TAG, String.format("DATA INTS: %d", dataInts.size))
        encoder.encode(dataInts, numErrorCorrectionBytes)
        val bos = ByteArrayOutputStream()
        for (i in dataInts) {
            bos.write(i)
        }

        Log.d(TAG, "[encodeData] encoded: " + bos.toByteArray().toHexString())

        return bos.toByteArray()
    }

    /**
     *
     * Repairs and decodes the supplied byte array, removing the error correction codes and returning the original data
     *
     * @param data The bytes to be repaired/decoded
     * @param numErrorCorrectionBytes The number of error correction bytes present in the encoded data.
     * If this field is incorrect the encoded data may not be able to be repaired/encoded
     * @return The decoded/repaired data. The returned byte array will be N bytes shorter than the supplied
     * encoded data, where N equals the number of error correction bytes within the encoded byte array
     * @throws ReedSolomonException if the data is not able to be repaired/decoded
     * @throws DataTooLargeException if the supplied byte array is greater than 256 bytes
     */
    @Throws(ReedSolomonException::class, DataTooLargeException::class)
    fun decodeData(data: ByteArray?, numErrorCorrectionBytes: Int): ByteArray? {
        if (data == null || data.size == 0) {
            return null
        }
        if (data.size > 256) {
            throw DataTooLargeException("Data exceeds 256 bytes! Too large")
        }
        val dataInts = IntArray(data.size)
        for (i in data.indices) {
            dataInts[i] = (data[i] and 0xFF.toByte()).toInt()
        }
        val totalBytes = data.size - numErrorCorrectionBytes
        decoder.decode(dataInts, numErrorCorrectionBytes)
        val bos = ByteArrayOutputStream()
        var i = 0
        while (i < totalBytes && i < dataInts.size) {
            bos.write(dataInts[i]) // read in all the data sans error correction codes
            i++
        }
        return bos.toByteArray()
    }

    init {
        decoder = ReedSolomonDecoder(GenericGF.QR_CODE_FIELD_256)
        encoder = ReedSolomonEncoder(GenericGF.QR_CODE_FIELD_256)
    }
}