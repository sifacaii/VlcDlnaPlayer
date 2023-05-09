package org.sifacaii.vlcdlnaplayer.vlcplayer;


import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.sifacaii.vlcdlnaplayer.R;
import org.videolan.libvlc.MediaPlayer;

public class Controller extends FrameLayout implements View.OnClickListener {
    private String TAG = "播放器控制器";


    private int defaultTimeout = 6000;
    private Player player;

    private Context mContext;

    private LinearLayout mContainerTitle;
    private LinearLayout mContainerBottom;
    private LinearLayout mContainerSeek;
    private TextView mSeekTime;
    private ProgressBar mSeekProgress;
    private ProgressBar mBufferProgress;

    private ImageButton mPauseButton;
    private ImageButton mFfwdButton;
    private ImageButton mRewButton;
    private ImageButton mNextButton;
    private ImageButton mPrevButton;
    private TextView mRateButton;
    private ImageButton mScaleButton;
    private TextView mAudioButton;
    private TextView mSubtitleButton;
    private ImageButton mPlayListButton;

    private ProgressBar mProgress;
    private TextView mEndTime;
    private TextView mCurrentTime;
    private TextView mTitle;

    private boolean mShowing = false;

    private boolean mPopupMenuShowing = false;
    private PopMenuP mRateMenu;
    private PopMenuP mScaleMenu;
    private PopMenuP mAudioMenu;
    private PopMenuP mSubtitleMenu;
    private PopMenuP mPlayListMenu;

