package server;

import database.DatabaseConnection;
import handling.world.exped.ExpeditionType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import tools.Pair;
import tools.StringUtil;
import tools.Triple;

public class SpeedRunner {

    private static final Map<ExpeditionType, Triple<String, Map<Integer, String>, Long>> speedRunData = new EnumMap(ExpeditionType.class);

    public static final Triple<String, Map<Integer, String>, Long> getSpeedRunData(ExpeditionType type) {
        return (Triple) speedRunData.get(type);
    }

    public static final void addSpeedRunData(ExpeditionType type, Pair<StringBuilder, Map<Integer, String>> mib, long tmp) {
        speedRunData.put(type, new Triple(((StringBuilder) mib.getLeft()).toString(), mib.getRight(), Long.valueOf(tmp)));
    }

    public static final void removeSpeedRunData(ExpeditionType type) {
        speedRunData.remove(type);
    }

    public static final void loadSpeedRuns() {
        if (speedRunData.size() > 0) {
            return;
        }
        for (ExpeditionType type : ExpeditionType.values()) {
            loadSpeedRunData(type);
        }
    }

    public static final String getPreamble(ExpeditionType type) {
        return new StringBuilder().append("#rThese are the speedrun times for ").append(StringUtil.makeEnumHumanReadable(type.name()).toUpperCase()).append(".#k\r\n\r\n").toString();
    }

    public static final void loadSpeedRunData(ExpeditionType type) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM speedruns WHERE type = ? ORDER BY time LIMIT 25");
            ps.setString(1, type.name());
            StringBuilder ret = new StringBuilder(getPreamble(type));
            Map rett = new LinkedHashMap();
            ResultSet rs = ps.executeQuery();
            int rank = 1;
            Set leaders = new HashSet();
            boolean cont = rs.first();
            boolean changed = cont;
            long tmp = 0L;
            while (cont) {
                if (!leaders.contains(rs.getString("leader"))) {
                    addSpeedRunData(ret, rett, rs.getString("members"), rs.getString("leader"), rank, rs.getString("timestring"));
                    rank++;
                    leaders.add(rs.getString("leader"));
                    tmp = rs.getLong("time");
                }
                cont = (rs.next()) && (rank < 25);
            }
            rs.close();
            ps.close();
            if (changed) {
                speedRunData.put(type, new Triple(ret.toString(), rett, Long.valueOf(tmp)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static final Pair<StringBuilder, Map<Integer, String>> addSpeedRunData(StringBuilder ret, Map<Integer, String> rett, String members, String leader, int rank, String timestring) {
        StringBuilder rettt = new StringBuilder();

        String[] membrz = members.split(",");
        rettt.append(new StringBuilder().append("#bThese are the squad members of ").append(leader).append("'s squad at rank ").append(rank).append(".#k\r\n\r\n").toString());
        for (int i = 0; i < membrz.length; i++) {
            rettt.append("#r#e");
            rettt.append(i + 1);
            rettt.append(".#n ");
            rettt.append(membrz[i]);
            rettt.append("#k\r\n");
        }
        rett.put(Integer.valueOf(rank), rettt.toString());
        ret.append("#b#L").append(rank).append("#Rank #e").append(rank).append("#n#k : ").append(leader).append(", in ").append(timestring);
        if (membrz.length > 1) {
            ret.append("#l");
        }
        ret.append("\r\n");
        return new Pair(ret, rett);
    }
}