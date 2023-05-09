package org.sifacaii.vlcdlnaplayer.dlna;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class AVTransport extends NanoHTTPD {
    private String TAG = "AVTransport服务：";

    public static final String localAddress = Utils.getIpAddress4();
    public static final int port = 63636;
    private Context mContext;
    private PlayerControl playerControl;

    public void setContext(Context context) {
        mContext = context;
    }

    public static AVTransport getInstance(Context context) {
        AVTransport avTransport = new AVTransport(port);
        avTransport.setContext(context);
        try {
            avTransport.start();
        } catch (IOException e) {
            Log.e("CONTROL", "getInstance: ", e);
            return null;
        }
        return avTransport;
    }

    public AVTransport(int port) {
        super(port);
    }

    public AVTransport(String hostname, int port) {
        super(hostname, port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        if (session.getMethod() == Method.GET) {
            try {
                InputStream is = mContext.getAssets().open("upnp" + uri);
                return newFixedLengthResponse(Response.Status.OK, "text/xml", is, is.available());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (session.getMethod() == Method.POST) {
            String rsp = "";
            Map<String, String> files = new HashMap<>();
            Map<String, String> header = session.getHeaders();
            String soapaction = header.get("soapaction");
            String xml = "";
            try {
                session.parseBody(files);
                xml = files.get("postData");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ResponseException e) {
                throw new RuntimeException(e);
            }
            if (uri.equals("/control")) {
                rsp = MediaControl(soapaction, xml);
            }
            //Log.d(TAG, "serve: " + uri + " rsp:" + rsp);
            return newFixedLengthResponse(Response.Status.OK, "text/xml", rsp);
        }

        return newFixedLengthResponse(Response.Status.NO_CONTENT, "text/xml", "");
    }

    private String MediaControl(String soapaction, String xml) {
        Log.d(TAG, "MediaControl_" + soapaction + ": " + xml);
        if (playerControl == null) return "";
        if (xml == null || xml.length() < 1) return "";

        String rsp = "";

        HashMap<String, String> x = null;
        try {
            x = ParseXML(xml);
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (x == null) return rsp;

        String[] action = soapaction.split("#");
        if (action.length < 2) return rsp;
        //Log.d(TAG, "MediaControl: action:" + action[1] + "   xml:" + x);

        String act = action[1].replace("\"", "");
        switch (act) {
            case "SetAVTransportURI":
                playerControl.SetAVTransportURI(x.get("CurrentURI"), x.get("CurrentURIMetaData"));
                break;
            case "Play":
                playerControl.Play(x.get("Speed"));
                break;
            case "Pause":
                playerControl.Pause();
                break;
            case "GetTransportInfo":
                rsp = getRspXML("GetTransportInfo", playerControl.GetTransportInfo());
                break;
            case "Seek":
                playerControl.Seek(x.get("Unit"), x.get("Target"));
                break;
            case "GetPositionInfo":
                rsp = getRspXML("GetPositionInfo", playerControl.GetPositionInfo());
                break;
            case "GetMediaInfo":
                rsp = getRspXML("GetMediaInfo", playerControl.GetMediaInfo());
                break;
            case "Stop":
                playerControl.Stop();
                break;
        }
        Log.d(TAG, "MediaControl_rsp: " + rsp);
        return rsp;
    }

    public HashMap<String, String> ParseXML(String xml) throws XmlPullParserException, IOException {
        HashMap<String, String> result = new HashMap<>();
        XmlPullParser xmlPullParser = Xml.newPullParser();
        xmlPullParser.setInput(new StringReader(xml));

        int eventType = xmlPullParser.getEventType();
        String tagName = "";
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    tagName = xmlPullParser.getName();
                    break;
                case XmlPullParser.TEXT:
                    String value = xmlPullParser.getText();
                    if (value != null) {
                        value = value.trim();
                        if (!value.equals("")) {
                            result.put(tagName, value);
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    break;
            }
            eventType = xmlPullParser.next();
        }
        return result;
    }

    private String getRspXML(String action, HashMap<String, String> map) {
        String rsp = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"" +
                " s:encodingstyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
                "<s:Body>" +
                "<u:" + action + "Response xmlns:u=\"urn:schemas-upnp-org:service:AVTransport:1\">";

        if (map != null) {
            for (String key : map.keySet()) {
                rsp += "<" + key + ">" + map.get(key) + "</" + key + ">";
            }
        }

        rsp += "</u:" + action + "Response>" +
                "</s:Body>" +
                "</s:Envelope>";

        return rsp;
    }

    public void setPlayerControl(PlayerControl playerControl) {
        this.playerControl = playerControl;
    }

    public interface PlayerControl {
        void SetAVTransportURI(String url, String metadata);

        void Play(String Speed);

        void Pause();

        HashMap<String, String> GetTransportInfo();

        void Seek(String Unit, String Target);

        HashMap<String, String> GetPositionInfo();

        HashMap<String, String> GetMediaInfo();

        void Stop();
    }
}
