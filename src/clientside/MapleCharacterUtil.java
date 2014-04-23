package clientside;

import constants.GameConstants;
import database.DatabaseConnection;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tools.Triple;

public class MapleCharacterUtil {

    private static final Pattern namePattern = Pattern.compile("[a-zA-Z0-9]{4,12}");
    private static final Pattern petPattern = Pattern.compile("[a-zA-Z0-9]{4,12}");

    public static final boolean canCreateChar(String name, boolean gm) {
        if ((getIdByName(name) != -1) || (!isEligibleCharName(name, gm))) {
            return false;
        }
        return true;
    }

    public static final boolean isEligibleCharName(String name, boolean gm) {
        if (name.length() > 12) {
            return false;
        }
        if (gm) {
            return true;
        }
        if ((name.length() < 3) || (!namePattern.matcher(name).matches())) {
            return false;
        }
        for (String z : GameConstants.RESERVED) {
            if (name.indexOf(z) != -1) {
                return false;
            }
        }
        return true;
    }

    public static final boolean canChangePetName(String name) {
        if (petPattern.matcher(name).matches()) {
            for (String z : GameConstants.RESERVED) {
                if (name.indexOf(z) != -1) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static final String makeMapleReadable(String in) {
        String wui = in.replace('I', 'i');
        wui = wui.replace('l', 'L');
        wui = wui.replace("rn", "Rn");
        wui = wui.replace("vv", "Vv");
        wui = wui.replace("VV", "Vv");
        return wui;
    }

    public static final int getIdByName(String name) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT id FROM characters WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            int id = rs.getInt("id");
            rs.close();
            ps.close();

            return id;
        } catch (SQLException e) {
            System.err.println("error 'getIdByName' " + e);
        }
        return -1;
    }

    public static final int Change_SecondPassword(int accid, String password, String newpassword) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * from accounts where id = ?");
            ps.setInt(1, accid);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            String secondPassword = rs.getString("2ndpassword");
            String salt2 = rs.getString("salt2");
            if ((secondPassword != null) && (salt2 != null)) {
                secondPassword = LoginCrypto.rand_r(secondPassword);
            } else if ((secondPassword == null) && (salt2 == null)) {
                rs.close();
                ps.close();
                return 0;
            }
            if (!check_ifPasswordEquals(secondPassword, password, salt2)) {
                rs.close();
                ps.close();
                return 1;
            }
            rs.close();
            ps.close();
            String SHA1hashedsecond;
            try {
                SHA1hashedsecond = LoginCryptoLegacy.encodeSHA1(newpassword);
            } catch (Exception e) {
                return -2;
            }
            ps = con.prepareStatement("UPDATE accounts set 2ndpassword = ?, salt2 = ? where id = ?");
            ps.setString(1, SHA1hashedsecond);
            ps.setString(2, null);
            ps.setInt(3, accid);

            if (!ps.execute()) {
                ps.close();
                return 2;
            }
            ps.close();
            return -2;
        } catch (SQLException e) {
            System.err.println("error 'getIdByName' " + e);
        }
        return -2;
    }

    private static final boolean check_ifPasswordEquals(String passhash, String pwd, String salt) {
        if ((LoginCryptoLegacy.isLegacyPassword(passhash)) && (LoginCryptoLegacy.checkPassword(pwd, passhash))) {
            return true;
        }
        if ((salt == null) && (LoginCrypto.checkSha1Hash(passhash, pwd))) {
            return true;
        }
        if (LoginCrypto.checkSaltedSha512Hash(passhash, pwd, salt)) {
            return true;
        }
        return false;
    }

    public static Triple<Integer, Integer, Integer> getInfoByName(String name, int world) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE name = ? AND world = ?");
            ps.setString(1, name);
            ps.setInt(2, world);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return null;
            }
            Triple id = new Triple(Integer.valueOf(rs.getInt("id")), Integer.valueOf(rs.getInt("accountid")), Integer.valueOf(rs.getInt("gender")));
            rs.close();
            ps.close();
            return id;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setNXCodeUsed(String name, String code) throws SQLException {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("UPDATE nxcode SET `user` = ?, `valid` = 0 WHERE code = ?");
        ps.setString(1, name);
        ps.setString(2, code);
        ps.execute();
        ps.close();
    }

    public static void sendNote(String to, String name, String msg, int fame) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO notes (`to`, `from`, `message`, `timestamp`, `gift`) VALUES (?, ?, ?, ?, ?)");
            ps.setString(1, to);
            ps.setString(2, name);
            ps.setString(3, msg);
            ps.setLong(4, System.currentTimeMillis());
            ps.setInt(5, fame);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            System.err.println("Unable to send note" + e);
        }
    }

    public static Triple<Boolean, Integer, Integer> getNXCodeInfo(String code) throws SQLException {
        Triple ret = null;
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT `valid`, `type`, `item` FROM nxcode WHERE code LIKE ?");
        ps.setString(1, code);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            ret = new Triple(Boolean.valueOf(rs.getInt("valid") > 0), Integer.valueOf(rs.getInt("type")), Integer.valueOf(rs.getInt("item")));
        }
        rs.close();
        ps.close();
        return ret;
    }
}