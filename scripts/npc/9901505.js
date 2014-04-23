var points;
var status = -1;
var sel, select;
var bosspq = 682020000;
var items = [
/*Timeless Weapons*/ [[1302081, 120000], [1312037, 120000], [1322060, 120000], [1332073, 120000], [1332074, 120000], [1342011, 120000], [1362016, 120000], [1372044, 120000], [1382057, 120000], [1402046, 120000], [1412033, 120000], [1422037, 120000], [1432047, 120000], [1442063, 120000], [1452057, 120000], [1462050, 120000], [1472068, 120000], [1482023, 120000], [1492023, 120000], [1522015, 120000], [1532015, 120000]],
/*Projectiles*/ [[2070018, 60000], [2070016, 50000], [2070006, 5000], [2070005, 2500], [2330005, 5000]], //Arcane-Like
/*Other*/ [[1122017, 30000], [2340000, 150000], [2530000, 75000], [2531000, 250000], [3993002, 10000], [5221001, 1000]], // Arcane-Like
/*Abyss Armor*/ [[1003280, 150000], [1003281, 150000], [1003282, 150000], [1003283, 150000], [1003284, 150000], [1052374, 150000], [1052375, 150000], [1052376, 150000], [1052377, 150000], [1052378, 150000], [1072544, 150000], [1072545, 150000], [1072546, 150000], [1072547, 150000], [1072548, 150000], [1082328, 150000], [1082329, 150000], [1082330, 150000], [1082331, 150000], [1082332, 150000]],
/*Abyss Weapons*/ [[1302173, 180000], [1312072, 180000], [1322107, 180000], [1332148, 180000], [1332149, 180000], [1342040, 180000], [1362022, 180000], [1372100, 180000], [1382124, 180000], [1402111, 180000], [1412071, 180000], [1422073, 180000], [1432099, 180000], [1442136, 180000], [1452129, 180000], [1462118, 180000], [1472141, 180000], [1482102, 180000], [1492101, 180000], [1522020, 180000], [1532037, 180000]],
/*Fearless Armor*/ [[1003285, 200000], [1003286, 200000], [1003287, 200000], [1003288, 200000], [1003289, 200000], [1052379, 200000], [1052380, 200000], [1052381, 200000], [1052382, 200000], [1052383, 200000], [1072549, 200000], [1072550, 200000], [1072551, 200000], [1072552, 200000], [1072553, 200000], [1082333, 200000], [1082334, 200000], [1082335, 200000], [1082336, 200000], [1082337, 200000], [1102311, 200000], [1032108, 200000], [1122148, 200000], [1092092, 200000], [1092093, 200000], [1092094, 200000]],
/*Fearless Weapons*/ [[1302174, 250000], [1312073, 250000], [1322108, 250000], [1332150, 250000], [1332151, 250000], [1342041, 250000], [1362023, 250000], [1372101, 250000], [1382125, 250000], [1402112, 250000], [1412072, 250000], [1422074, 250000], [1432100, 250000], [1442137, 250000], [1452130, 250000], [1462119, 250000], [1472142, 250000], [1482103, 250000], [1492102, 250000], [1522021, 250000], [1532038, 250000]]
]; //id, price

function start() {
    status = -1;
    action(1, 0, 0);
} 

