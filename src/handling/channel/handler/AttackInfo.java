package handling.channel.handler;

import clientside.MapleCharacter;
import clientside.Skill;
import clientside.SkillFactory;
import constants.GameConstants;

import tools.AttackPair;
import java.awt.Point;
import java.util.List;
import server.MapleStatEffect;

public class AttackInfo {

    public int skill, charge, lastAttackTickCount;
    public List<AttackPair> allDamage;
    public Point position;
    public byte tbyte, display, animation, speed, csstar, AOE;
    public short hits, targets, slot;
    public int skillLevel; //test v172

    public final MapleStatEffect getAttackEffect(final MapleCharacter chr, int skillLevel, final Skill skill_) {
        if (skillLevel == 0) {
            return null;
        }
        if (GameConstants.isLinkedAranSkill(skill)) {
            final Skill skillLink = SkillFactory.getSkill(skill);
//	    if (display > 80) {
//		if (!skillLink.getAction()) {
//		    //AutobanManager.getInstance().autoban(chr.getClient(), "No delay hack, SkillID : " + skill);
//		    return null;
//		}
//	    }
            return skillLink.getEffect(skillLevel);
        }
//	if (display > 80) {
//	    if (!skill_.getAction()) {
//		AutobanManager.getInstance().autoban(chr.getClient(), "No delay hack, SkillID : " + skill);
//		return null;
//	    }
//	}
        return skill_.getEffect(skillLevel);
    }
}
