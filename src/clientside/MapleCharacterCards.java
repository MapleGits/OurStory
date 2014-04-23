package clientside;

import constants.GameConstants;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import server.CharacterCardFactory;
import tools.Pair;
import tools.Triple;
import tools.data.MaplePacketLittleEndianWriter;

public class MapleCharacterCards {

    private Map<Integer, CardData> cards = new LinkedHashMap();
    private List<Pair<Integer, Integer>> skills = new LinkedList();

    public final Map<Integer, CardData> getCards() {
        return this.cards;
    }

    public final void setCards(Map<Integer, CardData> cads) {
        this.cards = cads;
    }

    public final List<Pair<Integer, Integer>> getCardEffects() {
        return this.skills;
    }

    public final void calculateEffects() {
        this.skills.clear();
        int deck1amount = 0;
        int deck2amount = 0;
        int lowD1 = 0;
        int lowD2 = 0;
        List cardids1 = new LinkedList();
        List cardids2 = new LinkedList();
        for (Map.Entry x : this.cards.entrySet()) {
            if (((CardData) x.getValue()).cid > 0) {
                Triple skillData = CharacterCardFactory.getInstance().getCardSkill(((CardData) x.getValue()).job, ((CardData) x.getValue()).level);
                if (((Integer) x.getKey()).intValue() < 4) {
                    if (skillData != null) {
                        cardids1.add(skillData.getLeft());
                        this.skills.add(new Pair(skillData.getMid(), skillData.getRight()));
                    }
                    deck1amount++;
                    if ((lowD1 == 0) || (lowD1 > ((CardData) x.getValue()).level)) {
                        lowD1 = ((CardData) x.getValue()).level;
                    }
                } else {
                    if (skillData != null) {
                        cardids2.add(skillData.getLeft());
                        this.skills.add(new Pair(skillData.getMid(), skillData.getRight()));
                    }
                    deck2amount++;
                    if ((lowD2 == 0) || (lowD2 > ((CardData) x.getValue()).level)) {
                        lowD2 = ((CardData) x.getValue()).level;
                    }
                }
            }
        }
        if ((deck1amount == 3) && (cardids1.size() == 3)) {
            List<Integer> uid = CharacterCardFactory.getInstance().getUniqueSkills(cardids1);
            for (Integer ii : uid) {
                this.skills.add(new Pair(ii, Integer.valueOf(GameConstants.getSkillLevel(lowD1))));
            }
            this.skills.add(new Pair(Integer.valueOf(CharacterCardFactory.getInstance().getRankSkill(lowD1)), Integer.valueOf(1)));
        }
        if ((deck2amount == 3) && (cardids2.size() == 3)) {
            List<Integer> uid = CharacterCardFactory.getInstance().getUniqueSkills(cardids2);
            for (Integer ii : uid) {
                this.skills.add(new Pair(ii, Integer.valueOf(GameConstants.getSkillLevel(lowD2))));
            }
            this.skills.add(new Pair(Integer.valueOf(CharacterCardFactory.getInstance().getRankSkill(lowD2)), Integer.valueOf(1)));
        }
    }

    public final void recalcLocalStats(MapleCharacter chr) {
        int pos = -1;
        for (Map.Entry x : this.cards.entrySet()) {
            if (((CardData) x.getValue()).cid == chr.getId()) {
                pos = ((Integer) x.getKey()).intValue();
                break;
            }
        }
        if (pos != -1) {
            if (!CharacterCardFactory.getInstance().canHaveCard(chr.getLevel(), chr.getJob())) {
                this.cards.remove(Integer.valueOf(pos));
            } else {
                this.cards.put(Integer.valueOf(pos), new CardData(chr.getId(), chr.getLevel(), chr.getJob()));
            }
        }
        //  calculateEffects();
    }

    public final void loadCards(MapleClient c, boolean channelserver) throws SQLException {
        this.cards = CharacterCardFactory.getInstance().loadCharacterCards(c.getAccID(), c.getWorld());
        if (channelserver) {
            //  calculateEffects();
        }
    }

    public final void connectData(MaplePacketLittleEndianWriter mplew) {
        if (this.cards.isEmpty()) {
            mplew.writeZeroBytes(81);
            return;
        }
        int poss = 0;
        for (CardData i : this.cards.values()) {
            poss++;
            if (poss > 9) {
                break;
            }
            mplew.writeInt(i.cid);
            mplew.write(i.level);
            mplew.writeInt(i.job);
        }
    }
}