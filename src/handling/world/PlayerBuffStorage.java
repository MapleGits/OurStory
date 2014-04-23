package handling.world;

import clientside.MapleCoolDownValueHolder;
import clientside.MapleDiseaseValueHolder;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerBuffStorage
        implements Serializable {

    private static final Map<Integer, List<PlayerBuffValueHolder>> buffs = new ConcurrentHashMap();
    private static final Map<Integer, List<MapleCoolDownValueHolder>> coolDowns = new ConcurrentHashMap();
    private static final Map<Integer, List<MapleDiseaseValueHolder>> diseases = new ConcurrentHashMap();

    public static final void addBuffsToStorage(int chrid, List<PlayerBuffValueHolder> toStore) {
        buffs.put(Integer.valueOf(chrid), toStore);
    }

    public static final void addCooldownsToStorage(int chrid, List<MapleCoolDownValueHolder> toStore) {
        coolDowns.put(Integer.valueOf(chrid), toStore);
    }

    public static final void addDiseaseToStorage(int chrid, List<MapleDiseaseValueHolder> toStore) {
        diseases.put(Integer.valueOf(chrid), toStore);
    }

    public static final List<PlayerBuffValueHolder> getBuffsFromStorage(int chrid) {
        return (List) buffs.remove(Integer.valueOf(chrid));
    }

    public static final List<MapleCoolDownValueHolder> getCooldownsFromStorage(int chrid) {
        return (List) coolDowns.remove(Integer.valueOf(chrid));
    }

    public static final List<MapleDiseaseValueHolder> getDiseaseFromStorage(int chrid) {
        return (List) diseases.remove(Integer.valueOf(chrid));
    }
}