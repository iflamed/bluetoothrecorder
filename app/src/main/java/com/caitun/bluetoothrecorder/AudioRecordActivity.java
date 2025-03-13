package com.caitun.bluetoothrecorder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

/**
 * Sample that demonstrates how to record a device's microphone using {@link AudioRecord}.
 */
public class AudioRecordActivity extends AppCompatActivity {
    private static final String TAG = "AudioRecordActivity";
    private static final int SAMPLING_RATE_IN_HZ = 44100;

    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;

    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    /**
     * Factor by that the minimum buffer size is multiplied. The bigger the factor is the less
     * likely it is that samples will be dropped, but more memory will be used. The minimum buffer
     * size is determined by {@link AudioRecord#getMinBufferSize(int, int, int)} and depends on the
     * recording settings.
     */
    private static final int BUFFER_SIZE_FACTOR = 2;

    /**
     * Size of the buffer where the audio data is stored by Android
     */
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLING_RATE_IN_HZ,
            CHANNEL_CONFIG, AUDIO_FORMAT) * BUFFER_SIZE_FACTOR;

    /**
     * Signals whether a recording is in progress (true) or not (false).
     */
    private final AtomicBoolean recordingInProgress = new AtomicBoolean(false);

    private AudioRecord recorder = null;

    private Thread recordingThread = null;

    private Button startButton;

    private Button stopButton;

    private Button goBluetoothButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio);

        startButton = (Button) findViewById(R.id.btnStart);
        startButton.setEnabled(true);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
            }
        });

        stopButton = (Button) findViewById(R.id.btnStop);
        stopButton.setEnabled(false);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
            }
        });
        goBluetoothButton = findViewById(R.id.goBluetooth);
        goBluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(AudioRecordActivity.this, BluetoothRecordActivity.class);
                startActivity(it);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 开始录音之后会自动触发
        Log.d(TAG, "onResume: Resumed!");
        // startButton.setEnabled(true);
        // stopButton.setEnabled(false);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(TAG, "onPause: Paused!");
        // 开始录音之后会自动触发
        // stopRecording();
    }

    private void startRecording() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, 123);
            return;
        }
        recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLING_RATE_IN_HZ,
                CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);

        recorder.startRecording();

        recordingInProgress.set(true);

        recordingThread = new Thread(new RecordingRunnable(), "Recording Thread");
        recordingThread.start();
    }

    private void stopRecording() {
        if (null == recorder) {
            return;
        }

        Log.d(TAG, "stopRecording: stopped!");
        recordingInProgress.set(false);

        recorder.stop();

        recorder.release();

        recorder = null;

        recordingThread = null;
    }

    private class RecordingRunnable implements Runnable {

        @Override
        public void run() {
            ActivityCompat.requestPermissions( AudioRecordActivity.this,
                    new String[]{
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    }, 1
            );
            String outFilePath = AppUtil.getOpusFileDirPath(AudioRecordActivity.this
            ) + File.separator + "recording.pcm";
            final File file = new File(outFilePath);
            final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

            try (final FileOutputStream outStream = new FileOutputStream(file)) {
                while (recordingInProgress.get()) {
                    int result = recorder.read(buffer, BUFFER_SIZE);
                    if (result < 0) {
                        throw new RuntimeException("Reading of audio buffer failed: " +
                                getBufferReadFailureReason(result));
                    }
                    outStream.write(buffer.array(), 0, BUFFER_SIZE);
                    buffer.clear();
                }
            } catch (IOException e) {
                throw new RuntimeException("Writing of recorded audio failed", e);
            }
            Log.d(TAG, "run: RecordingThread exit!");
        }

        private String getBufferReadFailureReason(int errorCode) {
            switch (errorCode) {
                case AudioRecord.ERROR_INVALID_OPERATION:
                    return "ERROR_INVALID_OPERATION";
                case AudioRecord.ERROR_BAD_VALUE:
                    return "ERROR_BAD_VALUE";
                case AudioRecord.ERROR_DEAD_OBJECT:
                    return "ERROR_DEAD_OBJECT";
                case AudioRecord.ERROR:
                    return "ERROR";
                default:
                    return "Unknown (" + errorCode + ")";
            }
        }
    }
}
