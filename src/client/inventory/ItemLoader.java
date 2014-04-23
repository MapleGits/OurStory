package client.inventory;

import constants.GameConstants;
import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import server.MapleItemInformationProvider;
import tools.Pair;

public enum ItemLoader {

    INVENTORY("inventoryitems", "inventoryequipment", 0, "characterid"),
    STORAGE("inventoryitems", "inventoryequipment", 1, "accountid"),
    CASHSHOP("csitems", "csequipment", 2, "accountid"),
    HIRED_MERCHANT("hiredmerchitems", "hiredmerchequipment", 5, "packageid"),
    DUEY("dueyitems", "dueyequipment", 6, "packageid"),
    MTS("mtsitems", "mtsequipment", 8, "packageid"),
    MTS_TRANSFER("mtstransfer", "mtstransferequipment", 9, "characterid");
    private int value;
    private String table;
    private String table_equip;
    private String arg;

    private ItemLoader(String table, String table_equip, int value, String arg) {
        this.table = table;
        this.table_equip = table_equip;
        this.value = value;
        this.arg = arg;
    }

    public int getValue() {
        return this.value;
    }

    public Map<Long, Pair<Item, MapleInventoryType>> loadItems(boolean login, int id) throws SQLException {
        Map items = new LinkedHashMap();
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM `");
        query.append(this.table);
        query.append("` LEFT JOIN `");
        query.append(this.table_equip);
        query.append("` USING(`inventoryitemid`) WHERE `type` = ?");
        query.append(" AND `");
        query.append(this.arg);
        query.append("` = ?");

        if (login) {
            query.append(" AND `inventorytype` = ");
            query.append(MapleInventoryType.EQUIPPED.getType());
        }

        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(query.toString());
        ps.setInt(1, this.value);
        ps.setInt(2, id);
        ResultSet rs = ps.executeQuery();
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        while (rs.next()) {
            if (ii.itemExists(rs.getInt("itemid"))) {
                MapleInventoryType mit = MapleInventoryType.getByType(rs.getByte("inventorytype"));

                if ((mit.equals(MapleInventoryType.EQUIP)) || (mit.equals(MapleInventoryType.EQUIPPED))) {
                    Equip equip = new Equip(rs.getInt("itemid"), rs.getShort("position"), rs.getInt("uniqueid"), rs.getShort("flag"));
                    if ((!login) && (equip.getPosition() != -55)) {
                        equip.setQuantity((short) 1);
                        equip.setInventoryId(rs.getLong("inventoryitemid"));
                        equip.setOwner(rs.getString("owner"));
                        equip.setExpiration(rs.getLong("expiredate"));
                        equip.setUpgradeSlots(rs.getByte("upgradeslots"));
                        equip.setLevel(rs.getByte("level"));
                        equip.setStr(rs.getShort("str"));
                        equip.setDex(rs.getShort("dex"));
                        equip.setInt(rs.getShort("int"));
                        equip.setLuk(rs.getShort("luk"));
                        equip.setHp(rs.getShort("hp"));
                        equip.setMp(rs.getShort("mp"));
                        equip.setWatk(rs.getShort("watk"));
                        equip.setMatk(rs.getShort("matk"));
                        equip.setWdef(rs.getShort("wdef"));
                        equip.setMdef(rs.getShort("mdef"));
                        equip.setAcc(rs.getShort("acc"));
                        equip.setAvoid(rs.getShort("avoid"));
                        equip.setHands(rs.getShort("hands"));
                        equip.setSpeed(rs.getShort("speed"));
                        equip.setJump(rs.getShort("jump"));
                        equip.setViciousHammer(rs.getByte("ViciousHammer"));
                        equip.setItemEXP(rs.getInt("itemEXP"));
                        equip.setGMLog(rs.getString("GM_Log"));
                        equip.setDurability(rs.getInt("durability"));
                        equip.setEnhance(rs.getByte("enhance"));
                        equip.setPotential1(rs.getInt("potential1"));
                        equip.setPotential2(rs.getInt("potential2"));
                        equip.setPotential3(rs.getInt("potential3"));
                        equip.setPotential4(rs.getInt("potential4"));
                        equip.setPotential5(rs.getInt("potential5"));
                  //      equip.setFusionAnvil(rs.getInt("fusionAnvil"));
                        equip.setSocket1(rs.getInt("socket1"));
                        equip.setSocket2(rs.getInt("socket2"));
                        equip.setSocket3(rs.getInt("socket3"));
                        equip.setGiftFrom(rs.getString("sender"));
                        equip.setIncSkill(rs.getInt("incSkill"));
                        equip.setPVPDamage(rs.getShort("pvpDamage"));
                        equip.setCharmEXP(rs.getShort("charmEXP"));
                        if (equip.getCharmEXP() < 0) {
                            equip.setCharmEXP(((Equip) ii.getEquipById(equip.getItemId())).getCharmEXP());
                        }
                        if (equip.getUniqueId() > -1) {
                            if (GameConstants.isEffectRing(rs.getInt("itemid"))) {
                                MapleRing ring = MapleRing.loadFromDb(equip.getUniqueId(), mit.equals(MapleInventoryType.EQUIPPED));
                                if (ring != null) {
                                    equip.setRing(ring);
                                }
                            } else if (equip.getItemId() / 10000 == 166) {
                                MapleAndroid ring = MapleAndroid.loadFromDb(equip.getItemId(), equip.getUniqueId());
                                if (ring != null) {
                                    equip.setAndroid(ring);
                                }
                            }
                        }
                    }
                    items.put(Long.valueOf(rs.getLong("inventoryitemid")), new Pair(equip.copy(), mit));
                } else {
                    Item item = new Item(rs.getInt("itemid"), rs.getShort("position"), rs.getShort("quantity"), rs.getShort("flag"), rs.getInt("uniqueid"));
                    item.setOwner(rs.getString("owner"));
                    item.setInventoryId(rs.getLong("inventoryitemid"));
                    item.setExpiration(rs.getLong("expiredate"));
                    item.setGMLog(rs.getString("GM_Log"));
                    item.setGiftFrom(rs.getString("sender"));
                    if (GameConstants.isPet(item.getItemId())) {
                        if (item.getUniqueId() > -1) {
                            MaplePet pet = MaplePet.loadFromDb(item.getItemId(), item.getUniqueId(), item.getPosition());
                            if (pet != null) {
                                item.setPet(pet);
                            }
                        } else {
                            item.setPet(MaplePet.createPet(item.getItemId(), MapleInventoryIdentifier.getInstance()));
                        }
                    }
                    items.put(Long.valueOf(rs.getLong("inventoryitemid")), new Pair(item.copy(), mit));
                }
            }
        }
        rs.close();
        ps.close();
        return items;
    }

