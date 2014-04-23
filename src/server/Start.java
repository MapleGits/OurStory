package server;

import clientside.SkillFactory;
import client.inventory.MapleInventoryIdentifier;
import database.DatabaseConnection;
import handling.MapleServerHandler;
import handling.cashshop.CashShopServer;
import handling.cashshop.handler.CashShopOperation;
import handling.channel.ChannelServer;
import handling.channel.MapleGuildRanking;
import handling.login.LoginInformationProvider;
import handling.login.LoginServer;
import handling.world.World;
import handling.world.family.MapleFamily;
import handling.world.guild.MapleGuild;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import server.life.MapleLifeFactory;
import server.life.MapleMonsterInformationProvider;
import server.life.MobSkillFactory;
import server.life.PlayerNPC;
import server.maps.MapleMapFactory;
import server.quest.MapleQuest;

public class Start {

    public static long startTime = System.currentTimeMillis();
    public static final Start instance = new Start();
    public static AtomicInteger CompletedLoadingThreads = new AtomicInteger(0);

    public static String nexonip = "8.31.99.141";

    public void run()
            throws InterruptedException {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET loggedin = 0,SessionIP=''");
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            throw new RuntimeException("[EXCEPTION] Please check if the SQL server is active.");
        }

   
        World.init();
        Timer.WorldTimer.getInstance().start();
        Timer.EtcTimer.getInstance().start();
        Timer.MapTimer.getInstance().start();
        Timer.CloneTimer.getInstance().start();
        Timer.EventTimer.getInstance().start();
        Timer.BuffTimer.getInstance().start();
        Timer.PingTimer.getInstance().start();
        System.out.println("Loader 1...");
        MapleGuildRanking.getInstance().load();
        MapleGuild.loadAll();
        System.out.println("Loader 2...");

        MapleFamily.loadAll();
        System.out.println("Loader 3...");
        MapleLifeFactory.loadQuestCounts();
        MapleQuest.initQuests();
        System.out.println("Loader 4...");
        MapleItemInformationProvider.getInstance().runEtc();
        System.out.println("Loader 5...");
        MapleMonsterInformationProvider.getInstance().load();

        System.out.println("Loader 6...");
        MapleItemInformationProvider.getInstance().runItems();
        System.out.println("Loader 7...");
        SkillFactory.load();
        System.out.println("Loader 8...");
        LoginInformationProvider.getInstance();
        RandomRewards.load();
        MapleCarnivalFactory.getInstance();
        CharacterCardFactory.getInstance().initialize();
        MobSkillFactory.getInstance();
        SpeedRunner.loadSpeedRuns();
        MTSStorage.load();
        System.out.println("Loader 9...");
        MapleInventoryIdentifier.getInstance();
        MapleMapFactory.loadCustomLife();
        System.out.println("Loader 10...");
        CashItemFactory.getInstance().initialize();

        CashShopOperation.runCashItems();
        MapleServerHandler.initiate();
        System.out.println("[Loading Login]");
        LoginServer.run_startup_configurations();
        System.out.println("[Login Initialized]");

        System.out.println("[Loading Channel]");
        ChannelServer.startChannel_Main();
        System.out.println("[Channel Initialized]");

        System.out.println("[Loading CS]");
        CashShopServer.run_startup_configurations();
        System.out.println("[CS Initialized]");

        World.registerRespawn();


        MapleMonsterInformationProvider.getInstance().addExtra();
        PlayerNPC.loadAll();
        LoginServer.setOn();

        System.out.println("[Fully Initialized in " + (System.currentTimeMillis() - startTime) / 1000L + " seconds]");
    }

    public static void main(String[] args)
            throws InterruptedException {
        instance.run();
    }

}