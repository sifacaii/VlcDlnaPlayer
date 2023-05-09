package org.sifacaii.vlcdlnaplayer.dlna;

import android.content.Context;
import android.os.Build;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;


public class SSDP {
    private String TAG = "广播器";
    private Context context;
    private final String SOCKET_HOST = "239.255.255.250";
    private final int SOCKET_PORT = 1900;
    private MulticastSocket mSocket;
    private Thread BroderCastThead;

    public final static String[] DeviceType = {
            "urn:schemas-upnp-org:device:MediaRenderer:1",
            "ssdp:all",
            "upnp:rootdevice"
    };

    public SSDP(Context context) {
        this.context = context;
        try {
            BroderCastThead = new Thread(this::sss);
            mSocket = new MulticastSocket(SOCKET_PORT);
            mSocket.joinGroup(InetAddress.getByName(SOCKET_HOST));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        BroderCastThead.start();
    }

    public void stop() {
        BroderCastThead.stop();
    }

    private void sss() {
        try {
            reviceNOTIFY();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void reviceNOTIFY() throws IOException {
        while (mSocket != null) {
            byte[] buff = new byte[1024];
            DatagramPacket inPacket = new DatagramPacket(buff, buff.length);
            mSocket.receive(inPacket);
            String clientIP = inPacket.getAddress().getHostAddress();
            int clientPort = inPacket.getPort();
            String data = new String(inPacket.getData()).trim();
            String sendmsg = processMSG(data);
            if (!sendmsg.equals("")) {
                sendNOTIFY(sendmsg, clientIP, clientPort);
            }
        }
    }

    private void sendNOTIFY(String msg, String clientIP, int clientPort) throws IOException {
        byte[] data = msg.getBytes();
        DatagramPacket outPacket = new DatagramPacket(data, data.length);
        outPacket.setAddress(InetAddress.getByName(clientIP));
        outPacket.setPort(clientPort);
        mSocket.send(outPacket);
    }

    private String processMSG(String data) {
        String ret = "";
        NOTIFY notify = new NOTIFY(data);
        if (notify.CMD.startsWith("M-SEARCH")) {
            if (notify.MAN.equals("ssdp:discover")) {
                if (isMatchType(notify.ST)) {
                    ret = getDeviceInfo(notify.ST);
                }
            }
        } else if (notify.CMD.startsWith("NOTIFY")) {
            // 接收到通知；
        }
        return ret;
    }

    private static boolean isMatchType(String type) {
        boolean ismatch = false;
        for (String t : DeviceType) {
            if (t.equals(type)) return true;
        }
        return ismatch;
    }

    private String getDeviceInfo(String st) {
        String info = "HTTP/1.1 200 OK\n" +
                "CACHE-CONTROL: max-age=30\n" +
                "EXT:\n" +
                "LOCATION: http://" + AVTransport.localAddress + ":" + AVTransport.port + "/description.xml\n" +
                "SERVER: Linux/1 UPnP/1.0 LIBVLC/3.5.9\n" +
                "USN: uuid:VLC_DLNA_PLAYER_UPNP_1_0_202305065544::" + st + "\n" +
                "NT: " + st;
        return info;
    }
}