function action(mode, type, selection) {
    var record = cm.getQuestRecord(150001);
    var intPoints = parseInt(points);
    if (mode == 1)
        status++;
    else {
        cm.dispose();
        return;
    }
    if (status == 0) {
        if (cm.getMapId() == 219000000) {
            cm.sendSimple("#b#L40##m219010000##l\r\n#L41##m219020000##l");
            cm.dispose();
            return;
        }
        points = record.getCustomData() == null ? "0" : record.getCustomData();
        //cm.sendSimple("Would you like to have a taste of a relentless boss battle?\r\n\r\n #b#k \r\n #b#L0#Warp to Lobby#l  \r\r\n\n#fUI/UIWindow.img/QuestIcon/4/0# \r\n #b#L32#Trade points(1000) for Enchanted Scroll#l#k  \r\n #b#L33#Trade Enchanted Scroll for points(1000)#l#k \r\n #b#L36#Trade points(10000) for Bamboo Luck Sack#l#k  \r\n #b#L37#Trade Bamboo Luck Sack for points(10000)#l#k \r\n #b#L4##i1492023:#Trade 120,000 points (Timeless Blindness)#l#k \r\n #b#L5##i1472068:#Trade 120,000 points (Timeless Lampion)#l#k \r\n #b#L6##i1462050:#Trade 120,000 points (Timeless Black Beauty)#l#k \r\n #b#L7##i1452057:#Trade 120,000 points (Timeless Engaw)#l#k \r\n #b#L8##i1432047:#Trade 120,000 points (Timeless Alchupiz)#l#k \r\n #b#L9##i1382057:#Trade 120,000 points (Timeless Aeas Hand)#l#k \r\n #b#L10##i1372044:#Trade 120,000 points (Timeless Enreal Tear)#l#k \r\n #b#L11##i1332074:#Trade 120,000 points (Timeless Killic)#l#k \r\n #b#L12##i1332073:#Trade 120,000 points (Timeless Pescas)#l#k \r\n #b#L13##i1482023:#Trade 120,000 points (Timeless Equinox)#l#k \r\n #b#L14##i1442063:#Trade 120,000 points (Timeless Diesra)#l#k \r\n #b#L15##i1422037:#Trade 120,000 points (Timeless Bellocce)#l#k \r\n #b#L16##i1412033:#Trade 120,000 points (Timeless Tabarzin)#l#k \r\n #b#L17##i1402046:#Trade 120,000 points (Timeless Nibleheim)#l#k \r\n #b#L18##i1322060:#Trade 120,000 points (Timeless Allargando)#l#k \r\n #b#L19##i1312037:#Trade 120,000 points (Timeless Bardiche)#l#k \r\n #b#L20##i1302081:#Trade 120,000 points (Timeless Executioners)#l#k \r\n #b#L31##i1342011:#Trade 120,000 points (Timeless Katara)#l#k" + (cm.isGMS() ? "\r\n #b#L34##i1532015:#Trade 120,000 points (The Obliterator)#l#k" : "") + "\r\n #b#L21##i2070018:#Trade 125,000 points (Balanced Fury)#l#k \r\n #b#L35##i2070016:#Trade 75,000 points (Crystal Ilbi)#l#k \r\n #b#L22# #i1122017:#Trade 30,000 points (Fairy Pendant, lasts 1 day)#l#k \r\n #b#L27##i2340000:#Trade 75,000 points (White Scroll)#l#k \r\n #b#L29##i5490001:#Trade 15,000 points (Silver Key)#l#k \r\n #b#L30##i5490000:#Trade 30,000 points (Gold Key)#l\r\n #b#L38##i2530000:#Trade 75,000 points (Lucky Day)#l \r\n #b#L39##i2531000:#Trade 150,000 points (Protection Scroll)#l#k");
        cm.sendSimple("Would you like to have a taste of a relentless boss battle?#b\r\nCurrent points: " + points + "\r\n#L0#Warp to Lobby#l\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#L1#Trade Enchanted Scroll for points(1000)#l\r\n#L2#Trade Bamboo Luck Sack for points(10000)#l\r\n#L3#Timeless Weapons#l\r\n#L4#Projectiles#l\r\n#L5#Other#l\r\n#L6#Abyss Armors#l\r\n#L7#Abyss Weapons#l\r\n#L8#Fearless Armors#l\r\n#L9#Fearless Weapons#l\r\n ");
    } else if (status == 1) {
        select = selection;
        switch (selection) {
            case 0:
                cm.warp(bosspq);
                cm.dispose();
                break;
            case 1:
                cm.sendGetNumber("How many Enchanted Scrolls would you like to trade?\r\n", cm.itemQuantity(5221001), 1, 100);
                break;
            case 2:
                cm.sendGetNumber("How many Bamboo Luck Sacks would you like to trade?\r\n", cm.itemQuantity(3993002), 1, 100);
                break;
            case 3:
                var timelesswep = "Choose from any of the items below:#b";
                for (var i = 0; i < items[0].length; i++)
                    timelesswep += "\r\n#L" + i + "##i" + items[0][i][0] + "# Trade " + items[0][i][1] + " points (#z" + items[0][i][0] + "#)#l";
                timelesswep += "\r\n "
                cm.sendSimple(timelesswep);
                break;
            case 4:
                var projectiles = "Choose from any of the items below:#b";
                for (var i = 0; i < items[1].length; i++)
                    projectiles += "\r\n#L" + i + "##i" + items[1][i][0] + "# Trade " + items[1][i][1] + " points (#z" + items[1][i][0] + "#)#l";
                projectiles += "\r\n "
                cm.sendSimple(projectiles);
                break;
            case 5:
                var other = "Choose from any of the items below:#b";
                for (var i = 0; i < items[2].length; i++)
                    other += "\r\n#L" + i + "##i" + items[2][i][0] + "# Trade " + items[2][i][1] + " points (#z" + items[2][i][0] + "#)#l";
                other += "\r\n "
                cm.sendSimple(other);
                break;
            case 6:
                var abyssarmor = "Choose from any of the items below:#b";
                for (var i = 0; i < items[3].length; i++)
                    abyssarmor += "\r\n#L" + i + "##i" + items[3][i][0] + "# Trade " + items[3][i][1] + " points (#z" + items[3][i][0] + "#)#l";
                abyssarmor += "\r\n "
                cm.sendSimple(abyssarmor);
                break;
            case 7:
                var abysswep = "Choose from any of the items below:#b";
                for (var i = 0; i < items[4].length; i++)
                    abysswep += "\r\n#L" + i + "##i" + items[4][i][0] + "# Trade " + items[4][i][1] + " points (#z" + items[4][i][0] + "#)#l";
                abysswep += "\r\n "
                cm.sendSimple(abysswep);
                break;
            case 8:
                var feararmor = "Choose from any of the items below:#b";
                for (var i = 0; i < items[5].length; i++)
                    feararmor += "\r\n#L" + i + "##i" + items[5][i][0] + "# Trade " + items[5][i][1] + " points (#z" + items[5][i][0] + "#)#l";
                feararmor += "\r\n "
                cm.sendSimple(feararmor);
                break;
            case 9:
                var fearwep = "Choose from any of the items below:#b";
                for (var i = 0; i < items[6].length; i++)
                    fearwep += "\r\n#L" + i + "##i" + items[6][i][0] + "# Trade " + items[6][i][1] + " points (#z" + items[6][i][0] + "#)#l";
                fearwep += "\r\n "
                cm.sendSimple(fearwep);
                break;
        }
    } else if (status == 2) {
        sel = selection;
        if (select == 1) {
            if (cm.haveItem(5221001, selection)) {
                intPoints += (1000 * cm.itemQuantity(5221001));
                record.setCustomData(""+intPoints+"");
                cm.gainItem(5221001, -selection);
                cm.sendOk("Enjoy your rewards :P");
            } else {
                cm.sendOk("Please check if you have sufficient item, or inventory slot for it.");
            }
        } else if (select == 2) {
            if (cm.haveItem(3993002, selection)) {
                intPoints += (1000 * cm.itemQuantity(3993002));
                record.setCustomData(""+intPoints+"");
                cm.gainItem(3993002, -selection);
                cm.sendOk("Enjoy your rewards :P");
            } else {
                cm.sendOk("Please check if you have sufficient item, or inventory slot for it.");
            }
        } else if (select == 3) {
            if (intPoints >= items[0][sel][1] && cm.canHold(items[0][sel][0])) {
                intPoints -= items[0][sel][1];
                record.setCustomData(""+intPoints+"");
                cm.gainItem(items[0][sel][0], 1);
                cm.sendOk("Enjoy your rewards :P");
            } else {
                cm.sendOk("Please check if you have sufficient points, or inventory slot for it. #bCurrent Points : " + points);
            }
        } else if (select == 4) {
            if (intPoints >= items[1][sel][1] && cm.canHold(items[1][sel][0])) {
                intPoints -= items[1][sel][1];
                record.setCustomData(""+intPoints+"");
                cm.gainItem(items[1][sel][0], (sel == 4 ? 3000 : 1000)); // 3000 for bullets, they're unrechargable
                cm.sendOk("Enjoy your rewards :P");
            } else {
                cm.sendOk("Please check if you have sufficient points, or inventory slot for it. #bCurrent Points : " + points);
            }
        } else if (select == 5) {
            if (intPoints >= items[2][sel][1] && cm.canHold(items[2][sel][0])) {
                intPoints -= items[2][sel][1];
                record.setCustomData(""+intPoints+"");
                cm.gainItem(items[2][sel][0], 1);
                cm.sendOk("Enjoy your rewards :P");
            } else {
                cm.sendOk("Please check if you have sufficient points, or inventory slot for it. #bCurrent Points : " + points);
            }
        } else if (select == 6) {
            if (intPoints >= items[3][sel][1] && cm.canHold(items[3][sel][0])) {
                intPoints -= items[3][sel][1];
                record.setCustomData(""+intPoints+"");
                cm.gainItem(items[3][sel][0], 1);
                cm.sendOk("Enjoy your rewards :P");
            } else {
                cm.sendOk("Please check if you have sufficient points, or inventory slot for it. #bCurrent Points : " + points);
            }
        } else if (select == 7) {
            if (intPoints >= items[4][sel][1] && cm.canHold(items[4][sel][0])) {
                intPoints -= items[4][sel][1];
                record.setCustomData(""+intPoints+"");
                cm.gainItem(items[4][sel][0], 1);
                cm.sendOk("Enjoy your rewards :P");
            } else {
                cm.sendOk("Please check if you have sufficient points, or inventory slot for it. #bCurrent Points : " + points);
            }
        } else if (select == 8) {
            if (intPoints >= items[5][sel][1] && cm.canHold(items[5][sel][0])) {
                intPoints -= items[5][sel][1];
                record.setCustomData(""+intPoints+"");
                cm.gainItem(items[5][sel][0], 1);
                cm.sendOk("Enjoy your rewards :P");
            } else {
                cm.sendOk("Please check if you have sufficient points, or inventory slot for it. #bCurrent Points : " + points);
            }
        } else if (select == 9) {
            if (intPoints >= items[6][sel][1] && cm.canHold(items[6][sel][0])) {
                intPoints -= items[6][sel][1];
                record.setCustomData(""+intPoints+"");
                cm.gainItem(items[6][sel][0], 1);
                cm.sendOk("Enjoy your rewards :P");
            } else {
                cm.sendOk("Please check if you have sufficient points, or inventory slot for it. #bCurrent Points : " + points);
            }
        }
        cm.dispose();
    }
}