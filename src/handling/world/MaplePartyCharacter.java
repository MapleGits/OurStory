package handling.world;

import clientside.MapleCharacter;
import clientside.MapleClient;
import database.DatabaseConnection;
import java.awt.Point;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import server.maps.MapleDoor;
import server.maps.MapleMap;

public class MaplePartyCharacter
        implements Serializable {

    private static final long serialVersionUID = 6215463252132450750L;
    private String name;
    private int id;
    private int level;
    private int channel;
    private int jobid;
    private int mapid;
    private int doorTown = 999999999;
    private int doorTarget = 999999999;
    private int doorSkill = 0;
    private Point doorPosition = new Point(0, 0);
    private boolean online;

    public MaplePartyCharacter(MapleCharacter maplechar) {
        this.name = maplechar.getName();
        this.level = maplechar.getLevel();
        this.channel = maplechar.getClient().getChannel();
        this.id = maplechar.getId();
        this.jobid = maplechar.getJob();
        this.mapid = maplechar.getMapId();
        this.online = true;

        List doors = maplechar.getDoors();
        if (doors.size() > 0) {
            MapleDoor door = (MapleDoor) doors.get(0);

            this.doorTown = door.getTown().getId();
            this.doorTarget = door.getTarget().getId();
            this.doorSkill = door.getSkill();
            this.doorPosition = door.getTargetPosition();
        } else {
            this.doorPosition = maplechar.getPosition();
        }
    }

    public MaplePartyCharacter() {
        this.name = "";
    }

    public int getLevel() {
        return this.level;
    }

    public int getChannel() {
        return this.channel;
    }

    public boolean isOnline() {
        return this.online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public int getMapid() {
        return this.mapid;
    }

    public String getName() {
        return this.name;
    }

    public int getId() {
        return this.id;
    }

    public int getJobId() {
        return this.jobid;
    }

    public int getDoorTown() {
        return this.doorTown;
    }

    public int getDoorTarget() {
        return this.doorTarget;
    }

    public int getDoorSkill() {
        return this.doorSkill;
    }

    public Point getDoorPosition() {
        return this.doorPosition;
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + (this.name == null ? 0 : this.name.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MaplePartyCharacter other = (MaplePartyCharacter) obj;
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    public int getBossLog(String boss) {
        Connection con = DatabaseConnection.getConnection();
        try {
            int count = 0;

            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM bosslog WHERE characterid = ? AND bossid = ? AND lastattempt >= subtime(CURRENT_TIMESTAMP, '1 0:0:0.0')");
            ps.setInt(1, this.id);
            ps.setString(2, boss);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            } else {
                count = -1;
            }
            rs.close();
            ps.close();
            return count;
        } catch (Exception Ex) {
        }
        return -1;
    }

    public void setBossLog(String boss) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("insert into bosslog (characterid, bossid) values (?,?)");
            ps.setInt(1, this.id);
            ps.setString(2, boss);
            ps.executeUpdate();
            ps.close();
        } catch (Exception Ex) {
        }
    }

    public int getfsbLog(String boss) {
        Connection con = DatabaseConnection.getConnection();
        try {
            int count = 0;
            getId();

            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM bosslog WHERE characterid = ? AND bossid = ?");
            ps.setInt(1, this.id);
            ps.setString(2, boss);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            } else {
                count = -1;
            }
            rs.close();
            ps.close();
            return count;
        } catch (Exception Ex) {
        }
        return -1;
    }

    public int getfsbLogs(String boss) {
        Connection con = DatabaseConnection.getConnection();
        try {
            int count = 0;

            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM bosslog WHERE bossid = ?");
            ps.setString(1, boss);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            } else {
                count = -1;
            }
            rs.close();
            ps.close();
            return count;
        } catch (Exception Ex) {
        }
        return -1;
    }

    public void delfsbLogs(String boss) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("delete from bosslog where bossid=?");
            ps.setString(1, boss);
            ps.executeUpdate();
            ps.close();
        } catch (Exception Ex) {
        }
    }

    public void delBossLog(String boss) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("delete from bosslog where characterid=? and bossid=?");
            ps.setInt(1, this.id);
            ps.setString(2, boss);
            ps.executeUpdate();
            ps.close();
        } catch (Exception Ex) {
        }
    }
}