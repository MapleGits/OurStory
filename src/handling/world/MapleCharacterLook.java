package handling.world;

import java.util.Map;

public abstract interface MapleCharacterLook {

    public abstract byte getGender();

    public abstract byte getSkinColor();

    public abstract int getFace();

    public abstract int getHair();

    public abstract int getDemonMarking();

    public abstract short getJob();

    public abstract Map<Byte, Integer> getEquips();
}