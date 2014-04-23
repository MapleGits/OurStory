package handling.login.handler;

import clientside.LoginCrypto;
import database.DatabaseConnection;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AutoRegister {

    private static final int ACCOUNTS_PER_IP = 2;
    public static final boolean autoRegister = true;
    public static boolean success = false;

    public static boolean getAccountExists(String login) {
        boolean accountExists = false;
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT name FROM accounts WHERE name = ?");
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if (rs.first()) {
                accountExists = true;
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return accountExists;
    }

    public static void createAccount(String login, String pwd, String eip) {
        String sockAddr = eip;
        Connection con;
        try {
            con = DatabaseConnection.getConnection();
        } catch (Exception ex) {
            System.out.println(ex);
            return;
        }
        try {
            PreparedStatement ipc = con.prepareStatement("SELECT SessionIP FROM accounts WHERE SessionIP = ?");
            Throwable localThrowable3 = null;
            ResultSet rs;
            try {
                ipc.setString(1, sockAddr.substring(1, sockAddr.lastIndexOf(':')));
                rs = ipc.executeQuery();
                if ((!rs.first()) || ((rs.last() == true) && (rs.getRow() < 2))) {
                    try {
                        PreparedStatement ps = con.prepareStatement("INSERT INTO accounts (name, password, email, birthday, macs, SessionIP) VALUES (?, ?, ?, ?, ?, ?)");
                        Throwable localThrowable4 = null;
                        try {
                            ps.setString(1, login);
                            ps.setString(2, LoginCrypto.hexSha1(pwd));
                            ps.setString(3, "no@email.provided");
                            ps.setString(4, "2008-04-07");
                            ps.setString(5, "00-00-00-00-00-00");

                            ps.setString(6, sockAddr.substring(1, sockAddr.lastIndexOf(':')));
                            ps.executeUpdate();
                        } catch (Throwable localThrowable1) {
                            localThrowable4 = localThrowable1;
                            throw localThrowable1;
                        } finally {
                        }

                        success = true;
                    } catch (SQLException ex) {
                        System.out.println(ex);
                        return;
                    }
                }
            } catch (Throwable localThrowable2) {
                localThrowable3 = localThrowable2;
                throw localThrowable2;
            } finally {
                if (ipc != null) {
                    if (localThrowable3 != null) {
                        try {
                            ipc.close();
                        } catch (Throwable x2) {
                            localThrowable3.addSuppressed(x2);
                        }
                    } else {
                        ipc.close();
                    }
                }
            }
            rs.close();
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }
}