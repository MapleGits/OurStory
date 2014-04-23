/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package constants;

import clientside.MapleCharacter;

/**
 *
 * @author Itzik
 */
public class MultiMirror {

    public static final int version = ServerConstants.MAPLE_VERSION; //Change as you wish

    public static enum DimensionalMirror { //Updated to V120 without quests
        //TODO: Order by levels

        PQ0(0, "Ariant Coliseum", 682020000, 3, 20, 30, 0, 0),
        PQ1(1, "Mu Lung Dojo", 682020000, 4, version < 101 ? 25 : 90, 200, 0, 0),
        //PQ2(2, "Spiegelmann's Gonzo Gallery", 0, 50, 200, 0, 0),
        PQ4(4, "Sea of Fog", 0, 0, 120, 159, 0, 0),
        PQ5(5, "Nett's Pyramid", 0, 0, 60, 109, 0, 0),
        PQ6(6, "Kerning Subway", 0, 0, 25, 30, 0, 0),
        //PQ8(8, "Golden Temple", 0, 110, 200, 0, 0),
        PQ9(9, "Moon Bunny", 0, 0, version < 93 ? 10 : version < 101 ? 20 : 50, 200, 0, 0),
        PQ10(10, "First Time Together", 0, 0, version < 101 ? 20 : 50, 200, 0, 0),
        PQ11(11, "Dimensional Schism", 0, 0, version < 93 ? 35 : version < 101 ? 20 : 50, version < 93 ? 50 : 200, 0, 0),
        PQ12(12, "Forest of Poison Haze", 0, 0, 70, 200, 0, 0),
        PQ13(13, "Remnant of the Goddess", 0, 0, version < 101 ? 50 : 70, 200, 0, 0),
        PQ14(14, "Lord Pirate", 0, 0, 70, 200, 0, 0),
        PQ15(15, "Romeo and Juliet", 0, 0, 70, 200, 0, 0),
        PQ16(16, "Resurrection of the Hoblin King", 0, 0, version < 101 ? 80 : 120, 200, 0, 0),
        PQ17(17, "Dragon's Nest", 0, 0, 120, 200, 0, 0),
        PQ21(21, "Kenta in Danger", 0, 0, 120, 200, 0, 0),
        PQ22(22, "Escape", 0, 0, 120, 200, 0, 0),
        PQ23(23, "The Ice Knight's Curse", 0, 0, 20, 200, 0, 0),
        PQ27(27, "Fight for Azwan", 0, 0, 40, 200, 0, 0),
        //PQ30(30, "Battle Mode", 0, 0, 70, 200, 0, 0), //V117 Doesn't have it
        PQ88(88, "New Leaf City", 0, 0, 10, 200, 0, 0),
        //PQ89(89, "Haunted Mansion Secret Corridor", 0, 0, 10, 200, 0, 0); //V117 Doesn't have it
        PQ98(98, "Astaroth", 0, 0, 25, 40, 0, 0),
        //PQ99(99, "Dragon Nest", 0, 0, 10, 200, 0, 0),
        DEFAULT(Integer.MAX_VALUE, "Default Map", 682020000, 4, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0); //Dojo Map as default
        private int icon, map, portal, minLevel, maxLevel, requieredQuest, requieredQuestState;
        private String name, portal_string;

        DimensionalMirror(int icon, String name, int map, int portal, int minLevel, int maxLevel, int requieredQuest, int requieredQuestState) {
            this.icon = icon;
            this.name = name;
            this.map = map;
            this.portal = portal;
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
            this.requieredQuest = requieredQuest;
            this.requieredQuestState = requieredQuestState;
        }

        DimensionalMirror(int icon, String name, int map, String portal_string, int minLevel, int maxLevel, int requieredQuest, int requieredQuestState) {
            this.icon = icon;
            this.name = name;
            this.map = map;
            this.portal_string = portal_string;
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
            this.requieredQuest = requieredQuest;
            this.requieredQuestState = requieredQuestState;
        }

        public int getIcon() {
            return icon;
        }

        public String getName() {
            return name;
        }

        public int getMap() {
            return map;
        }

        public int getPortal() {
            return portal;
        }

        public String getPortalString() {
            return portal_string;
        }

        public int getMinLevel() {
            return minLevel;
        }

        public int getMaxLevel() {
            return maxLevel;
        }

        public int getRequieredQuest() {
            return requieredQuest;
        }

        public int getRequieredQuestState() {
            return requieredQuestState;
        }

        public static DimensionalMirror getByIcon(int icon) {
            for (DimensionalMirror mirror : DimensionalMirror.values()) {
                if (mirror.getIcon() == icon) {
                    return mirror;
                }
            }
            return null; //default
        }
    }

    public static String getDimensionalDoorSelection(MapleCharacter chr) {
        String mapselect = "";
        for (DimensionalMirror mirror : DimensionalMirror.values()) {
            if (chr.getLevel() >= mirror.getMinLevel() && chr.getLevel() <= mirror.getMaxLevel()) {
                if ((chr.getQuestStatus(mirror.getRequieredQuest()) == mirror.getRequieredQuestState()) || mirror.getRequieredQuest() == 0) {
                    if (mirror != DimensionalMirror.DEFAULT && mirror.getIcon() != DimensionalMirror.DEFAULT.getIcon() /*just another check*/) {
                        mapselect += "#" + mirror.getIcon() + "#" + mirror.getName();
                    }
                }
            }
        }
        if (mapselect == null || "".equals(mapselect)) {
            mapselect = "#-1# There are no locations you can move to.";
        }
        return mapselect;
    }

