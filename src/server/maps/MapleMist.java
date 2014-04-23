package server.maps;

import clientside.MapleCharacter;
import clientside.MapleClient;
import clientside.Skill;
import clientside.SkillFactory;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.concurrent.ScheduledFuture;
import server.MapleStatEffect;
import server.life.MapleMonster;
import server.life.MobSkill;
import tools.packet.CField;

public class MapleMist extends MapleMapObject {

    private Rectangle mistPosition;
    private MapleStatEffect source;
    private MobSkill skill;
    private boolean isMobMist;
    private int skillDelay;
    private int skilllevel;
    private int isPoisonMist;
    private int ownerId;
    private ScheduledFuture<?> schedule = null;
    private ScheduledFuture<?> poisonSchedule = null;

    public MapleMist(Rectangle mistPosition, MapleMonster mob, MobSkill skill) {
        this.mistPosition = mistPosition;
        this.ownerId = mob.getId();
        this.skill = skill;
        this.skilllevel = skill.getSkillLevel();

        this.isMobMist = true;
        this.isPoisonMist = 0;
        this.skillDelay = 0;
    }

    public MapleMist(Rectangle mistPosition, MapleCharacter owner, MapleStatEffect source) {
        this.mistPosition = mistPosition;
        this.ownerId = owner.getId();
        this.source = source;
        this.skillDelay = 8;
        this.isMobMist = false;
        this.skilllevel = owner.getTotalSkillLevel(SkillFactory.getSkill(source.getSourceId()));

        switch (source.getSourceId()) {
            case 4221006:
            case 32121006:
            case 4121015: // 
            case 42111004:
            case 42121005:
                this.isPoisonMist = 0;
                break;
            case 1076:
            case 11076:
            case 2111003:
            case 12111005:
            case 14111006:
                this.isPoisonMist = 1;
                break;
            case 22161003:
                this.isPoisonMist = 4;
        }
    }

    public MapleMist(Rectangle mistPosition, MapleCharacter owner) {
        this.mistPosition = mistPosition;
        this.ownerId = owner.getId();
        this.source = new MapleStatEffect();
        this.source.setSourceId(2111003);
        this.skilllevel = 30;
        this.isMobMist = false;
        this.isPoisonMist = 0;
        this.skillDelay = 8;
    }

    public MapleMapObjectType getType() {
        return MapleMapObjectType.MIST;
    }

    public Point getPosition() {
        return this.mistPosition.getLocation();
    }

    public Skill getSourceSkill() {
        return SkillFactory.getSkill(this.source.getSourceId());
    }

    public void setSchedule(ScheduledFuture<?> s) {
        this.schedule = s;
    }

    public ScheduledFuture<?> getSchedule() {
        return this.schedule;
    }

    public void setPoisonSchedule(ScheduledFuture<?> s) {
        this.poisonSchedule = s;
    }

    public ScheduledFuture<?> getPoisonSchedule() {
        return this.poisonSchedule;
    }

    public boolean isMobMist() {
        return this.isMobMist;
    }

    public int isPoisonMist() {
        return this.isPoisonMist;
    }

    public int getSkillDelay() {
        return this.skillDelay;
    }

    public int getSkillLevel() {
        return this.skilllevel;
    }

    public int getOwnerId() {
        return this.ownerId;
    }

    public MobSkill getMobSkill() {
        return this.skill;
    }

    public Rectangle getBox() {
        return this.mistPosition;
    }

    public MapleStatEffect getSource() {
        return this.source;
    }

    public void setPosition(Point position) {
    }

    public byte[] fakeSpawnData(int level) {
        return CField.spawnMist(this);
    }

    public void sendSpawnData(MapleClient c) {
        c.getSession().write(CField.spawnMist(this));
    }

    public void sendDestroyData(MapleClient c) {
        c.getSession().write(CField.removeMist(getObjectId(), false));
    }

    public boolean makeChanceResult() {
        return this.source.makeChanceResult();
    }
}