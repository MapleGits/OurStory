package server;

import clientside.CardData;
import constants.GameConstants;
import database.DatabaseConnection;
import java.io.File;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import tools.Pair;
import tools.Triple;

public class CharacterCardFactory {

    private static final CharacterCardFactory instance = new CharacterCardFactory();
    private final MapleDataProvider data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/Etc.wz"));
    private final Map<Integer, Integer> cardEffects = new HashMap();
    private final Map<Integer, List<Integer>> uniqueEffects = new HashMap();

    public static CharacterCardFactory getInstance() {
        return instance;
    }

    public void initialize() {
    }

    public final Triple<Integer, Integer, Integer> getCardSkill(int job, int level) {
        int skillid = ((Integer) this.cardEffects.get(Integer.valueOf(job / 10))).intValue();
        if (skillid <= 0) {
            return null;
        }
        return new Triple(Integer.valueOf(skillid - 71000000), Integer.valueOf(skillid), Integer.valueOf(GameConstants.getSkillLevel(level)));
    }

    public final List<Integer> getUniqueSkills(List<Integer> special) {
        List uis = new LinkedList();
        for (Map.Entry m : this.uniqueEffects.entrySet()) {
            if ((((List) m.getValue()).contains(special.get(0))) && (((List) m.getValue()).contains(special.get(1))) && (((List) m.getValue()).contains(special.get(2)))) {
                uis.add(m.getKey());
            }
        }
        return uis;
    }

    public final int getRankSkill(int level) {
        return GameConstants.getSkillLevel(level) + 71001099;
    }

    public final boolean canHaveCard(int level, int job) {
        if (level < 30) {
            return false;
        }
        if (this.cardEffects.get(Integer.valueOf(job / 10)) == null) {
            return false;
        }
        return true;
    }

    public final Map<Integer, CardData> loadCharacterCards(int accId, int serverId) {
        Map cards = new LinkedHashMap();
        Map inf = loadCharactersInfo(accId, serverId);
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM `character_cards` WHERE `accid` = ?");
            ps.setInt(1, accId);
            ResultSet rs = ps.executeQuery();
            int deck1 = 0;
            int deck2 = 3;
            while (rs.next()) {
                int cid = rs.getInt("characterid");
                Pair x = (Pair) inf.get(Integer.valueOf(cid));
                if ((x != null)) {
                    int position = rs.getInt("position");
                    if (position < 4) {
                        deck1++;
                        cards.put(Integer.valueOf(deck1), new CardData(cid, ((Short) x.getLeft()).shortValue(), ((Short) x.getRight()).shortValue()));
                    } else {
                        deck2++;
                        cards.put(Integer.valueOf(deck2), new CardData(cid, ((Short) x.getLeft()).shortValue(), ((Short) x.getRight()).shortValue()));
                    }
                }
            }
            rs.close();
            ps.close();
        } catch (SQLException sqlE) {
            System.out.println("Failed to load character cards. Reason: " + sqlE.toString());
        }
        for (int i = 1; i <= 9; i++) {
            if (cards.get(Integer.valueOf(i)) == null) {
                cards.put(Integer.valueOf(i), new CardData(0, (short) 0, (short) 0));
            }
        }
        return cards;
    }

    public Map<Integer, Pair<Short, Short>> loadCharactersInfo(int accId, int serverId) {
        Map chars = new HashMap();
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT id, level, job FROM characters WHERE accountid = ? AND world = ?");
            ps.setInt(1, accId);
            ps.setInt(2, serverId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                chars.put(Integer.valueOf(rs.getInt("id")), new Pair(Short.valueOf(rs.getShort("level")), Short.valueOf(rs.getShort("job"))));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.err.println("error loading characters info. reason: " + e.toString());
        }
        return chars;
    }
}