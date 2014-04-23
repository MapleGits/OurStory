package handling.world.exped;

public enum PartySearchType {

    Kerning(20, 250, 1000, false),
    Ludi(30, 250, 1001, false),
    Orbis(50, 250, 1002, false),
    Pirate(60, 250, 1003, false),
    Magatia(70, 250, 1004, false),
    ElinForest(40, 250, 1005, false),
    Pyramid(40, 250, 1008, false),
    Dragonica(100, 250, 1009, false),
    Hoblin(80, 250, 1011, false),
    Henesys(10, 250, 1012, false),
    Dojo(25, 250, 1013, false),
    Balrog_Normal(50, 250, 2001, true),
    Zakum(50, 250, 2002, true),
    Horntail(80, 250, 2003, true),
    PinkBean(140, 250, 2004, true),
    ChaosZakum(100, 250, 2005, true),
    ChaosHT(110, 250, 2006, true),
    CWKPQ(90, 250, 2007, true),
    VonLeon(120, 250, 2008, true),
    Hilla(120, 250, 2009, true);
    public int id;
    public int minLevel;
    public int maxLevel;
    public int timeLimit;
    public boolean exped;

    private PartySearchType(int minLevel, int maxLevel, int value, boolean exped) {
        this.id = value;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.exped = exped;
        this.timeLimit = (exped ? 20 : 5);
    }

    public static PartySearchType getById(int id) {
        for (PartySearchType pst : values()) {
            if (pst.id == id) {
                return pst;
            }
        }
        return null;
    }
}