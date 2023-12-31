package com.nfcstar.norder.util;


import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.util.Log;

/* SoundPoolPlayer:
   custom extention from SoundPool with setOnCompletionListener
   without the low-efficiency drawback of MediaPlayer
   author: kenliu
*/
public class SoundPlayer extends SoundPool {
    Context context;
    int soundId;
    int streamId;
    int resId;
    long duration;
    boolean isPlaying = false;
    boolean loaded = false;
    MediaPlayer.OnCompletionListener listener;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (isPlaying) {
                isPlaying = false;
                Log.d("debug", "ending..");
                if (listener != null) {
                    listener.onCompletion(null);
                }
            }
        }
    };

    public int getStreamId() {
        return streamId;
    }

    //timing related
    Handler handler;
    long startTime;
    long endTime;
    long timeSinceStart = 0;


    public void loop(int i) {
        if (streamId > 0) {
            this.setLoop(streamId, i);
        }
    }

    public void pause() {
        if (streamId > 0) {
            endTime = System.currentTimeMillis();
            timeSinceStart += endTime - startTime;
            super.pause(streamId);
            if (handler != null) {
                handler.removeCallbacks(runnable);
            }
            isPlaying = false;
        }
    }

    public void stop() {
        if (streamId > 0) {
            timeSinceStart = 0;
            super.stop(streamId);
            if (handler != null) {
                handler.removeCallbacks(runnable);
            }
            isPlaying = false;
        }
    }

    public void play() {
        if (!loaded) {
            loadAndPlay();
        } else {
            playIt();
        }
    }

    public static SoundPlayer create(Context context, int resId) {
        SoundPlayer player = new SoundPlayer(1, AudioManager.STREAM_MUSIC, 0);
        player.context = context;
        player.resId = resId;
        return player;
    }

    public SoundPlayer(int maxStreams, int streamType, int srcQuality) {
        super(1, streamType, srcQuality);
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
        this.listener = listener;
    }

    private void loadAndPlay() {
        duration = getSoundDuration(resId);
        soundId = super.load(context, resId, 1);
        setOnLoadCompleteListener(new OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
                playIt();
            }
        });
    }

    private void playIt() {
        if (loaded && !isPlaying) {
            Log.d("debug", "start playing..");
            if (timeSinceStart == 0) {
                streamId = super.play(soundId, 1f, 1f, 1, 0, 1f);
            } else {
                super.resume(streamId);
            }
            startTime = System.currentTimeMillis();
            handler = new Handler();
            handler.postDelayed(runnable, duration - timeSinceStart);
            isPlaying = true;
        }
    }

    private long getSoundDuration(int rawId) {
        MediaPlayer player = MediaPlayer.create(context, rawId);
        int duration = player.getDuration();
        return duration;
    }
}