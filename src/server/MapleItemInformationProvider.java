package server;

import clientside.MapleCharacter;
import clientside.MapleTrait;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import database.DatabaseConnection;
import java.awt.Point;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import provider.MapleDataType;
import server.StructSetItem.SetItem;
import tools.Pair;
import tools.Triple;

public class MapleItemInformationProvider {

    private static final MapleItemInformationProvider instance = new MapleItemInformationProvider();
    protected final MapleDataProvider chrData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/Character.wz"));
    protected final MapleDataProvider etcData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/Etc.wz"));
    protected final MapleDataProvider itemData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/Item.wz"));
    protected final MapleDataProvider stringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/String.wz"));
    protected final Map<Integer, ItemInformation> dataCache = new HashMap();
    protected final Map<String, List<Triple<String, Point, Point>>> afterImage = new HashMap();
    protected final Map<Integer, List<StructItemOption>> potentialCache = new HashMap();
    protected final Map<Integer, Map<Integer, StructItemOption>> socketCache = new HashMap();
    protected final Map<Integer, MapleStatEffect> itemEffects = new HashMap();
    protected final Map<Integer, MapleStatEffect> itemEffectsEx = new HashMap();
    protected final Map<Integer, Integer> mobIds = new HashMap();
    protected final Map<Integer, Pair<Integer, Integer>> potLife = new HashMap();
    protected final Map<Integer, StructFamiliar> familiars = new HashMap();
    protected final Map<Integer, StructFamiliar> familiars_Item = new HashMap();
    protected final Map<Integer, StructFamiliar> familiars_Mob = new HashMap();
    protected final Map<Integer, Pair<List<Integer>, List<Integer>>> androids = new HashMap();
    protected final Map<Integer, Triple<Integer, List<Integer>, List<Integer>>> monsterBookSets = new HashMap();
    protected final Map<Integer, StructSetItem> setItems = new HashMap();
    private ItemInformation tmpInfo = null;

    public void runEtc() {
        if (!setItems.isEmpty() || !potentialCache.isEmpty() || !socketCache.isEmpty()) {
            return;
        }
        final MapleData setsData = etcData.getData("SetItemInfo.img");
        StructSetItem itemz;
        SetItem itez;
        for (MapleData dat : setsData) {
            itemz = new StructSetItem();
            itemz.setItemID = Integer.parseInt(dat.getName());
            itemz.completeCount = (byte) MapleDataTool.getIntConvert("completeCount", dat, 0);
            for (MapleData level : dat.getChildByPath("ItemID")) {
                if (level.getType() != MapleDataType.INT) {
                    for (MapleData leve : level) {
                        if (!leve.getName().equals("representName") && !leve.getName().equals("typeName")) {
                            itemz.itemIDs.add(MapleDataTool.getInt(leve));
                        }
                    }
                } else {
                    itemz.itemIDs.add(MapleDataTool.getInt(level));
                }
            }
            for (MapleData level : dat.getChildByPath("Effect")) {
                itez = new SetItem();
                itez.incPDD = MapleDataTool.getIntConvert("incPDD", level, 0);
                itez.incMDD = MapleDataTool.getIntConvert("incMDD", level, 0);
                itez.incSTR = MapleDataTool.getIntConvert("incSTR", level, 0);
                itez.incDEX = MapleDataTool.getIntConvert("incDEX", level, 0);
                itez.incINT = MapleDataTool.getIntConvert("incINT", level, 0);
                itez.incLUK = MapleDataTool.getIntConvert("incLUK", level, 0);
                itez.incACC = MapleDataTool.getIntConvert("incACC", level, 0);
                itez.incPAD = MapleDataTool.getIntConvert("incPAD", level, 0);
                itez.incMAD = MapleDataTool.getIntConvert("incMAD", level, 0);
                itez.incSpeed = MapleDataTool.getIntConvert("incSpeed", level, 0);
                itez.incMHP = MapleDataTool.getIntConvert("incMHP", level, 0);
                itez.incMMP = MapleDataTool.getIntConvert("incMMP", level, 0);
                itez.incMHPr = MapleDataTool.getIntConvert("incMHPr", level, 0);
                itez.incMMPr = MapleDataTool.getIntConvert("incMMPr", level, 0);
                itez.incAllStat = MapleDataTool.getIntConvert("incAllStat", level, 0);
                itez.option1 = MapleDataTool.getIntConvert("Option/1/option", level, 0);
                itez.option2 = MapleDataTool.getIntConvert("Option/2/option", level, 0);
                itez.option1Level = MapleDataTool.getIntConvert("Option/1/level", level, 0);
                itez.option2Level = MapleDataTool.getIntConvert("Option/2/level", level, 0);
                itemz.items.put(Integer.parseInt(level.getName()), itez);
            }
            setItems.put(itemz.setItemID, itemz);
        }
        StructItemOption item;
        final MapleData potsData = itemData.getData("ItemOption.img");
        List<StructItemOption> items;
        for (MapleData dat : potsData) {
            items = new LinkedList<>();
            for (MapleData potLevel : dat.getChildByPath("level")) {
                item = new StructItemOption();
                item.opID = Integer.parseInt(dat.getName());
                item.optionType = MapleDataTool.getIntConvert("info/optionType", dat, 0);
                item.reqLevel = MapleDataTool.getIntConvert("info/reqLevel", dat, 0);
                item.opString = MapleDataTool.getString("info/string", dat, "");
                for (final String i : StructItemOption.types) {
                    if (i.equals("face")) {
                        item.face = MapleDataTool.getString("face", potLevel, "");
                    } else {
                        final int level = MapleDataTool.getIntConvert(i, potLevel, 0);
                        if (level > 0) { // Save memory
                            item.data.put(i, level);
                        }
                    }
                }
                switch (item.opID) {
                    case 31001: // Haste
                    case 31002: // Mystic Door
                    case 31003: // Sharp Eyes
                    case 31004: // Hyper Body
                        item.data.put("skillID", (item.opID - 23001));
                        break;
                    case 41005: // Combat Orders
                    case 41006: // Advanced Blessing
                    case 41007: // Speed Infusion
                        item.data.put("skillID", (item.opID - 33001));
                        break;
                }
                items.add(item);
            }
            potentialCache.put(Integer.parseInt(dat.getName()), items);
        }
        final Map<Integer, StructItemOption> gradeS = new HashMap<>();
        final Map<Integer, StructItemOption> gradeA = new HashMap<>();
        final Map<Integer, StructItemOption> gradeB = new HashMap<>();
        final Map<Integer, StructItemOption> gradeC = new HashMap<>();
        final Map<Integer, StructItemOption> gradeD = new HashMap<>();
        final MapleData nebuliteData = itemData.getData("Install/0306.img");
        for (MapleData dat : nebuliteData) {
            item = new StructItemOption();
            item.opID = Integer.parseInt(dat.getName()); // Item Id
            item.optionType = MapleDataTool.getInt("optionType", dat.getChildByPath("socket"), 0);
            item.opString = MapleDataTool.getString("socket/string", dat, "");
            for (MapleData info : dat.getChildByPath("socket/option")) {
                final String optionString = MapleDataTool.getString("optionString", info, "");
                final int level = MapleDataTool.getInt("level", info, 0);
                if (level > 0) { // Save memory
                    item.data.put(optionString, level);
                }
            }
            switch (item.opID) {
                case 3063370: // Haste
                    item.data.put("skillID", 8000);
                    break;
                case 3063380: // Mystic Door
                    item.data.put("skillID", 8001);
                    break;
                case 3063390: // Sharp Eyes
                    item.data.put("skillID", 8002);
                    break;
                case 3063400: // Hyper Body
                    item.data.put("skillID", 8003);
                    break;
                case 3064470: // Combat Orders
                    item.data.put("skillID", 8004);
                    break;
                case 3064480: // Advanced Blessing
                    item.data.put("skillID", 8005);
                    break;
                case 3064490: // Speed Infusion
                    item.data.put("skillID", 8006);
                    break;
            }
            switch (GameConstants.getNebuliteGrade(item.opID)) {
                case 4: //S
                    gradeS.put(Integer.parseInt(dat.getName()), item);
                    break;
                case 3: //A
                    gradeA.put(Integer.parseInt(dat.getName()), item);
                    break;
                case 2: //B
                    gradeB.put(Integer.parseInt(dat.getName()), item);
                    break;
                case 1: //C
                    gradeC.put(Integer.parseInt(dat.getName()), item);
                    break;
                case 0: //D
                    gradeD.put(Integer.parseInt(dat.getName()), item);
                    break; // impossible to be -1 since we're looping in 306.img.xml					
            }
        }
        socketCache.put(4, gradeS);
        socketCache.put(3, gradeA);
        socketCache.put(2, gradeB);
        socketCache.put(1, gradeC);
        socketCache.put(0, gradeD);

        final MapleDataDirectoryEntry e = (MapleDataDirectoryEntry) etcData.getRoot().getEntry("Android");
        for (MapleDataEntry d : e.getFiles()) {
            final MapleData iz = etcData.getData("Android/" + d.getName());
            final List<Integer> hair = new ArrayList<Integer>(), face = new ArrayList<Integer>();
            for (MapleData ds : iz.getChildByPath("costume/hair")) {
                hair.add(MapleDataTool.getInt(ds, 30000));
            }
            for (MapleData ds : iz.getChildByPath("costume/face")) {
                face.add(MapleDataTool.getInt(ds, 20000));
            }
            androids.put(Integer.parseInt(d.getName().substring(0, 4)), new Pair<List<Integer>, List<Integer>>(hair, face));
        }

        final MapleData lifesData = etcData.getData("ItemPotLifeInfo.img");
        for (MapleData d : lifesData) {
            if (d.getChildByPath("info") != null && MapleDataTool.getInt("type", d.getChildByPath("info"), 0) == 1) {
                potLife.put(MapleDataTool.getInt("counsumeItem", d.getChildByPath("info"), 0), new Pair<Integer, Integer>(Integer.parseInt(d.getName()), d.getChildByPath("level").getChildren().size()));
            }
        }
        List<Triple<String, Point, Point>> thePointK = new ArrayList<Triple<String, Point, Point>>();
        List<Triple<String, Point, Point>> thePointA = new ArrayList<Triple<String, Point, Point>>();

        final MapleDataDirectoryEntry a = (MapleDataDirectoryEntry) chrData.getRoot().getEntry("Afterimage");
        for (MapleDataEntry b : a.getFiles()) {
            final MapleData iz = chrData.getData("Afterimage/" + b.getName());
            List<Triple<String, Point, Point>> thePoint = new ArrayList<Triple<String, Point, Point>>();
            Map<String, Pair<Point, Point>> dummy = new HashMap<String, Pair<Point, Point>>();
            for (MapleData i : iz) {
                for (MapleData xD : i) {
                    if (xD.getName().contains("prone") || xD.getName().contains("double") || xD.getName().contains("triple")) {
                        continue;
                    }
                    if ((b.getName().contains("bow") || b.getName().contains("Bow")) && !xD.getName().contains("shoot")) {
                        continue;
                    }
                    if ((b.getName().contains("gun") || b.getName().contains("cannon")) && !xD.getName().contains("shot")) {
                        continue;
                    }
                    if (dummy.containsKey(xD.getName())) {
                        if (xD.getChildByPath("lt") != null) {
                            Point lt = (Point) xD.getChildByPath("lt").getData();
                            Point ourLt = dummy.get(xD.getName()).left;
                            if (lt.x < ourLt.x) {
                                ourLt.x = lt.x;
                            }
                            if (lt.y < ourLt.y) {
                                ourLt.y = lt.y;
                            }
                        }
                        if (xD.getChildByPath("rb") != null) {
                            Point rb = (Point) xD.getChildByPath("rb").getData();
                            Point ourRb = dummy.get(xD.getName()).right;
                            if (rb.x > ourRb.x) {
                                ourRb.x = rb.x;
                            }
                            if (rb.y > ourRb.y) {
                                ourRb.y = rb.y;
                            }
                        }
                    } else {
                        Point lt = null, rb = null;
                        if (xD.getChildByPath("lt") != null) {
                            lt = (Point) xD.getChildByPath("lt").getData();
                        }
                        if (xD.getChildByPath("rb") != null) {
                            rb = (Point) xD.getChildByPath("rb").getData();
                        }
                        dummy.put(xD.getName(), new Pair<Point, Point>(lt, rb));
                    }
                }
            }
            for (Entry<String, Pair<Point, Point>> ez : dummy.entrySet()) {
                if (ez.getKey().length() > 2 && ez.getKey().substring(ez.getKey().length() - 2, ez.getKey().length() - 1).equals("D")) { //D = double weapon
                    thePointK.add(new Triple<String, Point, Point>(ez.getKey(), ez.getValue().left, ez.getValue().right));
                } else if (ez.getKey().contains("PoleArm")) { //D = double weapon
                    thePointA.add(new Triple<String, Point, Point>(ez.getKey(), ez.getValue().left, ez.getValue().right));
                } else {
                    thePoint.add(new Triple<String, Point, Point>(ez.getKey(), ez.getValue().left, ez.getValue().right));
                }
            }
            afterImage.put(b.getName().substring(0, b.getName().length() - 4), thePoint);
        }
        afterImage.put("katara", thePointK); //hackish
        afterImage.put("aran", thePointA); //hackish
    }

