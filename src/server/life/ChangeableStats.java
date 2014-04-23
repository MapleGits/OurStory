package server.life;

import constants.GameConstants;

public class ChangeableStats extends OverrideMonsterStats {

    public int watk;
    public int matk;
    public int acc;
    public int eva;
    public int PDRate;
    public int MDRate;
    public int pushed;
    public int level;

    public ChangeableStats(MapleMonsterStats stats, OverrideMonsterStats ostats) {
        this.hp = ostats.getHp();
        this.exp = ostats.getExp();
        this.mp = ostats.getMp();
        this.watk = stats.getPhysicalAttack();
        this.matk = stats.getMagicAttack();
        this.acc = stats.getAcc();
        this.eva = stats.getEva();
        this.PDRate = stats.getPDRate();
        this.MDRate = stats.getMDRate();
        this.pushed = stats.getPushed();
        this.level = stats.getLevel();
    }

    public ChangeableStats(MapleMonsterStats stats, int newLevel, boolean pqMob) {
        double mod = newLevel / stats.getLevel();
        double hpRatio = stats.getHp() / stats.getExp();
        double pqMod = pqMob ? 1.5D : 1.0D;
        this.hp = Math.round((!stats.isBoss() ? GameConstants.getMonsterHP(newLevel) : stats.getHp() * mod) * pqMod);
        this.exp = ((int) Math.round((!stats.isBoss() ? GameConstants.getMonsterHP(newLevel) / hpRatio : stats.getExp()) * pqMod));
        this.mp = ((int) Math.round(stats.getMp() * mod * pqMod));
        this.watk = ((int) Math.round(stats.getPhysicalAttack() * mod));
        this.matk = ((int) Math.round(stats.getMagicAttack() * mod));
        this.acc = Math.round(stats.getAcc() + Math.max(0, newLevel - stats.getLevel()) * 2);
        this.eva = Math.round(stats.getEva() + Math.max(0, newLevel - stats.getLevel()));
        this.PDRate = Math.min(stats.isBoss() ? 30 : 20, (int) Math.round(stats.getPDRate() * mod));
        this.MDRate = Math.min(stats.isBoss() ? 30 : 20, (int) Math.round(stats.getMDRate() * mod));
        this.pushed = ((int) Math.round(stats.getPushed() * mod));
        this.level = newLevel;
    }

    public ChangeableStats(MapleMonsterStats stats, double newLevel, int hpBuff, int bossHpBuff, double expMulti) { // Custom hell
        final double mod = newLevel / (double) stats.getLevel();
        final double hpRatio = (double) stats.getHp() / (double) stats.getExp();
        hp = (long) Math.round((!stats.isBoss() ? GameConstants.getMonsterHP(stats.getLevel()) * hpBuff : (stats.getHp() * bossHpBuff))); // right here lol
        exp = (int) Math.round(stats.getExp() * expMulti);
        mp = (int) Math.round(stats.getMp() * mod);
        watk = (int) Math.round(stats.getPhysicalAttack() * mod);
        matk = (int) Math.round(stats.getMagicAttack() * mod);
        acc = (int) Math.round(stats.getAcc() + Math.max(0, newLevel - stats.getLevel()) * 2);
        eva = (int) Math.round(stats.getEva() + Math.max(0, newLevel - stats.getLevel()));
        PDRate = Math.min(stats.isBoss() ? 30 : 20, (int) Math.round(stats.getPDRate() * mod));
        MDRate = Math.min(stats.isBoss() ? 30 : 20, (int) Math.round(stats.getMDRate() * mod));
        pushed = (int) Math.round(stats.getPushed() * mod);
        level = (int) newLevel;
    }
}