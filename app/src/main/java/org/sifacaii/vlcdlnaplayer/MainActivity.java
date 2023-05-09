package org.sifacaii.vlcdlnaplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.orhanobut.hawk.Hawk;

import org.sifacaii.vlcdlnaplayer.dlna.AVTransport;
import org.sifacaii.vlcdlnaplayer.dlna.SSDP;
import org.sifacaii.vlcdlnaplayer.vlcplayer.Player;
import org.sifacaii.vlcdlnaplayer.vlcplayer.Video;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private String TAG = "主窗口：";

    private TextView textView_logs;
    private FrameLayout play_container;
    private Player player;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView_logs = findViewById(R.id.textview_logs);
        play_container = findViewById(R.id.play_container);
        init();
    }

    protected void init() {
        Hawk.init(getApplicationContext()).build();
        SSDP ssdp = new SSDP(this);
        AVTransport avTransport = AVTransport.getInstance(this);
        avTransport.setPlayerControl(playerControl);
        ssdp.start();
    }

    protected void initParams(){

    }

    protected AVTransport.PlayerControl playerControl = new AVTransport.PlayerControl() {
        @Override
        public void SetAVTransportURI(String url, String metadata) {
            Video v = new Video();
            v.Url = url;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (player != null) {
                        player.setMedia(v);
                    } else {
                        player = new Player(MainActivity.this);
                        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, -1);
                        play_container.addView(player, lp);
                        player.setMedia(v);
                    }
                }
            });
        }

        @Override
        public void Play(String Speed) {
            if (player != null) player.start();
        }

        @Override
        public void Pause() {
            if (player != null) player.pause();
        }

        @Override
        public HashMap<String, String> GetTransportInfo() {
            HashMap<String, String> map = new HashMap<>();
            //if (player == null) return map;
            String state = "STOPPED";
            if(player == null){
                state = "STOPPED";
            }else{
                state = player.isPlaying() ? "PLAYING":"PAUSED_PLAYBACK";
            }
            map.put("CurrentTransportState", state);
            map.put("CurrentTransportStatus", "OK");
            map.put("CurrentSpeed", String.valueOf(player.getSpeed()));
            return map;
        }

        @Override
        public void Seek(String Unit, String Target) {
            Log.d(TAG, "Seek: Unit:" + Unit + " Target:" + Target);
            if(player != null) player.seekTo((int)TimeForstring(Target));
        }

        @Override
        public HashMap<String, String> GetPositionInfo() {
            //if (player != null) return String.valueOf(player.getCurrentPosition());
            HashMap<String, String> map = new HashMap<>();
            if (player == null) return map;
            map.put("Track", "0");
            map.put("TrackDuration", stringForTime(player.getDuration()));
            map.put("TrackMetaData", "");
            map.put("TrackURI", "");
            map.put("RelTime", stringForTime(player.getCurrentPosition()));
            map.put("AbsTime", "");
            map.put("RelCount", "");
            map.put("AbsCount", "");
            return map;
        }

        @Override
        public HashMap<String, String> GetMediaInfo() {
            return null;
        }

        @Override
        public void Stop() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (player != null) {
                        player.stop();
                        player.release();
                        play_container.removeView(player);
                        player = null;
                        Log.d(TAG, "run: 停止");
                    }
                }
            });
        }
    };

    private String stringForTime(long timeMs) {
        long totalSeconds = timeMs / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private long TimeForstring(String time) {
        String[] ts = time.split(":");
        long hours = Integer.parseInt(ts[0]) * 3600;
        long minutes = Integer.parseInt(ts[1]) * 60;
        long seconds = Integer.parseInt(ts[2]);
        long totalSeconds = (hours + minutes + seconds) * 1000;
        return totalSeconds;
    }

    protected void appExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("确认退出？");
        builder.setTitle("确认退出");
        builder.setPositiveButton("退出", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                System.exit(0);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
}