    public void runItems() {
        if (GameConstants.GMS) { //these must be loaded before items..
            final MapleData fData = etcData.getData("FamiliarInfo.img");
            for (MapleData d : fData) {
                StructFamiliar f = new StructFamiliar();
                f.grade = 0;
                f.mob = MapleDataTool.getInt("mob", d, 0);
                f.passive = MapleDataTool.getInt("passive", d, 0);
                f.itemid = MapleDataTool.getInt("consume", d, 0);
                f.familiar = Integer.parseInt(d.getName());
                familiars.put(f.familiar, f);
                familiars_Item.put(f.itemid, f);
                familiars_Mob.put(f.mob, f);
            }
            final MapleDataDirectoryEntry e = (MapleDataDirectoryEntry) chrData.getRoot().getEntry("Familiar");
            for (MapleDataEntry d : e.getFiles()) {
                final int id = Integer.parseInt(d.getName().substring(0, d.getName().length() - 4));
                if (familiars.containsKey(id)) {
                    familiars.get(id).grade = (byte) MapleDataTool.getInt("grade", chrData.getData("Familiar/" + d.getName()).getChildByPath("info"), 0);
                }
            }

            final MapleData mSetsData = etcData.getData("MonsterBookSet.img");
            for (MapleData d : mSetsData.getChildByPath("setList")) {
                if (MapleDataTool.getInt("deactivated", d, 0) > 0) {
                    continue;
                }
                final List<Integer> set = new ArrayList<Integer>(), potential = new ArrayList<Integer>(3);
                for (MapleData ds : d.getChildByPath("stats/potential")) {
                    if (ds.getType() != MapleDataType.STRING && MapleDataTool.getInt(ds, 0) > 0) {
                        potential.add(MapleDataTool.getInt(ds, 0));
                        if (potential.size() >= 5) {
                            break;
                        }
                    }
                }
                for (MapleData ds : d.getChildByPath("cardList")) {
                    set.add(MapleDataTool.getInt(ds, 0));
                }
                monsterBookSets.put(Integer.parseInt(d.getName()), new Triple<Integer, List<Integer>, List<Integer>>(MapleDataTool.getInt("setScore", d, 0), set, potential));
            }
        }

        try {
            Connection con = DatabaseConnection.getConnection();

            // Load Item Data
            PreparedStatement ps = con.prepareStatement("SELECT * FROM wz_itemdata");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                initItemInformation(rs);
            }
            rs.close();
            ps.close();

            // Load Item Equipment Data

            ps = con.prepareStatement("SELECT * FROM wz_itemequipdata ORDER BY itemid");
            rs = ps.executeQuery();
            while (rs.next()) {
                initItemEquipData(rs);
            }
            rs.close();
            ps.close();

            // Load Item Addition Data

            ps = con.prepareStatement("SELECT * FROM wz_itemadddata ORDER BY itemid");
            rs = ps.executeQuery();
            while (rs.next()) {
                initItemAddData(rs);
            }
            rs.close();
            ps.close();

            // Load Item Reward Data

            ps = con.prepareStatement("SELECT * FROM wz_itemrewarddata ORDER BY itemid");
            rs = ps.executeQuery();
            while (rs.next()) {
                initItemRewardData(rs);
            }
            rs.close();
            ps.close();

            // Finalize all Equipments

            for (Entry<Integer, ItemInformation> entry : dataCache.entrySet()) {
                if (GameConstants.getInventoryType(entry.getKey()) == MapleInventoryType.EQUIP) {
                    finalizeEquipData(entry.getValue());
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        System.out.println(dataCache.size() + " items loaded.");
    }

    public final List<StructItemOption> getPotentialInfo(int potId) {
        return (List) potentialCache.get(Integer.valueOf(potId));
    }

    public final Map<Integer, List<StructItemOption>> getAllPotentialInfo() {
        return this.potentialCache;
    }

    public final StructItemOption getSocketInfo(int potId) {
        int grade = GameConstants.getNebuliteGrade(potId);
        if (grade == -1) {
            return null;
        }
        return (StructItemOption) ((Map) this.socketCache.get(Integer.valueOf(grade))).get(Integer.valueOf(potId));
    }

    public final Map<Integer, StructItemOption> getAllSocketInfo(int grade) {
        return (Map) this.socketCache.get(Integer.valueOf(grade));
    }

    public final Collection<Integer> getMonsterBookList() {
        return this.mobIds.values();
    }

    public final Map<Integer, Integer> getMonsterBook() {
        return this.mobIds;
    }

    public final Pair<Integer, Integer> getPot(int f) {
        return (Pair) this.potLife.get(Integer.valueOf(f));
    }

    public final StructFamiliar getFamiliar(int f) {
        return (StructFamiliar) this.familiars.get(Integer.valueOf(f));
    }

    public final Map<Integer, StructFamiliar> getFamiliars() {
        return this.familiars;
    }

    public final StructFamiliar getFamiliarByItem(int f) {
        return (StructFamiliar) this.familiars_Item.get(Integer.valueOf(f));
    }

    public final StructFamiliar getFamiliarByMob(int f) {
        return (StructFamiliar) this.familiars_Mob.get(Integer.valueOf(f));
    }

    public static final MapleItemInformationProvider getInstance() {
        return instance;
    }

    public final Collection<ItemInformation> getAllItems() {
        return this.dataCache.values();
    }

    public final Pair<List<Integer>, List<Integer>> getAndroidInfo(int i) {
        return (Pair) this.androids.get(Integer.valueOf(i));
    }

    public final Triple<Integer, List<Integer>, List<Integer>> getMonsterBookInfo(int i) {
        return (Triple) this.monsterBookSets.get(Integer.valueOf(i));
    }

    public final Map<Integer, Triple<Integer, List<Integer>, List<Integer>>> getAllMonsterBookInfo() {
        return this.monsterBookSets;
    }

    protected final MapleData getItemData(int itemId) {
        MapleData ret = null;
        String idStr = "0" + String.valueOf(itemId);
        MapleDataDirectoryEntry root = this.itemData.getRoot();
        for (MapleDataDirectoryEntry topDir : root.getSubdirectories()) {
            for (MapleDataFileEntry iFile : topDir.getFiles()) {
                if (iFile.getName().equals(idStr.substring(0, 4) + ".img")) {
                    ret = this.itemData.getData(topDir.getName() + "/" + iFile.getName());
                    if (ret == null) {
                        return null;
                    }
                    ret = ret.getChildByPath(idStr);
                    return ret;
                }
                if (iFile.getName().equals(idStr.substring(1) + ".img")) {
                    ret = this.itemData.getData(topDir.getName() + "/" + iFile.getName());
                    return ret;
                }
            }
        }
        MapleDataDirectoryEntry topDir;
        return ret;
    }

    public Integer getItemIdByMob(int mobId) {
        return (Integer) this.mobIds.get(Integer.valueOf(mobId));
    }

    public Integer getSetId(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return Integer.valueOf(i.cardSet);
    }

    public List<Pair<Integer, String>> getAllItems2() {
        List itemPairs = new ArrayList();

        MapleData itemsData = this.stringData.getData("Cash.img");
        for (MapleData itemFolder : itemsData.getChildren()) {
            itemPairs.add(new Pair(Integer.valueOf(Integer.parseInt(itemFolder.getName())), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
        }
        itemsData = this.stringData.getData("Consume.img");
        for (MapleData itemFolder : itemsData.getChildren()) {
            itemPairs.add(new Pair(Integer.valueOf(Integer.parseInt(itemFolder.getName())), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
        }
        itemsData = this.stringData.getData("Eqp.img").getChildByPath("Eqp");
        for (MapleData eqpType : itemsData.getChildren()) {
            for (MapleData itemFolder : eqpType.getChildren()) {
                itemPairs.add(new Pair(Integer.valueOf(Integer.parseInt(itemFolder.getName())), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
            }
        }
        itemsData = this.stringData.getData("Etc.img").getChildByPath("Etc");
        for (MapleData itemFolder : itemsData.getChildren()) {
            itemPairs.add(new Pair(Integer.valueOf(Integer.parseInt(itemFolder.getName())), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
        }
        itemsData = this.stringData.getData("Ins.img");
        for (MapleData itemFolder : itemsData.getChildren()) {
            itemPairs.add(new Pair(Integer.valueOf(Integer.parseInt(itemFolder.getName())), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
        }
        itemsData = this.stringData.getData("Pet.img");
        for (MapleData itemFolder : itemsData.getChildren()) {
            itemPairs.add(new Pair(Integer.valueOf(Integer.parseInt(itemFolder.getName())), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
        }
        return itemPairs;
    }

    public final short getSlotMax(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return 0;
        }
        return i.slotMax;
    }

    public final int getWholePrice(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return 0;
        }
        return i.wholePrice;
    }

    public final double getPrice(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return -1.0D;
        }
        return i.price;
    }

    protected int rand(int min, int max) {
        return Math.abs(Randomizer.rand(min, max));
    }

    public Equip levelUpEquip(Equip equip, Map<String, Integer> sta) {
        Equip nEquip = (Equip) equip.copy();
        try {
            for (Map.Entry stat : sta.entrySet()) {
                if (((String) stat.getKey()).equals("STRMin")) {
                    nEquip.setStr((short) (nEquip.getStr() + rand(((Integer) stat.getValue()).intValue(), ((Integer) sta.get("STRMax")).intValue())));
                } else if (((String) stat.getKey()).equals("DEXMin")) {
                    nEquip.setDex((short) (nEquip.getDex() + rand(((Integer) stat.getValue()).intValue(), ((Integer) sta.get("DEXMax")).intValue())));
                } else if (((String) stat.getKey()).equals("INTMin")) {
                    nEquip.setInt((short) (nEquip.getInt() + rand(((Integer) stat.getValue()).intValue(), ((Integer) sta.get("INTMax")).intValue())));
                } else if (((String) stat.getKey()).equals("LUKMin")) {
                    nEquip.setLuk((short) (nEquip.getLuk() + rand(((Integer) stat.getValue()).intValue(), ((Integer) sta.get("LUKMax")).intValue())));
                } else if (((String) stat.getKey()).equals("PADMin")) {
                    nEquip.setWatk((short) (nEquip.getWatk() + rand(((Integer) stat.getValue()).intValue(), ((Integer) sta.get("PADMax")).intValue())));
                } else if (((String) stat.getKey()).equals("PDDMin")) {
                    nEquip.setWdef((short) (nEquip.getWdef() + rand(((Integer) stat.getValue()).intValue(), ((Integer) sta.get("PDDMax")).intValue())));
                } else if (((String) stat.getKey()).equals("MADMin")) {
                    nEquip.setMatk((short) (nEquip.getMatk() + rand(((Integer) stat.getValue()).intValue(), ((Integer) sta.get("MADMax")).intValue())));
                } else if (((String) stat.getKey()).equals("MDDMin")) {
                    nEquip.setMdef((short) (nEquip.getMdef() + rand(((Integer) stat.getValue()).intValue(), ((Integer) sta.get("MDDMax")).intValue())));
                } else if (((String) stat.getKey()).equals("ACCMin")) {
                    nEquip.setAcc((short) (nEquip.getAcc() + rand(((Integer) stat.getValue()).intValue(), ((Integer) sta.get("ACCMax")).intValue())));
                } else if (((String) stat.getKey()).equals("EVAMin")) {
                    nEquip.setAvoid((short) (nEquip.getAvoid() + rand(((Integer) stat.getValue()).intValue(), ((Integer) sta.get("EVAMax")).intValue())));
                } else if (((String) stat.getKey()).equals("SpeedMin")) {
                    nEquip.setSpeed((short) (nEquip.getSpeed() + rand(((Integer) stat.getValue()).intValue(), ((Integer) sta.get("SpeedMax")).intValue())));
                } else if (((String) stat.getKey()).equals("JumpMin")) {
                    nEquip.setJump((short) (nEquip.getJump() + rand(((Integer) stat.getValue()).intValue(), ((Integer) sta.get("JumpMax")).intValue())));
                } else if (((String) stat.getKey()).equals("MHPMin")) {
                    nEquip.setHp((short) (nEquip.getHp() + rand(((Integer) stat.getValue()).intValue(), ((Integer) sta.get("MHPMax")).intValue())));
                } else if (((String) stat.getKey()).equals("MMPMin")) {
                    nEquip.setMp((short) (nEquip.getMp() + rand(((Integer) stat.getValue()).intValue(), ((Integer) sta.get("MMPMax")).intValue())));
                } else if (((String) stat.getKey()).equals("MaxHPMin")) {
                    nEquip.setHp((short) (nEquip.getHp() + rand(((Integer) stat.getValue()).intValue(), ((Integer) sta.get("MaxHPMax")).intValue())));
                } else if (((String) stat.getKey()).equals("MaxMPMin")) {
                    nEquip.setMp((short) (nEquip.getMp() + rand(((Integer) stat.getValue()).intValue(), ((Integer) sta.get("MaxMPMax")).intValue())));
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return nEquip;
    }

    public final List<Triple<String, String, String>> getEquipAdditions(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return i.equipAdditions;
    }

    public final String getEquipAddReqs(int itemId, String key, String sub) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        for (Triple data : i.equipAdditions) {
            if ((((String) data.getLeft()).equals("key")) && (((String) data.getMid()).equals("con:" + sub))) {
                return (String) data.getRight();
            }
        }
        return null;
    }

    public final Map<Integer, Map<String, Integer>> getEquipIncrements(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return i.equipIncs;
    }

    public final List<Integer> getEquipSkills(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return i.incSkill;
    }

    public final Map<String, Integer> getEquipStats(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return i.equipStats;
    }

    public final boolean canEquip(Map<String, Integer> stats, int itemid, int level, int job, int fame, int str, int dex, int luk, int int_, int supremacy) {
        if (level + supremacy >= (stats.containsKey("reqLevel") ? ((Integer) stats.get("reqLevel")).intValue() : 0)) {
            if (str >= (stats.containsKey("reqSTR") ? ((Integer) stats.get("reqSTR")).intValue() : 0)) {
                if (dex >= (stats.containsKey("reqDEX") ? ((Integer) stats.get("reqDEX")).intValue() : 0)) {
                    if (luk >= (stats.containsKey("reqLUK") ? ((Integer) stats.get("reqLUK")).intValue() : 0)) {
                        if (int_ >= (stats.containsKey("reqINT") ? ((Integer) stats.get("reqINT")).intValue() : 0)) {
                            Integer fameReq = (Integer) stats.get("reqPOP");
                            if ((fameReq != null) && (fame < fameReq.intValue())) {
                                return false;
                            }
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public final int getReqLevel(int itemId) {
        if ((getEquipStats(itemId) == null) || (!getEquipStats(itemId).containsKey("reqLevel"))) {
            return 0;
        }
        return ((Integer) getEquipStats(itemId).get("reqLevel")).intValue();
    }

    public final int getSlots(int itemId) {
        if ((getEquipStats(itemId) == null) || (!getEquipStats(itemId).containsKey("tuc"))) {
            return 0;
        }
        return ((Integer) getEquipStats(itemId).get("tuc")).intValue();
    }

    public final Integer getSetItemID(int itemId) {
        if ((getEquipStats(itemId) == null) || (!getEquipStats(itemId).containsKey("setItemID"))) {
            return Integer.valueOf(0);
        }
        return (Integer) getEquipStats(itemId).get("setItemID");
    }

    public final StructSetItem getSetItem(int setItemId) {
        return (StructSetItem) this.setItems.get(Byte.valueOf((byte) setItemId));
    }

    public final List<Integer> getScrollReqs(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return i.scrollReqs;
    }

    public final Item scrollEquipWithId(Item equip, Item scrollId, boolean ws, MapleCharacter chr, int vegas) {
        if (equip.getType() == 1) {
            Equip nEquip = (Equip) equip;
            final Map<String, Integer> stats = getEquipStats(scrollId.getItemId());
            final Map<String, Integer> eqstats = getEquipStats(equip.getItemId());
            int succ = (GameConstants.isEquipScroll(scrollId.getItemId())) || (GameConstants.isPotentialScroll(scrollId.getItemId())) || (!stats.containsKey("success")) ? 0 : GameConstants.isTablet(scrollId.getItemId()) ? GameConstants.getSuccessTablet(scrollId.getItemId(), nEquip.getLevel()) : ((Integer) stats.get("success")).intValue();
            int curse = (GameConstants.isEquipScroll(scrollId.getItemId())) || (GameConstants.isPotentialScroll(scrollId.getItemId())) || (!stats.containsKey("cursed")) ? 0 : GameConstants.isTablet(scrollId.getItemId()) ? GameConstants.getCurseTablet(scrollId.getItemId(), nEquip.getLevel()) : ((Integer) stats.get("cursed")).intValue();
            int added = (ItemFlag.LUCKS_KEY.check(equip.getFlag()) ? 10 : 0) + chr.getTrait(MapleTrait.MapleTraitType.craft).getLevel() / 10;
            int success = succ + ((vegas == 5610001) && (succ == 60) ? 30 : (vegas == 5610000) && (succ == 10) ? 20 : 0) + added;
            if ((ItemFlag.LUCKS_KEY.check(equip.getFlag())) && (!GameConstants.isPotentialScroll(scrollId.getItemId())) && (!GameConstants.isEquipScroll(scrollId.getItemId())) && (!GameConstants.isSpecialScroll(scrollId.getItemId()))) {
                equip.setFlag((short) (equip.getFlag() - ItemFlag.LUCKS_KEY.getValue()));
            }
            if ((GameConstants.isPotentialScroll(scrollId.getItemId())) || (GameConstants.isEquipScroll(scrollId.getItemId())) || (GameConstants.isSpecialScroll(scrollId.getItemId())) || (Randomizer.nextInt(100) <= success)) {
                short flag;
                switch (scrollId.getItemId()) {
                    case 2049000:
                    case 2049001:
                    case 2049002:
                    case 2049003:
                    case 2049004:
                    case 2049005:
                        if ((eqstats.containsKey("tuc")) && (nEquip.getLevel() + nEquip.getUpgradeSlots() < ((Integer) eqstats.get("tuc")).intValue())) {
                            nEquip.setUpgradeSlots((byte) (nEquip.getUpgradeSlots() + 1));
                        }
                        break;
                    case 2049006:
                    case 2049007:
                    case 2049008:
                        if ((eqstats.containsKey("tuc")) && (nEquip.getLevel() + nEquip.getUpgradeSlots() < ((Integer) eqstats.get("tuc")).intValue())) {
                            nEquip.setUpgradeSlots((byte) (nEquip.getUpgradeSlots() + 2));
                        }
                        break;
                    case 2040727:
                        flag = nEquip.getFlag();
                        flag = (short) (flag | ItemFlag.SPIKES.getValue());
                        nEquip.setFlag(flag);
                        break;
                    case 2041058:
                        flag = nEquip.getFlag();
                        flag = (short) (flag | ItemFlag.COLD.getValue());
                        nEquip.setFlag(flag);
                        break;
                    case 2530000:
                    case 2530001:
                    case 5063000:
                        flag = nEquip.getFlag();
                        flag = (short) (flag | ItemFlag.LUCKS_KEY.getValue());
                        nEquip.setFlag(flag);
                        break;
                    case 2531000:
                    case 5064000:
                    case 5064002:
                    case 5064003:
                    case 5064004:
                        flag = nEquip.getFlag();
                        flag = (short) (flag | ItemFlag.SHIELD_WARD.getValue());
                        nEquip.setFlag(flag);
                        break;
                    case 5064100:
                    case 5064101:
                        flag = nEquip.getFlag();
                        flag = (short) (flag | ItemFlag.SLOTS_PROTECT.getValue());
                        nEquip.setFlag(flag);
                        break;
                    case 2049600:
                    case 2049601:
                    case 2049604:
                    case 2049605:
                    case 2049606:
                    case 2049607:
                    case 2049608:
                    case 2049609:
                    case 2049610:
                    case 2049611:
                        Item item;
                        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

                        item = ii.getStats((Equip) ii.getEquipById(nEquip.getItemId()), nEquip.getPotential1(), nEquip.getPotential2(), nEquip.getPotential3(), nEquip.getPotential4(), nEquip.getPotential5(), nEquip.getSocket1(), nEquip.getSocket2(), nEquip.getSocket3());

                        if (chr.getInventory(GameConstants.getInventoryType(item.getItemId())).getNextFreeSlot() > 0 && !isMSI(nEquip, (short) 32760)) {



                            MapleInventoryManipulator.addbyItem(chr.getClient(), item);
                        } else {
                            break;
                        }



                        break;
                    default:
                        if (GameConstants.isChaosScroll(scrollId.getItemId())) {
                            int z = GameConstants.getChaosNumber(scrollId.getItemId());
                            if (nEquip.getStr() > 0) {
                                nEquip.setStr((short) (nEquip.getStr() + Randomizer.nextInt(z) * (Randomizer.nextBoolean() ? 1 : -1)));
                            }
                            if (nEquip.getDex() > 0) {
                                nEquip.setDex((short) (nEquip.getDex() + Randomizer.nextInt(z) * (Randomizer.nextBoolean() ? 1 : -1)));
                            }
                            if (nEquip.getInt() > 0) {
                                nEquip.setInt((short) (nEquip.getInt() + Randomizer.nextInt(z) * (Randomizer.nextBoolean() ? 1 : -1)));
                            }
                            if (nEquip.getLuk() > 0) {
                                nEquip.setLuk((short) (nEquip.getLuk() + Randomizer.nextInt(z) * (Randomizer.nextBoolean() ? 1 : -1)));
                            }
                            if (nEquip.getWatk() > 0) {
                                nEquip.setWatk((short) (nEquip.getWatk() + Randomizer.nextInt(z) * (Randomizer.nextBoolean() ? 1 : -1)));
                            }
                            if (nEquip.getWdef() > 0) {
                                nEquip.setWdef((short) (nEquip.getWdef() + Randomizer.nextInt(z) * (Randomizer.nextBoolean() ? 1 : -1)));
                            }
                            if (nEquip.getMatk() > 0) {
                                nEquip.setMatk((short) (nEquip.getMatk() + Randomizer.nextInt(z) * (Randomizer.nextBoolean() ? 1 : -1)));
                            }
                            if (nEquip.getMdef() > 0) {
                                nEquip.setMdef((short) (nEquip.getMdef() + Randomizer.nextInt(z) * (Randomizer.nextBoolean() ? 1 : -1)));
                            }
                            if (nEquip.getAcc() > 0) {
                                nEquip.setAcc((short) (nEquip.getAcc() + Randomizer.nextInt(z) * (Randomizer.nextBoolean() ? 1 : -1)));
                            }
                            if (nEquip.getAvoid() > 0) {
                                nEquip.setAvoid((short) (nEquip.getAvoid() + Randomizer.nextInt(z) * (Randomizer.nextBoolean() ? 1 : -1)));
                            }
                            if (nEquip.getSpeed() > 0) {
                                nEquip.setSpeed((short) (nEquip.getSpeed() + Randomizer.nextInt(z) * (Randomizer.nextBoolean() ? 1 : -1)));
                            }
                            if (nEquip.getJump() > 0) {
                                nEquip.setJump((short) (nEquip.getJump() + Randomizer.nextInt(z) * (Randomizer.nextBoolean() ? 1 : -1)));
                            }
                            if (nEquip.getHp() > 0) {
                                nEquip.setHp((short) (nEquip.getHp() + Randomizer.nextInt(z) * (Randomizer.nextBoolean() ? 1 : -1)));
                            }
                            if (nEquip.getMp() > 0) {
                                nEquip.setMp((short) (nEquip.getMp() + Randomizer.nextInt(z) * (Randomizer.nextBoolean() ? 1 : -1)));
                            }
                        } else if (GameConstants.isEquipScroll(scrollId.getItemId())) {
                            int chanc = Math.max((scrollId.getItemId() == 2049305 ? 60 : (scrollId.getItemId() == 2049300) || (scrollId.getItemId() == 2049303) ? 100 : 80) - nEquip.getEnhance() * 10, 10) + added;
                            if (Randomizer.nextInt(100) > chanc) {
                                return null;
                            }
                            for (int i = 0; i < (scrollId.getItemId() == 2049304 ? 3 : scrollId.getItemId() == 2049305 ? 4 : scrollId.getItemId() == 2049308 ? 5 : 1); i++) {
                                if ((nEquip.getStr() > 0) || (Randomizer.nextInt(50) == 1)) {
                                    nEquip.setStr((short) (nEquip.getStr() + Randomizer.nextInt(5)));
                                }
                                if ((nEquip.getDex() > 0) || (Randomizer.nextInt(50) == 1)) {
                                    nEquip.setDex((short) (nEquip.getDex() + Randomizer.nextInt(5)));
                                }
                                if ((nEquip.getInt() > 0) || (Randomizer.nextInt(50) == 1)) {
                                    nEquip.setInt((short) (nEquip.getInt() + Randomizer.nextInt(5)));
                                }
                                if ((nEquip.getLuk() > 0) || (Randomizer.nextInt(50) == 1)) {
                                    nEquip.setLuk((short) (nEquip.getLuk() + Randomizer.nextInt(5)));
                                }
                                if ((nEquip.getWatk() > 0) && (GameConstants.isWeapon(nEquip.getItemId()))) {
                                    nEquip.setWatk((short) (nEquip.getWatk() + Randomizer.nextInt(5)));
                                }
                                if ((nEquip.getWdef() > 0) || (Randomizer.nextInt(40) == 1)) {
                                    nEquip.setWdef((short) (nEquip.getWdef() + Randomizer.nextInt(5)));
                                }
                                if ((nEquip.getMatk() > 0) && (GameConstants.isWeapon(nEquip.getItemId()))) {
                                    nEquip.setMatk((short) (nEquip.getMatk() + Randomizer.nextInt(5)));
                                }
                                if ((nEquip.getMdef() > 0) || (Randomizer.nextInt(40) == 1)) {
                                    nEquip.setMdef((short) (nEquip.getMdef() + Randomizer.nextInt(5)));
                                }
                                if ((nEquip.getAcc() > 0) || (Randomizer.nextInt(20) == 1)) {
                                    nEquip.setAcc((short) (nEquip.getAcc() + Randomizer.nextInt(5)));
                                }
                                if ((nEquip.getAvoid() > 0) || (Randomizer.nextInt(20) == 1)) {
                                    nEquip.setAvoid((short) (nEquip.getAvoid() + Randomizer.nextInt(5)));
                                }
                                if ((nEquip.getSpeed() > 0) || (Randomizer.nextInt(10) == 1)) {
                                    nEquip.setSpeed((short) (nEquip.getSpeed() + Randomizer.nextInt(5)));
                                }
                                if ((nEquip.getJump() > 0) || (Randomizer.nextInt(10) == 1)) {
                                    nEquip.setJump((short) (nEquip.getJump() + Randomizer.nextInt(5)));
                                }
                                if ((nEquip.getHp() > 0) || (Randomizer.nextInt(5) == 1)) {
                                    nEquip.setHp((short) (nEquip.getHp() + Randomizer.nextInt(5)));
                                }
                                if ((nEquip.getMp() > 0) || (Randomizer.nextInt(5) == 1)) {
                                    nEquip.setMp((short) (nEquip.getMp() + Randomizer.nextInt(5)));
                                }
                                nEquip.setEnhance((byte) (nEquip.getEnhance() + 1));
                            }
                        } else if (GameConstants.isPotentialScroll(scrollId.getItemId())) {
                            if ((nEquip.getState() <= 17) && (scrollId.getItemId() / 100 == 20497)) {
                                int chanc = (scrollId.getItemId() == 2049700 ? 100 : 80) + added;
                                if (Randomizer.nextInt(100) > chanc) {
                                    return null;
                                }
                                nEquip.renewPotential(2);
                            } else if (nEquip.getState() == 0) {
                                int chanc = (scrollId.getItemId() == 2049400 ? 90 : (scrollId.getItemId() == 5534000) || (scrollId.getItemId() == 2049402) || (scrollId.getItemId() == 2049406) ? 100 : 70) + added;
                                if (Randomizer.nextInt(100) > chanc) {
                                    return null;
                                }
                                nEquip.resetPotential();
                            }
                        } else {
                            for (Map.Entry stat : stats.entrySet()) {
                                String key = (String) stat.getKey();

                                if (key.equals("STR")) {
                                    nEquip.setStr((short) (nEquip.getStr() + ((Integer) stat.getValue()).intValue()));
                                } else if (key.equals("DEX")) {
                                    nEquip.setDex((short) (nEquip.getDex() + ((Integer) stat.getValue()).intValue()));
                                } else if (key.equals("INT")) {
                                    nEquip.setInt((short) (nEquip.getInt() + ((Integer) stat.getValue()).intValue()));
                                } else if (key.equals("LUK")) {
                                    nEquip.setLuk((short) (nEquip.getLuk() + ((Integer) stat.getValue()).intValue()));
                                } else if (key.equals("PAD")) {
                                    nEquip.setWatk((short) (nEquip.getWatk() + ((Integer) stat.getValue()).intValue()));
                                } else if (key.equals("PDD")) {
                                    nEquip.setWdef((short) (nEquip.getWdef() + ((Integer) stat.getValue()).intValue()));
                                } else if (key.equals("MAD")) {
                                    nEquip.setMatk((short) (nEquip.getMatk() + ((Integer) stat.getValue()).intValue()));
                                } else if (key.equals("MDD")) {
                                    nEquip.setMdef((short) (nEquip.getMdef() + ((Integer) stat.getValue()).intValue()));
                                } else if (key.equals("ACC")) {
                                    nEquip.setAcc((short) (nEquip.getAcc() + ((Integer) stat.getValue()).intValue()));
                                } else if (key.equals("EVA")) {
                                    nEquip.setAvoid((short) (nEquip.getAvoid() + ((Integer) stat.getValue()).intValue()));
                                } else if (key.equals("Speed")) {
                                    nEquip.setSpeed((short) (nEquip.getSpeed() + ((Integer) stat.getValue()).intValue()));
                                } else if (key.equals("Jump")) {
                                    nEquip.setJump((short) (nEquip.getJump() + ((Integer) stat.getValue()).intValue()));
                                } else if (key.equals("MHP")) {
                                    nEquip.setHp((short) (nEquip.getHp() + ((Integer) stat.getValue()).intValue()));
                                } else if (key.equals("MMP")) {
                                    nEquip.setMp((short) (nEquip.getMp() + ((Integer) stat.getValue()).intValue()));
                                }
                            }
                        }
                        break;
                }
                if (GameConstants.isInnocence(scrollId.getItemId())) {
                    nEquip.setAcc((short) 6969);
                }
                if ((!GameConstants.isCleanSlate(scrollId.getItemId())) && !GameConstants.isInnocence(scrollId.getItemId()) && (!GameConstants.isSpecialScroll(scrollId.getItemId())) && (!GameConstants.isEquipScroll(scrollId.getItemId())) && (!GameConstants.isPotentialScroll(scrollId.getItemId()))) {
                    nEquip.setUpgradeSlots((byte) (nEquip.getUpgradeSlots() - 1));
                    nEquip.setLevel((byte) (nEquip.getLevel() + 1));
                }
            } else {
                if ((!ws) && !GameConstants.isInnocence(scrollId.getItemId()) && (!GameConstants.isCleanSlate(scrollId.getItemId())) && (!GameConstants.isSpecialScroll(scrollId.getItemId())) && (!GameConstants.isEquipScroll(scrollId.getItemId())) && (!GameConstants.isPotentialScroll(scrollId.getItemId()))) {

                    if (ItemFlag.SLOTS_PROTECT.check(nEquip.getFlag())) {
                        chr.dropMessage(5, "Item has successfully been protected.");
                    } else {
                        nEquip.setUpgradeSlots((byte) (nEquip.getUpgradeSlots() - 1));
                    }


                }
                if (Randomizer.nextInt(99) < curse) {
                    return null;
                }
            }
        }
        return equip;
    }

    public final Item getEquipById(int equipId) {
        return getEquipById(equipId, -1);
    }

    public final Item getEquipById(int equipId, int ringId) {
        ItemInformation i = getItemInformation(equipId);
        if (i == null) {
            return new Equip(equipId, (short) 0, ringId, (short) 0);
        }
        Item eq = i.eq.copy();
        eq.setUniqueId(ringId);
        return eq;
    }

    protected final short getRandStatFusion(short defaultValue, int value1, int value2) {
        if (defaultValue == 0) {
            return 0;
        }
        int range = (value1 + value2) / 2 - defaultValue;
        int rand = Randomizer.nextInt(Math.abs(range) + 1);
        return (short) (defaultValue + (range < 0 ? -rand : rand));
    }

    protected final short getRandStat(short defaultValue, int maxRange) {
        if (defaultValue == 0) {
            return 0;
        }

        int lMaxRange = (int) Math.min(Math.ceil(defaultValue * 0.1D), maxRange);

        return (short) (defaultValue - lMaxRange + Randomizer.nextInt(lMaxRange * 2 + 1));
    }

    protected final short getRandStatAbove(short defaultValue, int maxRange) {
        if (defaultValue <= 0) {
            return 0;
        }
        int lMaxRange = (int) Math.min(Math.ceil(defaultValue * 0.1D), maxRange);

        return (short) (defaultValue + Randomizer.nextInt(lMaxRange + 1));
    }

    public final Equip randomizeStats(Equip equip) {
        equip.setStr(getRandStat(equip.getStr(), 5));
        equip.setDex(getRandStat(equip.getDex(), 5));
        equip.setInt(getRandStat(equip.getInt(), 5));
        equip.setLuk(getRandStat(equip.getLuk(), 5));
        equip.setMatk(getRandStat(equip.getMatk(), 5));
        equip.setWatk(getRandStat(equip.getWatk(), 5));
        equip.setAcc(getRandStat(equip.getAcc(), 5));
        equip.setAvoid(getRandStat(equip.getAvoid(), 5));
        equip.setJump(getRandStat(equip.getJump(), 5));
        equip.setHands(getRandStat(equip.getHands(), 5));
        equip.setSpeed(getRandStat(equip.getSpeed(), 5));
        equip.setWdef(getRandStat(equip.getWdef(), 10));
        equip.setMdef(getRandStat(equip.getMdef(), 10));
        equip.setHp(getRandStat(equip.getHp(), 10));
        equip.setMp(getRandStat(equip.getMp(), 10));
        return equip;
    }

    public final Equip randomizeStats_Above(Equip equip) {
        equip.setStr(getRandStatAbove(equip.getStr(), 5));
        equip.setDex(getRandStatAbove(equip.getDex(), 5));
        equip.setInt(getRandStatAbove(equip.getInt(), 5));
        equip.setLuk(getRandStatAbove(equip.getLuk(), 5));
        equip.setMatk(getRandStatAbove(equip.getMatk(), 5));
        equip.setWatk(getRandStatAbove(equip.getWatk(), 5));
        equip.setAcc(getRandStatAbove(equip.getAcc(), 5));
        equip.setAvoid(getRandStatAbove(equip.getAvoid(), 5));
        equip.setJump(getRandStatAbove(equip.getJump(), 5));
        equip.setHands(getRandStatAbove(equip.getHands(), 5));
        equip.setSpeed(getRandStatAbove(equip.getSpeed(), 5));
        equip.setWdef(getRandStatAbove(equip.getWdef(), 10));
        equip.setMdef(getRandStatAbove(equip.getMdef(), 10));
        equip.setHp(getRandStatAbove(equip.getHp(), 10));
        equip.setMp(getRandStatAbove(equip.getMp(), 10));
        return equip;
    }

    public final Equip fuse(Equip equip1, Equip equip2) {
        if (equip1.getItemId() != equip2.getItemId()) {
            return equip1;
        }
        Equip equip = (Equip) getEquipById(equip1.getItemId());
        equip.setStr(getRandStatFusion(equip.getStr(), equip1.getStr(), equip2.getStr()));
        equip.setDex(getRandStatFusion(equip.getDex(), equip1.getDex(), equip2.getDex()));
        equip.setInt(getRandStatFusion(equip.getInt(), equip1.getInt(), equip2.getInt()));
        equip.setLuk(getRandStatFusion(equip.getLuk(), equip1.getLuk(), equip2.getLuk()));
        equip.setMatk(getRandStatFusion(equip.getMatk(), equip1.getMatk(), equip2.getMatk()));
        equip.setWatk(getRandStatFusion(equip.getWatk(), equip1.getWatk(), equip2.getWatk()));
        equip.setAcc(getRandStatFusion(equip.getAcc(), equip1.getAcc(), equip2.getAcc()));
        equip.setAvoid(getRandStatFusion(equip.getAvoid(), equip1.getAvoid(), equip2.getAvoid()));
        equip.setJump(getRandStatFusion(equip.getJump(), equip1.getJump(), equip2.getJump()));
        equip.setHands(getRandStatFusion(equip.getHands(), equip1.getHands(), equip2.getHands()));
        equip.setSpeed(getRandStatFusion(equip.getSpeed(), equip1.getSpeed(), equip2.getSpeed()));
        equip.setWdef(getRandStatFusion(equip.getWdef(), equip1.getWdef(), equip2.getWdef()));
        equip.setMdef(getRandStatFusion(equip.getMdef(), equip1.getMdef(), equip2.getMdef()));
        equip.setHp(getRandStatFusion(equip.getHp(), equip1.getHp(), equip2.getHp()));
        equip.setMp(getRandStatFusion(equip.getMp(), equip1.getMp(), equip2.getMp()));
        return equip;
    }

    public final int getTotalStat(Equip equip) {
        return equip.getStr() + equip.getDex() + equip.getInt() + equip.getLuk() + equip.getMatk() + equip.getWatk() + equip.getAcc() + equip.getAvoid() + equip.getJump() + equip.getHands() + equip.getSpeed() + equip.getHp() + equip.getMp() + equip.getWdef() + equip.getMdef();
    }

    public final MapleStatEffect getItemEffect(int itemId) {
        MapleStatEffect ret = (MapleStatEffect) this.itemEffects.get(Integer.valueOf(itemId));
        if (ret == null) {
            MapleData item = getItemData(itemId);
            if ((item == null) || (item.getChildByPath("spec") == null)) {
                return null;
            }
            ret = MapleStatEffect.loadItemEffectFromData(item.getChildByPath("spec"), itemId);
            this.itemEffects.put(Integer.valueOf(itemId), ret);
        }
        return ret;
    }

    public final MapleStatEffect getItemEffectEX(int itemId) {
        MapleStatEffect ret = (MapleStatEffect) this.itemEffectsEx.get(Integer.valueOf(itemId));
        if (ret == null) {
            MapleData item = getItemData(itemId);
            if ((item == null) || (item.getChildByPath("specEx") == null)) {
                return null;
            }
            ret = MapleStatEffect.loadItemEffectFromData(item.getChildByPath("specEx"), itemId);
            this.itemEffectsEx.put(Integer.valueOf(itemId), ret);
        }
        return ret;
    }

    public final int getCreateId(int id) {
        ItemInformation i = getItemInformation(id);
        if (i == null) {
            return 0;
        }
        return i.create;
    }

    public final int getCardMobId(int id) {
        ItemInformation i = getItemInformation(id);
        if (i == null) {
            return 0;
        }
        return i.monsterBook;
    }

    public final int getBagType(int id) {
        ItemInformation i = getItemInformation(id);
        if (i == null) {
            return 0;
        }
        return i.flag & 0xF;
    }

    public final int getWatkForProjectile(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if ((i == null) || (i.equipStats == null) || (i.equipStats.get("incPAD") == null)) {
            return 0;
        }
        return ((Integer) i.equipStats.get("incPAD")).intValue();
    }

    public final boolean canScroll(int scrollid, int itemid) {
        return scrollid / 100 % 100 == itemid / 10000 % 100;
    }

    public final String getName(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return i.name;
    }

    public final String getDesc(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return i.desc;
    }

    public final String getMsg(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return i.msg;
    }

    public final short getItemMakeLevel(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return 0;
        }
        return i.itemMakeLevel;
    }
//
//    public final boolean isDropRestricted(int itemId) {
//        ItemInformation i = getItemInformation(itemId);
//        if (i == null) {
//            return false;
//        }
//        return (((i.flag & 0x200) != 0) || ((i.flag & 0x400) != 0) || (GameConstants.isDropRestricted(itemId))) && ((itemId == 3012000) || (itemId == 3012015) || (itemId / 10000 != 301)) && (itemId != 2041200) && (itemId != 5640000) && (itemId != 4170023) && (itemId != 2040124) && (itemId != 2040125) && (itemId != 2040126) && (itemId != 2040211) && (itemId != 2040212) && (itemId != 2040227) && (itemId != 2040228) && (itemId != 2040229) && (itemId != 2040230) && (itemId != 1002926) && (itemId != 1002906) && (itemId != 1002927);
//    }
//
//    public final boolean isPickupRestricted(int itemId) {
//        ItemInformation i = getItemInformation(itemId);
//        if (i == null) {
//            return false;
//        }
//        return (((i.flag & 0x80) != 0) || (GameConstants.isPickupRestricted(itemId))) && (itemId != 4001168) && (itemId != 4031306) && (itemId != 4031307);
//    }
//
//    public final boolean isAccountShared(int itemId) {
//        ItemInformation i = getItemInformation(itemId);
//        if (i == null) {
//            return false;
//        }
//        return (i.flag & 0x100) != 0;
//    }

    public final int getStateChangeItem(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return 0;
        }
        return i.stateChange;
    }

    public final int getMeso(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return 0;
        }
        return i.meso;
    }

//    public final boolean isShareTagEnabled(int itemId) {
//        ItemInformation i = getItemInformation(itemId);
//        if (i == null) {
//            return false;
//        }
//        return (i.flag & 0x800) != 0;
//    }

    public final boolean isKarmaEnabled(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return false;
        }
        return i.karmaEnabled == 1;
    }

    public final boolean isPKarmaEnabled(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return false;
        }
        return i.karmaEnabled == 2;
    }

//    public final boolean isPickupBlocked(int itemId) {
//        ItemInformation i = getItemInformation(itemId);
//        if (i == null) {
//            return false;
//        }
//        return (i.flag & 0x40) != 0;
//    }
//
//    public final boolean isLogoutExpire(int itemId) {
//        ItemInformation i = getItemInformation(itemId);
//        if (i == null) {
//            return false;
//        }
//        return (i.flag & 0x20) != 0;
//    }
//
//    public final boolean cantSell(int itemId) {
//        ItemInformation i = getItemInformation(itemId);
//        if (i == null) {
//            return false;
//        }
//        return (i.flag & 0x10) != 0;
//    }

    public final Pair<Integer, List<StructRewardItem>> getRewardItem(int itemid) {
        ItemInformation i = getItemInformation(itemid);
        if (i == null) {
            return null;
        }
        return new Pair(Integer.valueOf(i.totalprob), i.rewardItems);
    }

    public final boolean isMobHP(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return false;
        }
        return (i.flag & 0x1000) != 0;
    }

    public final boolean isQuestItem(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return false;
        }
        return ((i.flag & 0x200) != 0) && (itemId / 10000 != 301);
    }

    public final Pair<Integer, List<Integer>> questItemInfo(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return new Pair(Integer.valueOf(i.questId), i.questItems);
    }

    public final Pair<Integer, String> replaceItemInfo(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return new Pair(Integer.valueOf(i.replaceItem), i.replaceMsg);
    }

    public final List<Triple<String, Point, Point>> getAfterImage(String after) {
        return (List) this.afterImage.get(after);
    }

    public final String getAfterImage(int itemId) {
        ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return i.afterImage;
    }

    public final boolean itemExists(int itemId) {
        if (GameConstants.getInventoryType(itemId) == MapleInventoryType.UNDEFINED) {
            return false;
        }
        return getItemInformation(itemId) != null;
    }

    public final boolean isCash(int itemId) {
        if (getEquipStats(itemId) == null) {
            return GameConstants.getInventoryType(itemId) == MapleInventoryType.CASH;
        }
        return (GameConstants.getInventoryType(itemId) == MapleInventoryType.CASH) || (getEquipStats(itemId).get("cash") != null);
    }

    public final ItemInformation getItemInformation(int itemId) {
        if (itemId <= 0) {
            return null;
        }
        return (ItemInformation) this.dataCache.get(Integer.valueOf(itemId));
    }

    public void initItemRewardData(ResultSet sqlRewardData) throws SQLException {
        int itemID = sqlRewardData.getInt("itemid");
        if ((this.tmpInfo == null) || (this.tmpInfo.itemId != itemID)) {
            if (!this.dataCache.containsKey(Integer.valueOf(itemID))) {
                System.out.println("[initItemRewardData] Tried to load an item while this is not in the cache: " + itemID);
                return;
            }
            this.tmpInfo = ((ItemInformation) this.dataCache.get(Integer.valueOf(itemID)));
        }

        if (this.tmpInfo.rewardItems == null) {
            this.tmpInfo.rewardItems = new ArrayList();
        }

        StructRewardItem add = new StructRewardItem();
        add.itemid = sqlRewardData.getInt("item");
        add.period = (add.itemid == 1122017 ? Math.max(sqlRewardData.getInt("period"), 7200) : sqlRewardData.getInt("period"));
        add.prob = sqlRewardData.getInt("prob");
        add.quantity = sqlRewardData.getShort("quantity");
        add.worldmsg = (sqlRewardData.getString("worldMsg").length() <= 0 ? null : sqlRewardData.getString("worldMsg"));
        add.effect = sqlRewardData.getString("effect");

        this.tmpInfo.rewardItems.add(add);
    }

    public void initItemAddData(ResultSet sqlAddData) throws SQLException {
        int itemID = sqlAddData.getInt("itemid");
        if ((this.tmpInfo == null) || (this.tmpInfo.itemId != itemID)) {
            if (!this.dataCache.containsKey(Integer.valueOf(itemID))) {
                System.out.println("[initItemAddData] Tried to load an item while this is not in the cache: " + itemID);
                return;
            }
            this.tmpInfo = ((ItemInformation) this.dataCache.get(Integer.valueOf(itemID)));
        }

        if (this.tmpInfo.equipAdditions == null) {
            this.tmpInfo.equipAdditions = new LinkedList();
        }

        while (sqlAddData.next()) {
            this.tmpInfo.equipAdditions.add(new Triple(sqlAddData.getString("key"), sqlAddData.getString("subKey"), sqlAddData.getString("value")));
        }
    }

    public void initItemEquipData(ResultSet sqlEquipData) throws SQLException {
        int itemID = sqlEquipData.getInt("itemid");
        if ((this.tmpInfo == null) || (this.tmpInfo.itemId != itemID)) {
            if (!this.dataCache.containsKey(Integer.valueOf(itemID))) {
                System.out.println("[initItemEquipData] Tried to load an item while this is not in the cache: " + itemID);
                return;
            }
            this.tmpInfo = ((ItemInformation) this.dataCache.get(Integer.valueOf(itemID)));
        }

        if (this.tmpInfo.equipStats == null) {
            this.tmpInfo.equipStats = new HashMap();
        }

        int itemLevel = sqlEquipData.getInt("itemLevel");
        if (itemLevel == -1) {
            this.tmpInfo.equipStats.put(sqlEquipData.getString("key"), Integer.valueOf(sqlEquipData.getInt("value")));
        } else {
            if (this.tmpInfo.equipIncs == null) {
                this.tmpInfo.equipIncs = new HashMap();
            }

            Map toAdd = (Map) this.tmpInfo.equipIncs.get(Integer.valueOf(itemLevel));
            if (toAdd == null) {
                toAdd = new HashMap();
                this.tmpInfo.equipIncs.put(Integer.valueOf(itemLevel), toAdd);
            }
            toAdd.put(sqlEquipData.getString("key"), Integer.valueOf(sqlEquipData.getInt("value")));
        }
    }

    public void finalizeEquipData(ItemInformation item) {
        int itemId = item.itemId;

        if (item.equipStats == null) {
            item.equipStats = new HashMap();
        }

        item.eq = new Equip(itemId, (short) 0, -1, (short) 0);
        short stats = GameConstants.getStat(itemId, 0);
        if (stats > 0) {
            item.eq.setStr(stats);
            item.eq.setDex(stats);
            item.eq.setInt(stats);
            item.eq.setLuk(stats);
        }
        stats = GameConstants.getATK(itemId, 0);
        if (stats > 0) {
            item.eq.setWatk(stats);
            item.eq.setMatk(stats);
        }
        stats = GameConstants.getHpMp(itemId, 0);
        if (stats > 0) {
            item.eq.setHp(stats);
            item.eq.setMp(stats);
        }
        stats = GameConstants.getDEF(itemId, 0);
        if (stats > 0) {
            item.eq.setWdef(stats);
            item.eq.setMdef(stats);
        }
        if (item.equipStats.size() > 0) {
            for (Map.Entry stat : item.equipStats.entrySet()) {
                String key = (String) stat.getKey();

                if (key.equals("STR")) {
                    item.eq.setStr(GameConstants.getStat(itemId, ((Integer) stat.getValue()).intValue()));
                } else if (key.equals("DEX")) {
                    item.eq.setDex(GameConstants.getStat(itemId, ((Integer) stat.getValue()).intValue()));
                } else if (key.equals("INT")) {
                    item.eq.setInt(GameConstants.getStat(itemId, ((Integer) stat.getValue()).intValue()));
                } else if (key.equals("LUK")) {
                    item.eq.setLuk(GameConstants.getStat(itemId, ((Integer) stat.getValue()).intValue()));
                } else if (key.equals("PAD")) {
                    item.eq.setWatk(GameConstants.getATK(itemId, ((Integer) stat.getValue()).intValue()));
                } else if (key.equals("PDD")) {
                    item.eq.setWdef(GameConstants.getDEF(itemId, ((Integer) stat.getValue()).intValue()));
                } else if (key.equals("MAD")) {
                    item.eq.setMatk(GameConstants.getATK(itemId, ((Integer) stat.getValue()).intValue()));
                } else if (key.equals("MDD")) {
                    item.eq.setMdef(GameConstants.getDEF(itemId, ((Integer) stat.getValue()).intValue()));
                } else if (key.equals("ACC")) {
                    item.eq.setAcc((short) ((Integer) stat.getValue()).intValue());
                } else if (key.equals("EVA")) {
                    item.eq.setAvoid((short) ((Integer) stat.getValue()).intValue());
                } else if (key.equals("Speed")) {
                    item.eq.setSpeed((short) ((Integer) stat.getValue()).intValue());
                } else if (key.equals("Jump")) {
                    item.eq.setJump((short) ((Integer) stat.getValue()).intValue());
                } else if (key.equals("MHP")) {
                    item.eq.setHp(GameConstants.getHpMp(itemId, ((Integer) stat.getValue()).intValue()));
                } else if (key.equals("MMP")) {
                    item.eq.setMp(GameConstants.getHpMp(itemId, ((Integer) stat.getValue()).intValue()));
                } else if (key.equals("tuc")) {
                    item.eq.setUpgradeSlots(((Integer) stat.getValue()).byteValue());
                } else if (key.equals("Craft")) {
                    item.eq.setHands(((Integer) stat.getValue()).shortValue());
                } else if (key.equals("durability")) {
                    item.eq.setDurability(((Integer) stat.getValue()).intValue());
                } else if (key.equals("charmEXP")) {
                    item.eq.setCharmEXP(((Integer) stat.getValue()).shortValue());
                } else if (key.equals("PVPDamage")) {
                    item.eq.setPVPDamage(((Integer) stat.getValue()).shortValue());
                }
            }
            if ((item.equipStats.get("cash") != null) && (item.eq.getCharmEXP() <= 0)) {
                short exp = 0;
                int identifier = itemId / 10000;
                if ((GameConstants.isWeapon(itemId)) || (identifier == 106)) {
                    exp = 60;
                } else if (identifier == 100) {
                    exp = 50;
                } else if ((GameConstants.isAccessory(itemId)) || (identifier == 102) || (identifier == 108) || (identifier == 107)) {
                    exp = 40;
                } else if ((identifier == 104) || (identifier == 105) || (identifier == 110)) {
                    exp = 30;
                }
                item.eq.setCharmEXP(exp);
            }
        }
    }

    public void initItemInformation(ResultSet sqlItemData) throws SQLException {
        ItemInformation ret = new ItemInformation();
        int itemId = sqlItemData.getInt("itemid");
        ret.itemId = itemId;
        ret.slotMax = (GameConstants.getSlotMax(itemId) > 0 ? GameConstants.getSlotMax(itemId) : sqlItemData.getShort("slotMax"));
        ret.price = Double.parseDouble(sqlItemData.getString("price"));
        ret.wholePrice = sqlItemData.getInt("wholePrice");
        ret.stateChange = sqlItemData.getInt("stateChange");
        ret.name = sqlItemData.getString("name");
        ret.desc = sqlItemData.getString("desc");
        ret.msg = sqlItemData.getString("msg");

        ret.flag = sqlItemData.getInt("flags");

        ret.karmaEnabled = sqlItemData.getByte("karma");
        ret.meso = sqlItemData.getInt("meso");
        ret.monsterBook = sqlItemData.getInt("monsterBook");
        ret.itemMakeLevel = sqlItemData.getShort("itemMakeLevel");
        ret.questId = sqlItemData.getInt("questId");
        ret.create = sqlItemData.getInt("create");
        ret.replaceItem = sqlItemData.getInt("replaceId");
        ret.replaceMsg = sqlItemData.getString("replaceMsg");
        ret.afterImage = sqlItemData.getString("afterImage");
        ret.cardSet = 0;
        if ((ret.monsterBook > 0) && (itemId / 10000 == 238)) {
            this.mobIds.put(Integer.valueOf(ret.monsterBook), Integer.valueOf(itemId));
            for (Map.Entry set : this.monsterBookSets.entrySet()) {
                if (((List) ((Triple) set.getValue()).mid).contains(Integer.valueOf(itemId))) {
                    ret.cardSet = ((Integer) set.getKey()).intValue();
                    break;
                }
            }
        }

        String scrollRq = sqlItemData.getString("scrollReqs");
        if (scrollRq.length() > 0) {
            ret.scrollReqs = new ArrayList();
            String[] scroll = scrollRq.split(",");
            for (String s : scroll) {
                if (s.length() > 1) {
                    ret.scrollReqs.add(Integer.valueOf(Integer.parseInt(s)));
                }
            }
        }
        String consumeItem = sqlItemData.getString("consumeItem");
        if (consumeItem.length() > 0) {
            ret.questItems = new ArrayList();
            String[] scroll = scrollRq.split(",");
            for (String s : scroll) {
                if (s.length() > 1) {
                    ret.questItems.add(Integer.valueOf(Integer.parseInt(s)));
                }
            }
        }

        ret.totalprob = sqlItemData.getInt("totalprob");
        String incRq = sqlItemData.getString("incSkill");
        if (incRq.length() > 0) {
            ret.incSkill = new ArrayList();
            String[] scroll = incRq.split(",");
            for (String s : scroll) {
                if (s.length() > 1) {
                    ret.incSkill.add(Integer.valueOf(Integer.parseInt(s)));
                }
            }
        }
        this.dataCache.put(Integer.valueOf(itemId), ret);
    }

    public Equip voteitem(Equip equip) {
        short stat = 100;
        equip.setStr((short) (equip.getStr() + stat));
        equip.setDex((short) (equip.getDex() + stat));
        equip.setInt((short) (equip.getInt() + stat));
        equip.setLuk((short) (equip.getLuk() + stat));
        equip.setMatk((short) (equip.getMatk() + stat));
        equip.setWatk((short) (equip.getWatk() + stat));
        equip.setAcc((short) (equip.getAcc() + stat));
        equip.setAvoid((short) (equip.getAvoid() + stat));
        equip.setJump((short) (equip.getJump() + stat));
        equip.setHands((short) (equip.getHands() + stat));
        equip.setSpeed((short) (equip.getSpeed() + stat));
        equip.setWdef((short) (equip.getWdef() + stat));
        equip.setMdef((short) (equip.getMdef() + stat));
        equip.setHp((short) (equip.getHp() + stat));
        equip.setMp((short) (equip.getMp() + stat));
        return equip;
    }

    public Equip MSI(Equip equip, short stat) {
        final int uid = MapleInventoryIdentifier.getInstance();
        equip.setStr(stat);
        equip.setDex(stat);
        equip.setInt(stat);
        equip.setLuk(stat);
        equip.setMatk(stat);
        equip.setWatk(stat);
        equip.setAcc(stat);
        equip.setAvoid(stat);
        equip.setJump(stat);
        equip.setSpeed(stat);
        equip.setWdef(stat);
        equip.setMdef(stat);
        equip.setHp(stat);
        equip.setMp(stat);
        equip.setUpgradeSlots((byte) 0);
        equip.setViciousHammer((byte) 2);
        equip.setGiftFrom(Integer.toString(uid));
        return equip;
    }

    public boolean isMSI(Equip equip, short stat) {
        if (equip.getStr() > stat && equip.getDex() > stat && equip.getInt() > stat && equip.getLuk() > stat && equip.getMatk() > stat && equip.getWatk() > stat && equip.getAcc() > stat && equip.getAvoid() > stat && equip.getSpeed() > stat && equip.getJump() > stat && equip.getWdef() > stat && equip.getMdef() > stat && equip.getMp() > stat && equip.getHp() > stat) {
            return true;
        } else {
            return false;
        }
    }

    public Equip SRB2(Equip equip) {
        // short stat = ;
        equip.setStr((short) Math.max(3000, (Randomizer.nextInt(20000))));
        equip.setDex((short) Math.max(3000, (Randomizer.nextInt(20000))));
        equip.setInt((short) Math.max(3000, (Randomizer.nextInt(20000))));
        equip.setLuk((short) Math.max(3000, (Randomizer.nextInt(20000))));
        equip.setMatk((short) Math.max(50, (Randomizer.nextInt(200))));
        equip.setWatk((short) Math.max(50, (Randomizer.nextInt(200))));
        equip.setAcc((short) Math.max(3000, (Randomizer.nextInt(20000))));
        equip.setAvoid((short) Math.max(3000, (Randomizer.nextInt(20000))));
        equip.setJump((short) Math.max(3000, (Randomizer.nextInt(20000))));
        equip.setSpeed((short) Math.max(3000, (Randomizer.nextInt(20000))));
        equip.setWdef((short) Math.max(3000, (Randomizer.nextInt(20000))));
        equip.setMdef((short) Math.max(3000, (Randomizer.nextInt(20000))));
        equip.setHp((short) Math.max(3000, (Randomizer.nextInt(20000))));
        equip.setMp((short) Math.max(3000, (Randomizer.nextInt(20000))));
        return equip;
    }

    public Equip SRB3(Equip equip) {
        // short stat = ;
        equip.setStr((short) Math.max(20000, (Randomizer.nextInt(32767))));
        equip.setDex((short) Math.max(20000, (Randomizer.nextInt(32767))));
        equip.setInt((short) Math.max(20000, (Randomizer.nextInt(32767))));
        equip.setLuk((short) Math.max(20000, (Randomizer.nextInt(32767))));
        equip.setMatk((short) Math.max(200, (Randomizer.nextInt(500))));
        equip.setWatk((short) Math.max(200, (Randomizer.nextInt(500))));
        equip.setAcc((short) Math.max(20000, (Randomizer.nextInt(32767))));
        equip.setAvoid((short) Math.max(20000, (Randomizer.nextInt(32767))));
        equip.setJump((short) Math.max(20000, (Randomizer.nextInt(32767))));
        equip.setSpeed((short) Math.max(20000, (Randomizer.nextInt(32767))));
        equip.setWdef((short) Math.max(20000, (Randomizer.nextInt(32767))));
        equip.setMdef((short) Math.max(20000, (Randomizer.nextInt(32767))));
        equip.setHp((short) Math.max(20000, (Randomizer.nextInt(32767))));
        equip.setMp((short) Math.max(20000, (Randomizer.nextInt(32767))));
        return equip;
    }

    public boolean isEquip(int itemId) {
        return itemId / 1000000 == 1;
    }

    public Equip getStats(Equip equip, int pot1, int pot2, int pot3, int pot4, int pot5, int sock1, int sock2, int sock3) {
        equip.setStr(equip.getStr());
        equip.setDex(equip.getDex());
        equip.setInt(equip.getInt());
        equip.setLuk(equip.getLuk());
        equip.setMatk(equip.getMatk());
        equip.setWatk(equip.getWatk());
        equip.setAcc(equip.getAcc());
        equip.setAvoid(equip.getAvoid());
        equip.setJump(equip.getJump());
        equip.setHands(equip.getHands());
        equip.setSpeed(equip.getSpeed());
        equip.setWdef(equip.getWdef());
        equip.setMdef(equip.getMdef());
        equip.setHp(equip.getHp());
        equip.setMp(equip.getMp());
        equip.setPotential1(pot1);
        equip.setPotential2(pot2);
        equip.setPotential3(pot3);
        equip.setPotential4(pot4);
        equip.setPotential5(pot5);
        equip.setSocket1(sock1);
        equip.setSocket2(sock2);
        equip.setSocket3(sock3);

        return equip;
    }
}