    public Controller(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public Controller(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public Controller(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Controller(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        init();
    }

    private void init() {
        LayoutInflater.from(mContext).inflate(R.layout.controller, this);

        mContainerTitle = findViewById(R.id.container_title);
        mContainerBottom = findViewById(R.id.container_bottom);
        mContainerSeek = findViewById(R.id.container_seek);
        mSeekTime = findViewById(R.id.seek_time);
        mSeekProgress = findViewById(R.id.seek_progress);
        mBufferProgress = findViewById(R.id.buffer_Progress);
        mPauseButton = findViewById(R.id.pause);
        mFfwdButton = findViewById(R.id.ffwd);
        mRewButton = findViewById(R.id.rew);
        mNextButton = findViewById(R.id.next);
        mPrevButton = findViewById(R.id.prev);
        mRateButton = findViewById(R.id.rate);
        mScaleButton = findViewById(R.id.scale);
        mAudioButton = findViewById(R.id.audiotrack);
        mSubtitleButton = findViewById(R.id.subtitletrack);
        mPlayListButton = findViewById(R.id.playlist);

        mProgress = findViewById(R.id.mediacontroller_progress);
        mEndTime = findViewById(R.id.time);
        mCurrentTime = findViewById(R.id.time_current);
        mTitle = findViewById(R.id.title);

        mPauseButton.setOnClickListener(this);
        mFfwdButton.setOnClickListener(this);
        mRewButton.setOnClickListener(this);
        mNextButton.setOnClickListener(this);
        mPrevButton.setOnClickListener(this);
        mRateButton.setOnClickListener(this);
        mScaleButton.setOnClickListener(this);
        mAudioButton.setOnClickListener(this);
        mSubtitleButton.setOnClickListener(this);
        mPlayListButton.setOnClickListener(this);
    }

    public void setMediaPlayer(Player player) {
        this.player = player;
        initPopupMenu();
    }

    private void initPopupMenu() {
        // 速率
        mRateMenu = new PopMenuP(mContext, mRateButton);
        for (int i = 0; i < player.speedRate.length; i++) {
            mRateMenu.add(mRateButton.getId(), i, i, String.valueOf(player.speedRate[i]));
        }
        mRateMenu.setOnItemClickListener(new PopMenuP.OnItemClickListener() {
            @Override
            public void onClick(PopMenuP.menu m) {
                player.setSpeed(Float.valueOf(m.name));
                mRateButton.setText(m.name + "x");
            }
        });
        mRateMenu.setOnDismissListener(popupMenuDismissListener);

        // 画面缩放
        mScaleMenu = new PopMenuP(mContext, mScaleButton);
        for (int i = 0; i < player.scaleTypes.length; i++) {
            mScaleMenu.add(mScaleButton.getId(), i, i, player.scaleTypes[i].name());
        }
        mScaleMenu.setOnDismissListener(popupMenuDismissListener);
        mScaleMenu.setOnItemClickListener(new PopMenuP.OnItemClickListener() {
            @Override
            public void onClick(PopMenuP.menu m) {
                player.setScale(player.scaleTypes[m.id]);
            }
        });
    }

    public void initTrackList() {
        // 音轨
        MediaPlayer.TrackDescription[] audioTracks = player.getAudioSessions();
        if (mAudioButton != null && audioTracks != null) {
            mAudioMenu = new PopMenuP(mContext, mAudioButton);
            for (int i = 0;i<audioTracks.length;i++) {
                mAudioMenu.add(mAudioButton.getId(), audioTracks[i].id, i, audioTracks[i].name);
            }
            if (mAudioMenu.size() > 0) {
                mAudioMenu.setOnDismissListener(popupMenuDismissListener);
                mAudioMenu.setOnItemClickListener(new PopMenuP.OnItemClickListener() {
                    @Override
                    public void onClick(PopMenuP.menu m) {
                        int audioid = m.id;
                        if (audioid == 0) audioid = -1;
                        player.setAudioSessionId(audioid);
                    }
                });
            }
        }

        //字幕
        MediaPlayer.TrackDescription[] subtitleTracks = player.getSubtitleSessions();
        if (mSubtitleButton != null && subtitleTracks != null) {
            mSubtitleMenu = new PopMenuP(mContext, mSubtitleButton);
            for (int i=0;i<subtitleTracks.length;i++) {
                mSubtitleMenu.add(mSubtitleButton.getId(), subtitleTracks[i].id, i, subtitleTracks[i].name);
            }
            if (mSubtitleMenu.size() > 0) {
                mSubtitleMenu.setOnDismissListener(popupMenuDismissListener);
                mSubtitleMenu.setOnItemClickListener(new PopMenuP.OnItemClickListener() {
                    @Override
                    public void onClick(PopMenuP.menu m) {
                        int subid = m.id;
                        if (subid == 0) subid = -1;
                        player.setSubtitleSessionId(subid);
                    }
                });
            }
        }

        // 播放列表
//        ArrayList<Video> playlist = player.getPlayList();
//        if (mPlayListButton != null && playlist.size() > 0) {
//            mPlayListMenu = new PopMenuP(mContext, mPlayListButton);
//            for (int i = 0; i < playlist.size(); i++) {
//                mPlayListMenu.add(mPlayListButton.getId(), i, i, i + playlist.get(i).Name);
//            }
//            if (mPlayListMenu.size() > 0) {
//                mPlayListMenu.setOnDismissListener(popupMenuDismissListener);
//                mPlayListMenu.setOnItemClickListener(new PopMenuP.OnItemClickListener() {
//                    @Override
//                    public void onClick(PopMenuP.menu m) {
//                        if (m.id != player.getPlayIndex()) player.start(m.id);
//                    }
//                });
//            }
//        }

        String title = player.getCurrentVideo().Name;
        if (!mTitle.equals("")) {
            mTitle.setText(title);
        }
    }

    private void updatePausePlay() {
        if (player.isPlaying()) {
            // com.android.internal.R.drawable.ic_media_pause
            mPauseButton.setImageResource(Resources.getSystem().getIdentifier("ic_media_pause", "drawable", "android"));
        } else {
            // com.android.internal.R.drawable.ic_media_play
            mPauseButton.setImageResource(Resources.getSystem().getIdentifier("ic_media_play", "drawable", "android"));
        }
    }

    private final PopupWindow.OnDismissListener popupMenuDismissListener = new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
            mPopupMenuShowing = false;
            show(3000);
        }
    };

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == mPauseButton.getId()) {
            player.start();
            updatePausePlay();
        } else if (id == mFfwdButton.getId()) {
            player.seekTo(player.getCurrentPosition() - 10000);
        } else if (id == mRewButton.getId()) {
            player.seekTo(player.getCurrentPosition() + 10000);
        } else if (id == mNextButton.getId()) {
            //player.nextVideo();
        } else if (id == mPrevButton.getId()) {
            //player.preVideo();
        } else if (id == mRateButton.getId()) {
            mPopupMenuShowing = true;
            String ratex = String.valueOf(player.getSpeed());
            mRateMenu.show(ratex);
        } else if (id == mScaleButton.getId()) {
            mPopupMenuShowing = true;
            String scale = player.getScale().name();
            mScaleMenu.show(scale);
        } else if (id == mAudioButton.getId()) {
            mPopupMenuShowing = true;
            if (mAudioMenu != null) mAudioMenu.show(player.getAudioSessionId());
        } else if (id == mSubtitleButton.getId()) {
            mPopupMenuShowing = true;
            if (mSubtitleMenu != null) mSubtitleMenu.show(player.getSubtitleSessionId());
        } else if (id == mPlayListButton.getId()) {
            mPopupMenuShowing = true;
            //if (mPlayListMenu != null) mPlayListMenu.show(player.getPlayIndex());
        }
        show();
    }

    private final Runnable mFadeOut = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private final Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            long pos = setProgress();
            if (mShowing && player.isPlaying()) {
                postDelayed(mShowProgress, 1000 - (pos % 1000));
            }
        }
    };

    private long setProgress() {
        if (player == null || !mShowing) {
            return 0;
        }
        long position = player.getCurrentPosition();
        long duration = player.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
            //int percent = mMediaplayer.getBufferPercentage();
            //mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(position));

        return position;
    }

    public void show(int timeout) {
        if (!mShowing) {
            setProgress();
            mContainerTitle.setVisibility(VISIBLE);
            mContainerBottom.setVisibility(VISIBLE);
            mShowing = true;
            post(mShowProgress);
            if (mPauseButton != null) {
                mPauseButton.requestFocus();
            }
        }
        updatePausePlay();
        if (timeout != 0) {
            Log.d(TAG, "show: 稍后隐藏");
            removeCallbacks(mFadeOut);
            postDelayed(mFadeOut, timeout);
        }
    }

    public void show() {
        show(defaultTimeout);
    }

    public void hide() {
        Log.d(TAG, "hide: 隐藏");
        if (mPopupMenuShowing || !mShowing)
            return;

        Log.d(TAG, "hide: 隐藏２２２");
        removeCallbacks(mShowProgress);
        mContainerTitle.setVisibility(GONE);
        mContainerBottom.setVisibility(GONE);
        mShowing = false;
    }

    public void finish() {
        if (mRateMenu != null) mRateMenu.dismiss();
        if (mScaleMenu != null) mScaleMenu.dismiss();
        if (mAudioMenu != null) mAudioMenu.dismiss();
        if (mSubtitleMenu != null) mSubtitleMenu.dismiss();
        if (mPlayListMenu != null) mPlayListMenu.dismiss();
        removeCallbacks(mShowProgress);
        removeCallbacks(mFadeOut);
    }

    public void showBuffing(float buffing) {
        Log.d(TAG, "setBuffing: " + buffing);
        if (buffing < 100) {
            if (mBufferProgress.getVisibility() == GONE) {
                mBufferProgress.setVisibility(VISIBLE);
            }
        } else {
            if (mBufferProgress.getVisibility() != GONE) {
                mBufferProgress.setVisibility(GONE);
            }
        }
    }

    public void pauseImgShowOrHide(Boolean show){
        if(show){
            findViewById(R.id.img_pause).setVisibility(VISIBLE);
        }else{
            findViewById(R.id.img_pause).setVisibility(GONE);
        }
    }

    private String stringForTime(long timeMs) {
        long totalSeconds = timeMs / 1000;

        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;

        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }

    private boolean simSlideStart = false;
    private int simSeekPosition = 0;
    private long simSlideOffset = 0;

    public void tvSlideStop() {
        if (!simSlideStart)
            return;
        player.seekTo(simSeekPosition);
        simSlideStart = false;
        simSeekPosition = 0;
        simSlideOffset = 0;
    }

    public void tvSlideStart(int dir) {
        int duration = (int) player.getDuration();
        if (duration <= 0)
            return;
        if (!simSlideStart) {
            simSlideStart = true;
        }
        // 每次10秒
        simSlideOffset += (10000.0f * dir);
        int currentPosition = (int) player.getCurrentPosition();
        int position = (int) (simSlideOffset + currentPosition);
        if (position > duration) position = duration;
        if (position < 0) position = 0;
        updateSeekUI(currentPosition, position, duration);
        simSeekPosition = position;
    }

    private void updateSeekUI(int currentPosition, int position, int duration) {
        if (mContainerSeek.getVisibility() == GONE) mContainerSeek.setVisibility(VISIBLE);
        removeCallbacks(hideSeekContainerRunable);
        String time = stringForTime(position) + "/" + stringForTime(duration);
        int pos = (int) (position * 1.0 / duration * mSeekProgress.getMax());
        mSeekTime.setText(time);
        mSeekProgress.setProgress(pos);
        postDelayed(hideSeekContainerRunable, 2000);
    }

    private Runnable hideSeekContainerRunable = new Runnable() {
        @Override
        public void run() {
            mContainerSeek.setVisibility(GONE);
        }
    };

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keycode = event.getKeyCode();
        if (mShowing) {
            if (keycode == KeyEvent.KEYCODE_BACK) {
                hide();
            } else {
                show();
            }
            return super.dispatchKeyEvent(event);
        }

        if (action == KeyEvent.ACTION_DOWN) {
            switch (keycode) {
                case KeyEvent.KEYCODE_ENTER:
                    player.start();
                    updatePausePlay();
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    show();
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (player.isPlaying()) {
                        tvSlideStart(keycode == KeyEvent.KEYCODE_DPAD_RIGHT ? 1 : -1);
                    }
                    break;
                case KeyEvent.KEYCODE_BACK:
                    player.stop();
            }
            return true;
        }
        if (action == KeyEvent.ACTION_UP) {
            if (keycode == KeyEvent.KEYCODE_DPAD_RIGHT || keycode == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (player.isPlaying()) {
                    tvSlideStop();
                }
            }
            return true;
        }
        return true;
    }
}

