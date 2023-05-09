package org.sifacaii.vlcdlnaplayer.dlna;

public class NOTIFY {

    public String CMD;
    public String HOST;
    public String ST;
    public String MAN;
    public String MX;

    public NOTIFY(String msg){
        String[] d = msg.split("\n");
        if(d.length > 0){
            CMD = d[0];
        }
        for(int i=1;i<d.length;i++){
            String dd = d[i].trim();
            String[] ddd = dd.split(":",2);
            if(ddd.length < 2) continue;

            String ds = ddd[1].trim();
            switch (ddd[0]){
                case "HOST":
                    HOST = ds;
                    break;
                case "ST":
                    ST = ds;
                    break;
                case "MAN":
                    MAN = ds.replace("\"","");
                    break;
                case "MX":
                    MX = ds;
                    break;
            }
        }
    }
}
