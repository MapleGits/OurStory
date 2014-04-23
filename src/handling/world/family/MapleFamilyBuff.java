package handling.world.family;

import clientside.MapleBuffStat;
import clientside.MapleCharacter;
import java.util.EnumMap;
import java.util.concurrent.ScheduledFuture;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import server.Timer;
import tools.packet.CWvsContext;

public enum MapleFamilyBuff {

    Teleport("Family Reunion", "[Target] Me\n[Effect] Teleport directly to the Family member of your choice.", 0, 0, 0, 300, 190000),
    Summon("Summon Family", "[Target] 1 Family member\n[Effect] Summon a Family member of choice to the map you're in.", 1, 0, 0, 500, 190001),
    Drop_12_15("My Drop Rate 1.2x (15min)", "[Target] Me\n[Time] 15 min.\n[Effect] Monster drop rate will be increased #c1.2x#.\n*  If the event is in progress, this will be nullified.", 2, 15, 120, 700, 190002),
    EXP_12_15("My EXP Rate 1.2x (15min)", "[Target] Me\n[Time] 15 min.\n[Effect] Monster EXP rate will be increased #c1.2x#.\n*  If the event is in progress, this will be nullified.", 3, 15, 120, 800, 190003),
    Drop_12_30("My Drop Rate 1.2x (30min)", "[Target] Me\n[Time] 30 min.\n[Effect] Monster drop rate will be increased #c1.2x#.\n*  If the event is in progress, this will be nullified.", 2, 30, 120, 1000, 190004),
    EXP_12_30("My EXP Rate 1.2x (30min)", "[Target] Me\n[Time] 30 min.\n[Effect] Monster EXP rate will be increased #c1.2x#.\n*  If the event is in progress, this will be nullified.", 3, 30, 120, 1200, 190005),
    Drop_15_15("My Drop Rate 1.5x (15min)", "[Target] Me\n[Time] 15 min.\n[Effect] Monster drop rate will be increased #c1.5x#.\n*  If the event is in progress, this will be nullified.", 2, 15, 150, 1500, 190009),
    Drop_15_30("My Drop Rate 1.5x (30min)", "[Target] Me\n[Time] 30 min.\n[Effect] Monster drop rate will be increased #c1.5x#.\n*  If the event is in progress, this will be nullified.", 2, 30, 150, 2000, 190010),
    Bonding("Family Bonding (30min)", "[Target] At least 6 Family members online that are below me in the Pedigree\n[Time] 30 min.\n[Effect] Monster drop rate and EXP earned will be increased #c1.5x#. \n* If the EXP event is in progress, this will be nullified.", 4, 30, 150, 3000, 190006),
    Drop_Party_12("My Party Drop Rate 1.2x (30min)", "[Target] Party\n[Time] 30 min.\n[Effect] Monster drop rate will be increased #c1.2x#.\n*  If the event is in progress, this will be nullified.", 2, 30, 120, 4000, 190007),
    EXP_Party("My Party EXP Rate 1.2x (30min)", "[Target] Party\n[Time] 30 min.\n[Effect] Monster EXP rate will be increased #c1.2x#.\n*  If the event is in progress, this will be nullified.", 3, 30, 120, 5000, 190008),
    Drop_Party_15("My Party Drop Rate 1.5x (30min)", "[Target] Party\n[Time] 30 min.\n[Effect] Monster drop rate will be increased #c1.5x#.\n*  If the event is in progress, this will be nullified.", 2, 30, 150, 7000, 190011);
    public String name;
    public String desc;
    public int rep;
    public int type;
    public int questID;
    public int duration;
    public int effect;
    public EnumMap<MapleBuffStat, Integer> effects;

    private MapleFamilyBuff(String name, String desc, int type, int duration, int effect, int rep, int questID) {
        this.name = name;
        this.desc = desc;
        this.rep = rep;
        this.type = type;
        this.questID = questID;
        this.duration = duration;
        this.effect = effect;
        setEffects();
    }

    public int getEffectId() {
        switch (this.type) {
            case 2:
                return 2022694;
            case 3:
                return 2450018;
        }
        return 2022332;
    }

    public final void setEffects() {
        this.effects = new EnumMap(MapleBuffStat.class);
        switch (this.type) {
            case 2:
                this.effects.put(MapleBuffStat.DROP_RATE, Integer.valueOf(this.effect));
                this.effects.put(MapleBuffStat.MESO_RATE, Integer.valueOf(this.effect));
                break;
            case 3:
                this.effects.put(MapleBuffStat.EXPRATE, Integer.valueOf(this.effect));
                break;
            case 4:
                this.effects.put(MapleBuffStat.EXPRATE, Integer.valueOf(this.effect));
                this.effects.put(MapleBuffStat.DROP_RATE, Integer.valueOf(this.effect));
                this.effects.put(MapleBuffStat.MESO_RATE, Integer.valueOf(this.effect));
        }
    }

    public void applyTo(MapleCharacter chr) {
        chr.getClient().getSession().write(CWvsContext.BuffPacket.giveBuff(-getEffectId(), this.duration * 60000, this.effects, null));
        MapleStatEffect eff = MapleItemInformationProvider.getInstance().getItemEffect(getEffectId());
        chr.cancelEffect(eff, true, -1L, this.effects);
        long starttime = System.currentTimeMillis();
        MapleStatEffect.CancelEffectAction cancelAction = new MapleStatEffect.CancelEffectAction(chr, eff, starttime, this.effects);
        ScheduledFuture schedule = Timer.BuffTimer.getInstance().schedule(cancelAction, this.duration * 60000);
        chr.registerEffect(eff, starttime, schedule, this.effects, false, this.duration, chr.getId());
    }
}