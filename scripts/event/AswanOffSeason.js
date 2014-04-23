importPackage(Packages.tools.packet);
importPackage(Packages.server.life);
importPackage(java.lang);
importPackage(java.awt);
importPackage(Packages.tools.RandomStream);
importPackage(Packages.main.world);
importPackage(Packages.tools.packet);


function init() {
    
}

function setup(eim) {
    var a = Randomizer.nextInt();
    while (em.getInstance("AswanOffSeason") != null) {
        a = Randomizer.nextInt();
    }
    var eim = em.newInstance("AswanOffSeason");
    return eim;
}

function playerEntry(eim, player) {
    var map = Integer.parseInt(eim.getProperty("Global_StartMap"));
    player.changeMap(eim.getMapFactory().getMap(map), eim.getMapFactory().getMap(map).getPortal("sp"));
    //player.dropMessage(6, "[Hilla's Gang Liberation] Watch out, for they are powerful!");
}



function changedMap(eim, player, mapid) {
    if (mapid != 955000100 && mapid != 955000200 && mapid != 955000300) {
        eim.unregisterPlayerAzwan(player);
    }
}

function scheduledTimeout(eim) {
    
    var exit = em.getChannelServer().getMapFactory().getMap(Integer.parseInt(eim.getProperty("Global_ExitMap")));
    var it = eim.getPlayers().iterator();
    while (it.hasNext()) {
        var chr = it.next();
        if (chr == null) {
            System.out.println("Character is null.!");
        }
        if (exit == null) {
            System.out.println("Map exist is null!");
        }
        if (exit.getPortal("sp") == null) {
            System.out.println("Map portal is null!");
        }
        chr.changeMap(exit, exit.getPortal("sp"));
        //chr.dropMessage(6, "[Hilla's Gang Liberation] You have failed to defeat her gang in the time limit.");
    }
    eim.unregisterAll();
    if (eim != null) {
        eim.dispose();
    }
}

function allMonstersDead(eim) {
    var startmap = Integer.parseInt(eim.getProperty("Global_StartMap"));
    var curstage = Integer.parseInt(eim.getProperty("CurrentStage"));
    var curmap = (startmap + ((curstage - 1) * 100));
    var map = eim.getMapFactory().getMap(curmap);
    if (map == 955000300) {
        eim.broadcastPacket(CField.showEffect("aswan/clear"));
        //eim.broadcastPlayerMsg(6, "[Hilla's Gang Liberation] Enter the portal to claim your reward!");
    } else {
        eim.broadcastPacket(CWvsContext.enableActions());
    }
    eim.broadcastPacket(CField.showEffect("aswan/clear"));
    //eim.broadcastPlayerMsg(6, "[Hilla's Gang Liberation] Please enter the portal to procced.");
    eim.setProperty("CurrentStage", (curstage + 1)+"");
}

function playerDead(eim, player) {
    return 0;
}

function playerRevive(eim, player) {
    
}

function playerDisconnected(eim, player) {
    if (eim.getProperty("Global_MinPerson") == null) {
        return -1;
    }
    return -Integer.parseInt(eim.getProperty("Global_MinPerson"));
}

function monsterValue(eim, mobid) {
    return 1;
}

function leftParty(eim, player) {
    if (eim.getPlayerCount() < Integer.parseInt(eim.getProperty("Global_MinPerson"))) {
        var exit = em.getChannelServer().getMapFactory().getMap(Integer.parseInt(eim.getProperty("Global_ExitMap")));
        var it = eim.getPlayers().iterator();
        while (it.hasNext()) {
            var chr = it.next();
            chr.changeMap(exit, exit.getPortal(0));
            //chr.Message("not done yet");
        }
        eim.unregisterAll();
        if (eim != null) {
            eim.dispose();
        }
    }
    
}

function disbandParty(eim) {
    var exit = eim.getPlayers().get(0).getClient().getChannelServer().getMapFactory().getMap(Integer.parseInt(eim.getProperty("Global_ExitMap")));
    var it = eim.getPlayers().iterator();
    while (it.hasNext()) {
        var chr = it.next();
        chr.changeMap(exit, exit.getPortal(0));
        //chr.Message("zz");
    }
    eim.unregisterAll();
    if (eim != null) {
        eim.dispose();
    }
}

function clearPQ(eim) {
    
}

function playerExit(eim, player) {
    var exit = eim.getPlayers().get(0).getClient().getChannelServer().getMapFactory().getMap(Integer.parseInt(eim.getProperty("Global_ExitMap")));
    var it = eim.getPlayers().iterator();
    while (it.hasNext()) {
        var chr = it.next();
        chr.changeMap(exit, exit.getPortal(0));
        //chr.Message("zz");
    }
    eim.unregisterAll();
    if (eim != null) {
        eim.dispose();
    }
}

function onMapLoad(eim, player) {
    
}

function cancelSchedule(a) {
    
}