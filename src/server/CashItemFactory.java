package server;

import database.DatabaseConnection;
import java.io.File;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import server.CashItemInfo.CashModInfo;

public class CashItemFactory {

    private static final CashItemFactory instance = new CashItemFactory();
    private static final int[] bestItems = {10003055, 10003090, 10103464, 10002960, 10103363};
    private final Map<Integer, CashItemInfo> itemStats = new HashMap();
    private final Map<Integer, List<Integer>> itemPackage = new HashMap();
    private final Map<Integer, List<Integer>> itemPackage2 = new HashMap();
    private final Map<Integer, CashItemInfo.CashModInfo> itemMods = new HashMap();
    private final Map<Integer, List<Integer>> openBox = new HashMap();
    private final MapleDataProvider data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/Etc.wz"));

    public static final CashItemFactory getInstance() {
        return instance;
    }

    public void initialize() {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM cashshop_item");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int SN = rs.getInt("sn");
                CashItemInfo stats = new CashItemInfo(rs.getInt("itemid"), rs.getInt("quantity"), rs.getInt("price_new"), SN, rs.getInt("day"), rs.getInt("gender"), true);
                if (SN > 0) {
                    this.itemStats.put(SN, stats);
                }
                if (rs.getInt("pack") > 0) {



                    List packageItems = new ArrayList();
                    List realItems = new ArrayList();

                    for (int i = 0; i < rs.getInt("pack"); i++) {
                        String[] packhead = rs.getString("packhead").split(",");
                        String[] realhead = rs.getString("realitems").split(",");
                        packageItems.add(Integer.valueOf(packhead[(i) * 5]));
                        realItems.add(Integer.valueOf(realhead[(i)]));


                    }
                    itemPackage2.put(SN, realItems);
                    itemPackage.put(SN, packageItems);

                }
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            System.out.println(e);
        }

        List availableSN = new LinkedList();
        availableSN.add(Integer.valueOf(20001141));
        availableSN.add(Integer.valueOf(20001142));
        availableSN.add(Integer.valueOf(20001143));
        availableSN.add(Integer.valueOf(20001144));
        availableSN.add(Integer.valueOf(20001145));
        availableSN.add(Integer.valueOf(20001146));
        availableSN.add(Integer.valueOf(20001147));
        this.openBox.put(Integer.valueOf(5533003), availableSN);

        availableSN = new LinkedList();
        availableSN.add(Integer.valueOf(20000462));
        availableSN.add(Integer.valueOf(20000463));
        availableSN.add(Integer.valueOf(20000464));
        availableSN.add(Integer.valueOf(20000465));
        availableSN.add(Integer.valueOf(20000466));
        availableSN.add(Integer.valueOf(20000467));
        availableSN.add(Integer.valueOf(20000468));
        availableSN.add(Integer.valueOf(20000469));
        this.openBox.put(Integer.valueOf(5533000), availableSN);

        availableSN = new LinkedList();
        availableSN.add(Integer.valueOf(20800259));
        availableSN.add(Integer.valueOf(20800260));
        availableSN.add(Integer.valueOf(20800263));
        availableSN.add(Integer.valueOf(20800264));
        availableSN.add(Integer.valueOf(20800265));
        availableSN.add(Integer.valueOf(20800267));
        this.openBox.put(Integer.valueOf(5533001), availableSN);

        availableSN = new LinkedList();
        availableSN.add(Integer.valueOf(20800270));
        availableSN.add(Integer.valueOf(20800271));
        availableSN.add(Integer.valueOf(20800272));
        availableSN.add(Integer.valueOf(20800273));
        availableSN.add(Integer.valueOf(20800274));
        this.openBox.put(Integer.valueOf(5533002), availableSN);
    }

    public final CashItemInfo getSimpleItem(int sn) {
        return (CashItemInfo) this.itemStats.get(Integer.valueOf(sn));
    }

    public final CashItemInfo getItem(int sn) {
        final CashItemInfo stats = itemStats.get(Integer.valueOf(sn));
        final CashModInfo z = getModInfo(sn);
        if (z != null && z.showUp) {
            return z.toCItem(stats); //null doesnt matter
        }
        if (stats == null || !stats.onSale()) {
            return null;
        }
        //hmm
        return stats;
    }

    public final List<Integer> getPackageItems(int itemId) {
        return itemPackage.get(itemId);
    }

    public final List<Integer> getPackageItems2(int itemId) {
        return itemPackage2.get(itemId);
    }

    public final CashItemInfo.CashModInfo getModInfo(int sn) {
        return (CashItemInfo.CashModInfo) this.itemMods.get(Integer.valueOf(sn));
    }

    public final Collection<CashItemInfo.CashModInfo> getAllModInfo() {
        return this.itemMods.values();
    }

    public final Map<Integer, List<Integer>> getRandomItemInfo() {
        return this.openBox;
    }

    public final int[] getBestItems() {
        return bestItems;
    }
}