package server.life;

import clientside.MapleCharacter;
import clientside.MapleDisease;
import client.status.MonsterStatus;
import constants.GameConstants;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import scripting.EventInstanceManager;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleMist;

public class MobSkill {

    private int skillId;
    private int skillLevel;
    private int mpCon;
    private int spawnEffect;
    private int hp;
    private int x;
    private int y;
    private long duration;
    private long cooltime;
    private float prop;
    private short limit;
    private List<Integer> toSummon = new ArrayList();
    private Point lt;
    private Point rb;
    private boolean summonOnce;

    public MobSkill(int skillId, int level) {
        this.skillId = skillId;
        this.skillLevel = level;
    }

    public void setOnce(boolean o) {
        this.summonOnce = o;
    }

    public boolean onlyOnce() {
        return this.summonOnce;
    }

    public void setMpCon(int mpCon) {
        this.mpCon = mpCon;
    }

    public void addSummons(List<Integer> toSummon) {
        this.toSummon = toSummon;
    }

    public void setSpawnEffect(int spawnEffect) {
        this.spawnEffect = spawnEffect;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setCoolTime(long cooltime) {
        this.cooltime = cooltime;
    }

    public void setProp(float prop) {
        this.prop = prop;
    }

    public void setLtRb(Point lt, Point rb) {
        this.lt = lt;
        this.rb = rb;
    }

    public void setLimit(short limit) {
        this.limit = limit;
    }

    public boolean checkCurrentBuff(MapleCharacter player, MapleMonster monster) {
        boolean stop = false;
        switch (this.skillId) {
            case 100:
            case 110:
            case 150:
                stop = monster.isBuffed(MonsterStatus.WEAPON_ATTACK_UP);
                break;
            case 101:
            case 111:
            case 151:
                stop = monster.isBuffed(MonsterStatus.MAGIC_ATTACK_UP);
                break;
            case 102:
            case 112:
            case 152:
                stop = monster.isBuffed(MonsterStatus.WEAPON_DEFENSE_UP);
                break;
            case 103:
            case 113:
            case 153:
                stop = monster.isBuffed(MonsterStatus.MAGIC_DEFENSE_UP);
                break;
            case 140:
            case 141:
            case 142:
            case 143:
            case 144:
            case 145:
                stop = (monster.isBuffed(MonsterStatus.DAMAGE_IMMUNITY)) || (monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY)) || (monster.isBuffed(MonsterStatus.WEAPON_IMMUNITY));
                break;
            case 200:
                stop = player.getMap().getNumMonsters() >= this.limit;
        }

        stop |= monster.isBuffed(MonsterStatus.MAGIC_CRASH);
        return stop;
    }