    public static int getDimensionalLocation(int icon) {
        return DimensionalMirror.getByIcon(icon) != null ? DimensionalMirror.getByIcon(icon).getMap() : DimensionalMirror.DEFAULT.getMap();
    }

    public static enum TimeGate { //Updated to V124 without quests
        //TODO: finish this(add quest ids and map ids)

        YEAR_2021(1, "Year 2021, Average Town", 0, 0, 0, 0),
        YEAR_2099(2, "Year 2099, Midnight Harbor", 0, 0, 0, 0),
        YEAR_2215(3, "Year 2215, Bombed City Center", 0, 0, 0, 0),
        YEAR_2216(4, "Year 2216, Ruined City", 0, 0, 0, 0),
        YEAR_2230(5, "Year 2230, Dangerous Tower", 0, 0, 0, 0),
        YEAR_2503(6, "Year 2503, Air Battleship Hermes", 0, 0, 0, 0);
        private int icon, map, portal, requieredQuest, requieredQuestState;
        private String name;

        TimeGate(int icon, String name, int map, int portal, int requieredQuest, int requieredQuestState) {
            this.icon = icon;
            this.name = name;
            this.map = map;
            this.requieredQuest = requieredQuest;
            this.requieredQuestState = requieredQuestState;
            this.portal = portal;
        }

        public int getIcon() {
            return icon;
        }

        public String getName() {
            return name;
        }

        public int getMap() {
            return map;
        }

        public int getPortal() {
            return portal;
        }

        public int getRequieredQuest() {
            return requieredQuest;
        }

        public int getRequieredQuestState() {
            return requieredQuestState;
        }

        public static TimeGate getByIcon(int icon) {
            for (TimeGate gate : TimeGate.values()) {
                if (gate.getIcon() == icon) {
                    return gate;
                }
            }
            return null; //default
        }
    }

    public static String getTimeGateSelection(MapleCharacter chr) {
        String mapselect = "";
        for (TimeGate gate : TimeGate.values()) {
            if ((chr.getQuestStatus(gate.getRequieredQuest()) == gate.getRequieredQuestState()) || gate.getRequieredQuest() == 0) {
                mapselect += "#" + gate.getIcon() + "#" + gate.getName();
            }
        }
        if (mapselect == null || "".equals(mapselect)) {
            mapselect = "#-1# There are no locations you can move to.";
        }
        return mapselect;
    }

    public static int getGateLocation(int icon) {
        return TimeGate.getByIcon(icon) != null ? TimeGate.getByIcon(icon).getMap() : TimeGate.YEAR_2099.getMap(); //Year 2099 as default
    }

    public static enum TownTeleport {

        TOWN_0(0, "Six Path Crossway", 0, 0),
        TOWN_1(1, "Henesys", 0, 0),
        TOWN_2(2, "Ellinia", 0, 0),
        TOWN_3(3, "Perion", 0, 0),
        TOWN_4(4, "Kerning City", 0, 0),
        TOWN_5(5, "Lith Harbor", 0, 0),
        TOWN_6(6, "Sleepywood", 0, 0),
        TOWN_7(7, "Nautilus", 0, 0),
        TOWN_8(8, "Ereve", 0, 0),
        TOWN_9(9, "Rien", 0, 0),
        TOWN_10(0, "Orbis", 0, 0),
        TOWN_11(1, "El Nath", 0, 0),
        TOWN_12(2, "Ludibrium", 0, 0),
        TOWN_13(3, "Omega Sector", 0, 0),
        TOWN_14(4, "Korean Folk Town", 0, 0),
        TOWN_15(5, "Aquarium", 0, 0),
        TOWN_16(6, "Leafre", 0, 0),
        TOWN_17(7, "Mu Lung", 0, 0),
        TOWN_18(8, "Herb Town", 0, 0),
        TOWN_19(9, "Ariant", 0, 0),
        TOWN_20(0, "Magatia", 0, 0),
        TOWN_21(1, "Edelstein", 0, 0),
        TOWN_22(2, "Elluel", 0, 0);
        private int icon, map, portal;
        private String name;

        TownTeleport(int icon, String name, int map, int portal) {
            this.icon = icon;
            this.name = name;
            this.map = map;
            this.portal = portal;
        }

        public int getIcon() {
            return icon;
        }

        public String getName() {
            return name;
        }

        public int getMap() {
            return map;
        }

        public int getPortal() {
            return portal;
        }

        public static TownTeleport getByIcon(int icon) {
            for (TownTeleport town : TownTeleport.values()) {
                if (town.getIcon() == icon) {
                    return town;
                }
            }
            return null; //default
        }
    }

    public static String getTownTeleportSelection() {
        String mapselect = "";
        for (TownTeleport gate : TownTeleport.values()) {
            mapselect += "#" + gate.getIcon() + "#" + gate.getName();
        }
        if (mapselect == null || "".equals(mapselect)) {
            mapselect = "#-1# There are no locations you can move to.";
        }
        return mapselect;
    }

    public static int getTownLocation(int icon) {
        return TownTeleport.getByIcon(icon) != null ? TownTeleport.getByIcon(icon).getMap() : TownTeleport.TOWN_0.getMap();
    }

    public static String getPantheonPortalSelection() {
        return getTownTeleportSelection();
    }

    public static String getOldMapleDimensionalDoorSelection() {
        return "#87# Crack in the Dimensional Mirror";
    }
}