    public void saveItems(List<Pair<Item, MapleInventoryType>> items, int id) throws SQLException {
        saveItems(items, DatabaseConnection.getConnection(), id);
    }

    public void saveItems(List<Pair<Item, MapleInventoryType>> items, Connection con, int id) throws SQLException {
        StringBuilder query = new StringBuilder();
        query.append("DELETE FROM `");
        query.append(this.table);
        query.append("` WHERE `type` = ? AND `");
        query.append(this.arg);
        query.append("` = ?");

        PreparedStatement ps = con.prepareStatement(query.toString());
        ps.setInt(1, this.value);
        ps.setInt(2, id);
        ps.executeUpdate();
        ps.close();
        if (items == null) {
            return;
        }
        StringBuilder query_2 = new StringBuilder("INSERT INTO `");
        query_2.append(this.table);
        query_2.append("` (");
        query_2.append(this.arg);
        query_2.append(", itemid, inventorytype, position, quantity, owner, GM_Log, uniqueid, expiredate, flag, `type`, sender) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        ps = con.prepareStatement(query_2.toString(), 1);
        PreparedStatement pse = con.prepareStatement(new StringBuilder().append("INSERT INTO ").append(this.table_equip).append(" VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)").toString());
        Iterator iter = items.iterator();

        while (iter.hasNext()) {
            Pair pair = (Pair) iter.next();
            Item item = (Item) pair.getLeft();
            MapleInventoryType mit = (MapleInventoryType) pair.getRight();
            if (item.getPosition() != -55) {
                ps.setInt(1, id);
                ps.setInt(2, item.getItemId());
                ps.setInt(3, mit.getType());
                ps.setInt(4, item.getPosition());
                ps.setInt(5, item.getQuantity());
                ps.setString(6, item.getOwner());
                ps.setString(7, item.getGMLog());
                if (item.getPet() != null) {
                    ps.setInt(8, Math.max(item.getUniqueId(), item.getPet().getUniqueId()));
                } else {
                    ps.setInt(8, item.getUniqueId());
                }

                ps.setLong(9, item.getExpiration());
                ps.setShort(10, item.getFlag());
                ps.setByte(11, (byte) this.value);
                ps.setString(12, item.getGiftFrom());

                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();

                if (!rs.next()) {
                    rs.close();
                } else {
                    long iid = rs.getLong(1);
                    rs.close();

                    item.setInventoryId(iid);
                    if ((mit.equals(MapleInventoryType.EQUIP)) || (mit.equals(MapleInventoryType.EQUIPPED))) {
                        Equip equip = (Equip) item;
                        pse.setLong(1, iid);
                        pse.setInt(2, equip.getUpgradeSlots());
                        pse.setInt(3, equip.getLevel());
                        pse.setInt(4, equip.getStr());
                        pse.setInt(5, equip.getDex());
                        pse.setInt(6, equip.getInt());
                        pse.setInt(7, equip.getLuk());
                        pse.setInt(8, equip.getHp());
                        pse.setInt(9, equip.getMp());
                        pse.setInt(10, equip.getWatk());
                        pse.setInt(11, equip.getMatk());
                        pse.setInt(12, equip.getWdef());
                        pse.setInt(13, equip.getMdef());
                        pse.setInt(14, equip.getAcc());
                        pse.setInt(15, equip.getAvoid());
                        pse.setInt(16, equip.getHands());
                        pse.setInt(17, equip.getSpeed());
                        pse.setInt(18, equip.getJump());
                        pse.setInt(19, equip.getViciousHammer());
                        pse.setInt(20, equip.getItemEXP());
                        pse.setInt(21, equip.getDurability());
                        pse.setByte(22, equip.getEnhance());
                        pse.setInt(23, equip.getPotential1());
                        pse.setInt(24, equip.getPotential2());
                        pse.setInt(25, equip.getPotential3());
                        pse.setInt(26, equip.getPotential4());
                        pse.setInt(27, equip.getPotential5());
                        pse.setInt(28, equip.getSocket1());
                        pse.setInt(29, equip.getSocket2());
                        pse.setInt(30, equip.getSocket3());
                        pse.setInt(31, equip.getIncSkill());
                        pse.setShort(32, equip.getCharmEXP());
                        pse.setShort(33, equip.getPVPDamage());
                //        pse.setInt(33, equip.getFusionAnvil());
                        pse.executeUpdate();
                    }
                }
            }
        }
        pse.close();
        ps.close();
    }
}