    public void applyEffect(MapleCharacter player, MapleMonster monster, boolean skill) {
        MapleDisease disease = MapleDisease.getBySkill(this.skillId);
        Map stats = new EnumMap(MonsterStatus.class);
        List reflection = new LinkedList();

        switch (this.skillId) {
            case 100:
            case 110:
            case 150:
                stats.put(MonsterStatus.WEAPON_ATTACK_UP, Integer.valueOf(this.x));
                break;
            case 101:
            case 111:
            case 151:
                stats.put(MonsterStatus.MAGIC_ATTACK_UP, Integer.valueOf(this.x));
                break;
            case 102:
            case 112:
            case 152:
                stats.put(MonsterStatus.WEAPON_DEFENSE_UP, Integer.valueOf(this.x));
                break;
            case 103:
            case 113:
            case 153:
                stats.put(MonsterStatus.MAGIC_DEFENSE_UP, Integer.valueOf(this.x));
                break;
            case 154:
                stats.put(MonsterStatus.ACC, Integer.valueOf(this.x));
                break;
            case 155:
                stats.put(MonsterStatus.AVOID, Integer.valueOf(this.x));
                break;
            case 115:
            case 156:
                stats.put(MonsterStatus.SPEED, Integer.valueOf(this.x));
                break;
            case 157:
                stats.put(MonsterStatus.SEAL, Integer.valueOf(this.x));
                break;
            case 114:
                int hp;
                if ((this.lt != null) && (this.rb != null) && (skill) && (monster != null)) {
                    List<MapleMapObject> objects = getObjectsInRange(monster, MapleMapObjectType.MONSTER);
                    hp = getX() / 1000 * (int) (950.0D + 1050.0D * Math.random());
                    for (MapleMapObject mons : objects) {
                        ((MapleMonster) mons).heal(hp, getY(), true);
                    }
                } else if (monster != null) {
                    monster.heal(getX(), getY(), true);
                }
                break;
            case 105:
                if ((this.lt != null) && (this.rb != null) && (skill) && (monster != null)) {
                    List<MapleMapObject> objects = getObjectsInRange(monster, MapleMapObjectType.MONSTER);
                    for (MapleMapObject mons : objects) {
                        if (mons.getObjectId() != monster.getObjectId()) {
                            player.getMap().killMonster((MapleMonster) mons, player, true, false, (byte) 1, 0);
                            monster.heal(getX(), getY(), true);
                            break;
                        }
                    }
                } else if (monster != null) {
                    monster.heal(getX(), getY(), true);
                }
                break;
            case 127:
                if ((this.lt != null) && (this.rb != null) && (skill) && (monster != null) && (player != null)) {
                    for (MapleCharacter character : getPlayersInRange(monster, player)) {
                        character.dispel();
                    }
                } else if (player != null) {
                    player.dispel();
                }
                break;
            case 129:
                if ((monster != null) && (monster.getMap().getSquadByMap() == null) && ((monster.getEventInstance() == null) || (monster.getEventInstance().getName().indexOf("BossQuest") == -1))) {
                    BanishInfo info = monster.getStats().getBanishInfo();
                    if (info != null) {
                        if ((this.lt != null) && (this.rb != null) && (skill) && (player != null)) {
                            for (MapleCharacter chr : getPlayersInRange(monster, player)) {
                                if (!chr.hasBlockedInventory()) {
                                    chr.changeMapBanish(info.getMap(), info.getPortal(), info.getMsg());
                                }
                            }
                        } else if ((player != null) && (!player.hasBlockedInventory())) {
                            player.changeMapBanish(info.getMap(), info.getPortal(), info.getMsg());
                        }
                    }
                }
                break;
            case 131:
                if (monster != null) {
                    monster.getMap().spawnMist(new MapleMist(calculateBoundingBox(monster.getTruePosition(), true), monster, this), this.x * 10, false);
                }
                break;
            case 140:
                stats.put(MonsterStatus.WEAPON_IMMUNITY, Integer.valueOf(this.x));
                break;
            case 141:
                stats.put(MonsterStatus.MAGIC_IMMUNITY, Integer.valueOf(this.x));
                break;
            case 142:
                stats.put(MonsterStatus.DAMAGE_IMMUNITY, Integer.valueOf(this.x));
                break;
            case 143:
                stats.put(MonsterStatus.WEAPON_DAMAGE_REFLECT, Integer.valueOf(this.x));
                stats.put(MonsterStatus.WEAPON_IMMUNITY, Integer.valueOf(this.x));
                reflection.add(Integer.valueOf(this.x));
                break;
            case 144:
                stats.put(MonsterStatus.MAGIC_DAMAGE_REFLECT, Integer.valueOf(this.x));
                stats.put(MonsterStatus.MAGIC_IMMUNITY, Integer.valueOf(this.x));
                reflection.add(Integer.valueOf(this.x));
                break;
            case 145:
                stats.put(MonsterStatus.WEAPON_DAMAGE_REFLECT, Integer.valueOf(this.x));
                stats.put(MonsterStatus.WEAPON_IMMUNITY, Integer.valueOf(this.x));
                stats.put(MonsterStatus.MAGIC_DAMAGE_REFLECT, Integer.valueOf(this.x));
                stats.put(MonsterStatus.MAGIC_IMMUNITY, Integer.valueOf(this.x));
                reflection.add(Integer.valueOf(this.x));
                reflection.add(Integer.valueOf(this.x));
                break;
            case 200:
                if (monster == null) {
                    return;
                }
                for (Integer mobId : getSummons()) {
                    MapleMonster toSpawn = null;
                    try {
                        toSpawn = MapleLifeFactory.getMonster(GameConstants.getCustomSpawnID(monster.getId(), mobId.intValue()));
                    } catch (RuntimeException e) {
                        continue;
                    }

                    if (toSpawn != null) {
                        toSpawn.setPosition(monster.getTruePosition());
                        int ypos = (int) monster.getTruePosition().getY();
                        int xpos = (int) monster.getTruePosition().getX();

                        switch (mobId.intValue()) {
                            case 8500003:
                                toSpawn.setFh((int) Math.ceil(Math.random() * 19.0D));
                                ypos = -590;
                                break;
                            case 8500004:
                                xpos = (int) (monster.getTruePosition().getX() + Math.ceil(Math.random() * 1000.0D) - 500.0D);
                                ypos = (int) monster.getTruePosition().getY();
                                break;
                            case 8510100:
                                if (Math.ceil(Math.random() * 5.0D) == 1.0D) {
                                    ypos = 78;
                                    xpos = (int) (0.0D + Math.ceil(Math.random() * 5.0D)) + (Math.ceil(Math.random() * 2.0D) == 1.0D ? 180 : 0);
                                } else {
                                    xpos = (int) (monster.getTruePosition().getX() + Math.ceil(Math.random() * 1000.0D) - 500.0D);
                                }
                                break;
                            case 8820007:
                                break;
                            default:
                                switch (monster.getMap().getId()) {
                                    case 220080001:
                                        if (xpos < -890) {
                                            xpos = (int) (-890.0D + Math.ceil(Math.random() * 150.0D));
                                        } else if (xpos > 230) {
                                            xpos = (int) (230.0D - Math.ceil(Math.random() * 150.0D));
                                        }
                                        break;
                                    case 230040420:
                                        if (xpos < -239) {
                                            xpos = (int) (-239.0D + Math.ceil(Math.random() * 150.0D));
                                        } else if (xpos > 371) {
                                            xpos = (int) (371.0D - Math.ceil(Math.random() * 150.0D));
                                        }
                                        break;
                                }
                                monster.getMap().spawnMonsterWithEffect(toSpawn, getSpawnEffect(), monster.getMap().calcPointBelow(new Point(xpos, ypos - 1)));
                        }
                    }
                }
            case 104:
            case 106:
            case 107:
            case 108:
            case 109:
            case 116:
            case 117:
            case 118:
            case 119:
            case 120:
            case 121:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
            case 128:
            case 130:
            case 132:
            case 133:
            case 134:
            case 135:
            case 136:
            case 137:
            case 138:
            case 139:
            case 146:
            case 147:
            case 148:
            case 149:
            case 158:
            case 159:
            case 160:
            case 161:
            case 162:
            case 163:
            case 164:
            case 165:
            case 166:
            case 167:
            case 168:
            case 169:
            case 170:
            case 171:
            case 172:
            case 173:
            case 174:
            case 175:
            case 176:
            case 177:
            case 178:
            case 179:
            case 180:
            case 181:
            case 182:
            case 183:
            case 184:
            case 185:
            case 186:
            case 187:
            case 188:
            case 189:
            case 190:
            case 191:
            case 192:
            case 193:
            case 194:
            case 195:
            case 196:
            case 197:
            case 198:
            case 199:
        }
        if ((stats.size() > 0) && (monster != null)) {
            if ((this.lt != null) && (this.rb != null) && (skill)) {
                for (MapleMapObject mons : getObjectsInRange(monster, MapleMapObjectType.MONSTER)) {
                    ((MapleMonster) mons).applyMonsterBuff(stats, getSkillId(), getDuration(), this, reflection);
                }
            } else {
                monster.applyMonsterBuff(stats, getSkillId(), getDuration(), this, reflection);
            }
        }
        if ((disease != null) && (player != null)) {
            if ((this.lt != null) && (this.rb != null) && (skill) && (monster != null)) {
                for (MapleCharacter chr : getPlayersInRange(monster, player)) {
                    chr.giveDebuff(disease, this);
                }
            } else {
                player.giveDebuff(disease, this);
            }
        }
        if (monster != null) {
            monster.setMp(monster.getMp() - getMpCon());
        }
    }

