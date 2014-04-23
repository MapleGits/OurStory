package clientside;

import database.DatabaseConnection;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import tools.Pair;
import tools.data.MaplePacketLittleEndianWriter;

public class MapleKeyLayout
        implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private boolean changed = false;
    private Map<Integer, Pair<Byte, Integer>> keymap;

    public MapleKeyLayout() {
        this.keymap = new HashMap();
    }

    public MapleKeyLayout(Map<Integer, Pair<Byte, Integer>> keys) {
        this.keymap = keys;
    }

    public final Map<Integer, Pair<Byte, Integer>> Layout() {
        this.changed = true;
        return this.keymap;
    }

    public final void unchanged() {
        this.changed = false;
    }

    public final void writeData(MaplePacketLittleEndianWriter mplew) {
        mplew.write(this.keymap.isEmpty() ? 1 : 0);
        if (this.keymap.isEmpty()) {
            return;
        }

        for (int x = 0; x < 89; x++) {
            Pair binding = (Pair) this.keymap.get(Integer.valueOf(x));
            if (binding != null) {
                mplew.write(((Byte) binding.getLeft()).byteValue());
                mplew.writeInt(((Integer) binding.getRight()).intValue());
            } else {
                mplew.write(0);
                mplew.writeInt(0);
            }
        }
    }

    public final void saveKeys(int charid) throws SQLException {
        if (!this.changed) {
            return;
        }
        Connection con = DatabaseConnection.getConnection();

        PreparedStatement ps = con.prepareStatement("DELETE FROM keymap WHERE characterid = ?");
        ps.setInt(1, charid);
        ps.execute();
        ps.close();
        if (this.keymap.isEmpty()) {
            return;
        }
        boolean first = true;
        StringBuilder query = new StringBuilder();

        for (Map.Entry keybinding : this.keymap.entrySet()) {
            if (first) {
                first = false;
                query.append("INSERT INTO keymap VALUES (");
            } else {
                query.append(",(");
            }
            query.append("DEFAULT,");
            query.append(charid).append(",");
            query.append(((Integer) keybinding.getKey()).intValue()).append(",");
            query.append(((Byte) ((Pair) keybinding.getValue()).getLeft()).byteValue()).append(",");
            query.append(((Integer) ((Pair) keybinding.getValue()).getRight()).intValue()).append(")");
        }
        ps = con.prepareStatement(query.toString());
        ps.execute();
        ps.close();
    }
}