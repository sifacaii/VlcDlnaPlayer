package org.sifacaii.vlcdlnaplayer;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MessageEvent {

    public static enum Event{
        Play(1),
        Pause(2),
        Stop(3),
        TimeChanged(4),
        Message(5);
        Event(int i) {}
    };

    private Event event;

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    private String MSG;

    public String getMSG() {
        return MSG;
    }

    public void setMSG(String MSG) {
        this.MSG = MSG;
    }
}
