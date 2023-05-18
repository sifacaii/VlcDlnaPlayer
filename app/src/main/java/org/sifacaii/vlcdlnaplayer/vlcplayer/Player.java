package org.sifacaii.vlcdlnaplayer.vlcplayer;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.MediaController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.util.ArrayList;
import java.util.Arrays;

public class Player extends VLCVideoLayout implements MediaPlayer.EventListener, MediaController.MediaPlayerControl {
    private String TAG = "VLCPLAYER";

    private String[] debugOptions = {"-vvv"};
    public float speedRate[] = {0.5f, 1.0f, 1.5f, 2.0f}; //倍速播放列表
    public MediaPlayer.ScaleType[] scaleTypes = org.videolan.libvlc.MediaPlayer.ScaleType.getMainScaleTypes(); //缩放类型列表
    private Context mContext;
    private LibVLC mLibVLC;
    private MediaPlayer mMediaPlayer;
    private Controller mController;
    private long mDuration = 0;
    private long mCurrentTime = 0;
    private int mBufferPercentage = 0;

    public Video mCurrentVideo;

    public Player(@NonNull Context context) {
        super(context);
        mContext = context;
        init();
    }

    public Player(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public Player(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Player(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        init();
    }

    public void init() {
        mLibVLC = new LibVLC(mContext, new ArrayList<>(Arrays.asList(debugOptions)));
        mMediaPlayer = new MediaPlayer(mLibVLC);
        mMediaPlayer.attachViews(this, null, true, false);
        mMediaPlayer.setEventListener(this);

        setFocusable(true);
        setFocusableInTouchMode(true);

        mController = new Controller(mContext);
        mController.setMediaPlayer(this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        addView(mController, layoutParams);
    }

    public void setMedia(Video v) {
        mCurrentVideo = v;
        String path = v.Url;
        Media media;
        if (path.startsWith("http")) {
            media = new Media(mLibVLC, Uri.parse(path));
        } else {
            media = new Media(mLibVLC, path);
        }
        media.setHWDecoderEnabled(v.Hacc, v.ForceHacc);
        if (!v.StartInBegin) {
            long startTime = v.PositionTicks / 1000L;
            if (startTime > 60) media.addOption(":start-time=" + startTime); //设置开始位置
        }

        mMediaPlayer.setMedia(media);
        media.release();
        mMediaPlayer.play();
    }

    public Video getCurrentVideo() {
        return mCurrentVideo;
    }

    private PlayEvent playEvent;

    public void setPlayEvent(PlayEvent pe) {
        playEvent = pe;
    }

    public void release() {
        if(mController != null) mController.finish();
        mMediaPlayer.release();
        mLibVLC.release();
    }

    public interface PlayEvent {
        void onStart(long tricks);

        void onPause(boolean isPause);

        void onStop();

        void onTimeChanged(long triks);

        void onMessage(String msg);
    }

    private long oldTime = 0;
    @Override
    public void onEvent(MediaPlayer.Event event) {
        switch (event.type) {
            case org.videolan.libvlc.MediaPlayer.Event.Playing: // 开始播放
                if (mController != null) {
                    mController.initTrackList();
                    mController.show();
                    mController.pauseImgShowOrHide(false);
                }
                if (playEvent != null) playEvent.onStart(event.getTimeChanged());
                break;
            case org.videolan.libvlc.MediaPlayer.Event.MediaChanged: // 切换新播放条目 setMedia
                break;
            case org.videolan.libvlc.MediaPlayer.Event.Buffering: //载入中
                if (mController != null) {
                    mController.showBuffing(event.getBuffering());
                }
                break;
            case org.videolan.libvlc.MediaPlayer.Event.Paused: //暂停
                if (mController != null) mController.pauseImgShowOrHide(true);
                if (playEvent != null) playEvent.onPause(true);
                break;
            case org.videolan.libvlc.MediaPlayer.Event.Stopped:    //停止
                if (mController != null) mController.finish();
                if (playEvent != null) playEvent.onStop();
                break;
            case org.videolan.libvlc.MediaPlayer.Event.TimeChanged: //进度
                mCurrentTime = event.getTimeChanged();
                if ((mCurrentTime - oldTime) > 10000) {
                    oldTime = mCurrentTime;
                    if (playEvent != null) playEvent.onTimeChanged(mCurrentTime);
                }
                break;
            case org.videolan.libvlc.MediaPlayer.Event.LengthChanged: //长度
                mDuration = event.getLengthChanged();
                break;
        }
    }

    public ArrayList<Video> getPlayList() {
        return new ArrayList<>();
    }

    public int getPlayIndex(){
        return 0;
    }

    @Override
    public void start() {
        if (mMediaPlayer.hasMedia()) {
            mMediaPlayer.play();
        }
    }

    @Override
    public void pause() {
        mMediaPlayer.pause();
    }

    public void stop() {
        mMediaPlayer.stop();
    }

    @Override
    public int getDuration() {
        return (int)mDuration;
    }

    @Override
    public int getCurrentPosition() {
        return (int) mCurrentTime;
    }

    @Override
    public void seekTo(int pos) {
        mMediaPlayer.setTime(pos);
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return mBufferPercentage;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return mMediaPlayer.isSeekable();
    }

    @Override
    public boolean canSeekForward() {
        return mMediaPlayer.isSeekable();
    }

    @Override
    public int getAudioSessionId() {
        return mMediaPlayer.getAudioTrack();
    }

    public int getSubtitleSessionId() {
        return mMediaPlayer.getSpuTrack();
    }

    public void setAudioSessionId(int id) {
        if (id != getAudioSessionId()) mMediaPlayer.setAudioTrack(id);
    }

    public void setSubtitleSessionId(int id) {
        if (id != getSubtitleSessionId()) mMediaPlayer.setSpuTrack(id);
    }

    public MediaPlayer.TrackDescription[] getAudioSessions() {
        return mMediaPlayer.getAudioTracks();
    }

    public MediaPlayer.TrackDescription[] getSubtitleSessions() {
        return mMediaPlayer.getSpuTracks();
    }

    public MediaPlayer.ScaleType getScale() {
        return mMediaPlayer.getVideoScale();
    }

    public void setScale(MediaPlayer.ScaleType scale) {
        if (scale != getScale()) mMediaPlayer.setVideoScale(scale);
    }

    public float getSpeed() {
        return mMediaPlayer.getRate();
    }

    public void setSpeed(float speed) {
        if (speed != getSpeed()) mMediaPlayer.setRate(speed);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(mController != null){
            return mController.dispatchKeyEvent(event);
        }
        return super.dispatchKeyEvent(event);
    }
}