    public int getSkillId() {
        return this.skillId;
    }

    public int getSkillLevel() {
        return this.skillLevel;
    }

    public int getMpCon() {
        return this.mpCon;
    }

    public List<Integer> getSummons() {
        return Collections.unmodifiableList(this.toSummon);
    }

    public int getSpawnEffect() {
        return this.spawnEffect;
    }

    public int getHP() {
        return this.hp;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public long getDuration() {
        return this.duration;
    }

    public long getCoolTime() {
        return this.cooltime;
    }

    public Point getLt() {
        return this.lt;
    }

    public Point getRb() {
        return this.rb;
    }

    public int getLimit() {
        return this.limit;
    }

    public boolean makeChanceResult() {
        return (this.prop >= 1.0D) || (Math.random() < this.prop);
    }

    private Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft) {
        Point myrb;
        Point mylt;
        if (facingLeft) {
            mylt = new Point(this.lt.x + posFrom.x, this.lt.y + posFrom.y);
            myrb = new Point(this.rb.x + posFrom.x, this.rb.y + posFrom.y);
        } else {
            myrb = new Point(this.lt.x * -1 + posFrom.x, this.rb.y + posFrom.y);
            mylt = new Point(this.rb.x * -1 + posFrom.x, this.lt.y + posFrom.y);
        }
        Rectangle bounds = new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
        return bounds;
    }

    private List<MapleCharacter> getPlayersInRange(MapleMonster monster, MapleCharacter player) {
        Rectangle bounds = calculateBoundingBox(monster.getTruePosition(), monster.isFacingLeft());
        List players = new ArrayList();
        players.add(player);
        return monster.getMap().getPlayersInRectAndInList(bounds, players);
    }

    private List<MapleMapObject> getObjectsInRange(MapleMonster monster, MapleMapObjectType objectType) {
        Rectangle bounds = calculateBoundingBox(monster.getTruePosition(), monster.isFacingLeft());
        List objectTypes = new ArrayList();
        objectTypes.add(objectType);
        return monster.getMap().getMapObjectsInRect(bounds, objectTypes);
    }
}