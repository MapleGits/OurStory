package server.life;

import clientside.Skill;
import clientside.SkillFactory;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import java.awt.Point;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import server.MapleCarnivalFactory;
import server.MapleCarnivalFactory.MCSkill;
import server.MapleStatEffect;
import server.maps.MapleMap;
import server.maps.MapleReactor;
import server.maps.MapleSummon;
import tools.packet.CWvsContext;

public class SpawnPoint extends Spawns {

    private MapleMonsterStats monster;
    private Point pos;
    private long nextPossibleSpawn;
    private int mobTime;
    private int carnival = -1;
    private int fh;
    private int f;
    private int id;
    private int level = -1;
    private AtomicInteger spawnedMonsters = new AtomicInteger(0);
    private String msg;
    private byte carnivalTeam;

    public SpawnPoint(MapleMonster monster, Point pos, int mobTime, byte carnivalTeam, String msg) {
        this.monster = monster.getStats();
        this.pos = pos;
        this.id = monster.getId();
        this.fh = monster.getFh();
        this.f = monster.getF();
        this.mobTime = (mobTime < 0 ? -1 : mobTime * 1000);
        this.carnivalTeam = carnivalTeam;
        this.msg = msg;
        this.nextPossibleSpawn = System.currentTimeMillis();
    }

    public final void setCarnival(int c) {
        this.carnival = c;
    }

    public final void setLevel(int c) {
        this.level = c;
    }

    public final int getF() {
        return this.f;
    }

    public final int getFh() {
        return this.fh;
    }

    public final Point getPosition() {
        return this.pos;
    }

    public final MapleMonsterStats getMonster() {
        return this.monster;
    }

    public final byte getCarnivalTeam() {
        return this.carnivalTeam;
    }

    public final int getCarnivalId() {
        return this.carnival;
    }

    public final boolean shouldSpawn(long time) {
        if (this.mobTime < 0) {
            return false;
        }

        if (((this.mobTime == 0) && (this.monster.getMobile())) || ((this.spawnedMonsters.get() > 0) || (this.spawnedMonsters.get() > 1))) {
            return false;
        }
        return this.nextPossibleSpawn <= time;
    }

    public final MapleMonster spawnMonster(MapleMap map) {
        MapleMonster mob = new MapleMonster(this.id, this.monster);
        mob.setPosition(this.pos);
        mob.setCy(this.pos.y);
        mob.setRx0(this.pos.x - 50);
        mob.setRx1(this.pos.x + 50);
        mob.setFh(this.fh);
        mob.setF(this.f);
        mob.setCarnivalTeam(this.carnivalTeam);
        if (this.level > -1) {
            mob.changeLevel(this.level);
        }
        this.spawnedMonsters.incrementAndGet();
        mob.addListener(new MonsterListener() {
            @Override
            public void monsterKilled() {
                nextPossibleSpawn = System.currentTimeMillis();

                if (mobTime > 0) {
                    nextPossibleSpawn += mobTime;
                }
                spawnedMonsters.decrementAndGet();
            }
        });
        map.spawnMonster(mob, -2);
        if (this.carnivalTeam > -1) {
            for (MapleReactor r : map.getAllReactorsThreadsafe()) {
                if ((r.getName().startsWith(String.valueOf(this.carnivalTeam))) && (r.getReactorId() == 9980000 + this.carnivalTeam) && (r.getState() < 5)) {
                    int num = Integer.parseInt(r.getName().substring(1, 2));
                    MapleCarnivalFactory.MCSkill skil = MapleCarnivalFactory.getInstance().getGuardian(num);
                    if (skil != null) {
                        skil.getSkill().applyEffect(null, mob, false);
                    }
                }
            }
        }
        for (MapleSummon s : map.getAllSummonsThreadsafe()) {
            if (s.getSkill() == 35111005) {
                MapleStatEffect effect = SkillFactory.getSkill(s.getSkill()).getEffect(s.getSkillLevel());
                for (Map.Entry stat : effect.getMonsterStati().entrySet()) {
                    mob.applyStatus(s.getOwner(), new MonsterStatusEffect((MonsterStatus) stat.getKey(), (Integer) stat.getValue(), s.getSkill(), null, false), false, effect.getDuration(), true, effect);
                }
                break;
            }
        }
        if (this.msg != null) {
            map.broadcastMessage(CWvsContext.serverNotice(6, this.msg));
        }
        return mob;
    }

    public final int getMobTime() {
        return this.mobTime;
    }
}