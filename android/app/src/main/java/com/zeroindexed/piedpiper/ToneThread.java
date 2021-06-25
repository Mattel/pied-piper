package com.zeroindexed.piedpiper;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.util.Log;
import com.jraska.console.Console;

public class ToneThread extends Thread {
    public interface ToneCallback {
        public void onProgress(int current, int total);

        public void onDone();
    }

    public interface ToneIterator extends Iterable<Integer> {
        public int size();
    }

    static final int sample_rate = 44100; // 16000;//
    //
    float duration = 0.2f;//1.0f;//0.1f;
    int sample_size = Math.round(duration * sample_rate);

    final ToneIterator frequencies;
    final ToneCallback callback;
    boolean callback_done = false;
    boolean cancel_tone = false;
    int total_samples;

    public ToneThread(ToneIterator frequencies, float duration, ToneCallback callback) {
        this.frequencies = frequencies;
        this.callback = callback;
        this.duration = duration;
        this.sample_size = Math.round(this.duration * this.sample_rate);
        this.total_samples = Math.round(frequencies.size() * sample_size);
        Console.write(String.format("ToneThread: sample_size: %d, duration %f, frequencies.size %d", sample_size, this.duration, frequencies.size()));
        setPriority(Thread.MAX_PRIORITY);
    }

    public void stopAudio() {
        cancel_tone = true;
    }
    @Override
    public void run() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
        final AudioTrack track = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                sample_rate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                2 * sample_size,
                AudioTrack.MODE_STREAM
        );


            track.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
                @Override
                public void onMarkerReached(AudioTrack track) {
                    if (!callback_done) {
                        callback.onDone();
                        callback_done = true;
                    }
                }

                @Override
                public void onPeriodicNotification(AudioTrack track) {
                    if (!callback_done) {
                        callback.onProgress(track.getPlaybackHeadPosition(), total_samples);
                    }
                    if (cancel_tone) {
                        Log.d(TAG,"Tone Cancelled");
                        track.stop();
                        if (!callback_done) {
                            callback.onDone();
                            callback_done = true;
                        }
                    }
                }
            });
        track.setPositionNotificationPeriod(sample_rate / 10);

        track.play();

        for (int freq : frequencies) {
            Log.d(TAG, String.format("----- PLAY: %d", freq));
            short[] samples = generate(freq, this.sample_size);
            track.write(samples, 0, samples.length);
        }

        track.setNotificationMarkerPosition(sample_size);
        }
    }

    static String TAG = "ToneThread";
    static short[] generate(float frequency, int sample_size) {
        final short sample[] = new short[sample_size];
        final double increment = 2 * Math.PI * frequency / sample_rate;

        Log.d(TAG, String.format("freq: %f, sample_size: %d, sample.length: %d, increment %f", frequency, sample_size, sample.length, increment));
        double angle = 0;
        for (int i = 0; i < sample.length; ++i) {
            sample[i] = (short) (Math.sin(angle) * Short.MAX_VALUE);
            angle += increment;
        }
        return sample;
    }
}
