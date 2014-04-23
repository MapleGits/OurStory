package client.inventory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.Randomizer;

public class PetDataFactory {

    private static MapleDataProvider dataRoot = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/Item.wz"));
    private static Map<Integer, List<PetCommand>> petCommands = new HashMap();
    private static Map<Integer, Integer> petHunger = new HashMap();

    public static final PetCommand getRandomPetCommand(int petId) {
        if (getPetCommand(petId, 0) == null) {
            return null;
        }
        List gg = (List) petCommands.get(Integer.valueOf(petId));
        return (PetCommand) gg.get(Randomizer.nextInt(gg.size()));
    }

    public static final PetCommand getPetCommand(int petId, int skillId) {
        List gg = (List) petCommands.get(Integer.valueOf(petId));
        if (gg != null) {
            if ((gg.size() > skillId) && (gg.size() > 0)) {
                return (PetCommand) gg.get(skillId);
            }
            return null;
        }
        MapleData skillData = dataRoot.getData("Pet/" + petId + ".img");
        int theSkill = 0;
        gg = new ArrayList();
        while (skillData != null) {
            MapleData dd = skillData.getChildByPath("interact/" + theSkill);
            if (dd == null) {
                break;
            }
            PetCommand retr = new PetCommand(petId, skillId, MapleDataTool.getInt("prob", dd, 0), MapleDataTool.getInt("inc", dd, 0));
            gg.add(retr);
            theSkill++;
        }
        petCommands.put(Integer.valueOf(petId), gg);
        if ((gg.size() <= skillId) && (gg.size() > 0)) {
            return (PetCommand) gg.get(skillId);
        }
        return null;
    }

    public static final int getHunger(int petId) {
        Integer ret = (Integer) petHunger.get(Integer.valueOf(petId));
        if (ret != null) {
            return ret.intValue();
        }
        MapleData hungerData = dataRoot.getData("Pet/" + petId + ".img").getChildByPath("info/hungry");
        ret = Integer.valueOf(MapleDataTool.getInt(hungerData, 1));
        petHunger.put(Integer.valueOf(petId), ret);

        return ret.intValue();
    }
}