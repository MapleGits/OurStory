package server;

import constants.GameConstants;
import database.DatabaseConnection;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class ServerProperties {

    private static final Properties props = new Properties();

    public static void loadProperties(String s) {
        try {
            FileReader fr = new FileReader(s);
            props.load(fr);
            fr.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getProperty(String s) {
        return props.getProperty(s);
    }

    public static void setProperty(String prop, String newInf) {
        props.setProperty(prop, newInf);
    }

    public static String getProperty(String s, String def) {
        return props.getProperty(s, def);
    }

    static {
        String toLoad = "channel.properties";
        loadProperties(toLoad);
        if (getProperty("GMS") != null) {
            GameConstants.GMS = Boolean.parseBoolean(getProperty("GMS"));
        }
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM auth_server_channel_ip");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                props.put(rs.getString("name") + rs.getInt("channelid"), rs.getString("value"));
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.exit(0);
        }
        toLoad = GameConstants.GMS ? "worldGMS.properties" : "world.properties";
        loadProperties(toLoad);
    }
}