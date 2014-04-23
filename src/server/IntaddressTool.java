package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class IntaddressTool {

    public static void main(String[] args) {
    }

    public static String getwebData() {
        String cccDate = getmyWebIp("http://game.xctzf.com/1server/checkipgms.txt");
        return cccDate;
    }

    public static String getwebip() {
        String cccDate = getmyWebIp("http://game.xctzf.com/1server/ip.asp");
        return cccDate;
    }

    public static String getmyWebIp(String strUrl) {
        try {
            URL url = new URL(strUrl);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            String s = "";
            StringBuffer sb = new StringBuffer("");
            String webContent = "";
            while ((s = br.readLine()) != null) {
                sb.append(s);
            }
            br.close();
            return sb.toString();
        } catch (Exception e) {
        }
        return "<fail>";
    }
}