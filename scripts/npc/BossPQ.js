var points;
var status = -1;
var sel, select;
var bosspq = 682020000;
var items = [
/*Timeless Weapons*/ [[1302081, 120000], [1312037, 120000], [1322060, 120000], [1332073, 120000], [1332074, 120000], [1342011, 120000], [1362016, 120000], [1372044, 120000], [1382057, 120000], [1402046, 120000], [1412033, 120000], [1422037, 120000], [1432047, 120000], [1442063, 120000], [1452057, 120000], [1462050, 120000], [1472068, 120000], [1482023, 120000], [1492023, 120000], [1522015, 120000], [1532015, 120000]],
/*Projectiles*/ [[2070018, 60000], [2070016, 50000], [2070006, 5000], [2070005, 2500], [2330005, 5000]], //Arcane-Like
/*Other*/ [[1122017, 30000], [2340000, 150000], [2530000, 75000], [2531000, 250000], [3993002, 10000]], // Arcane-Like
/*Abyss Armor*/ [[1003280, 150000], [1003281, 150000], [1003282, 150000], [1003283, 150000], [1003284, 150000], [1052374, 150000], [1052375, 150000], [1052376, 150000], [1052377, 150000], [1052378, 150000], [1072544, 150000], [1072545, 150000], [1072546, 150000], [1072547, 150000], [1072548, 150000], [1082328, 150000], [1082329, 150000], [1082330, 150000], [1082331, 150000], [1082332, 150000]],
/*Abyss Weapons*/ [[1302173, 180000], [1312072, 180000], [1322107, 180000], [1332148, 180000], [1332149, 180000], [1342040, 180000], [1362022, 180000], [1372100, 180000], [1382124, 180000], [1402111, 180000], [1412071, 180000], [1422073, 180000], [1432099, 180000], [1442136, 180000], [1452129, 180000], [1462118, 180000], [1472141, 180000], [1482102, 180000], [1492101, 180000], [1522020, 180000], [1532037, 180000]],
/*Fearless Armor*/ [[1003285, 200000], [1003286, 200000], [1003287, 200000], [1003288, 200000], [1003289, 200000], [1052379, 200000], [1052380, 200000], [1052381, 200000], [1052382, 200000], [1052383, 200000], [1072549, 200000], [1072550, 200000], [1072551, 200000], [1072552, 200000], [1072553, 200000], [1082333, 200000], [1082334, 200000], [1082335, 200000], [1082336, 200000], [1082337, 200000], [1102311, 200000], [1032108, 200000], [1122148, 200000], [1092092, 200000], [1092093, 200000], [1092094, 200000]],
/*Fearless Weapons*/ [[1302174, 250000], [1312073, 250000], [1322108, 250000], [1332150, 250000], [1332151, 250000], [1342041, 250000], [1362023, 250000], [1372101, 250000], [1382125, 250000], [1402112, 250000], [1412072, 250000], [1422074, 250000], [1432100, 250000], [1442137, 250000], [1452130, 250000], [1462119, 250000], [1472142, 250000], [1482103, 250000], [1492102, 250000], [1522021, 250000], [1532038, 250000]]
]; //id, price

function start() {
    action(1, 0, 0);
} 

function action(mode, type, selection) {
    var record = cm.getQuestRecord(150001);
    var intPoints = parseInt(points);
    if (mode == 0 || mode == -1 && status == 0) {
        cm.dispose();
        return;
    } else (mode == 1 ? status++ : status--);
    
    if (status == 0) {
        points = record.getCustomData() == null ? "0" : record.getCustomData();
        cm.sendSimple("Would you like to have a taste of a relentless boss battle?#b\r\nCurrent points: " + points + "\r\n#L0#Warp to Lobby#l\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#L2#Trade Bamboo Luck Sack for points(10000)#l\r\n#L3#Timeless Weapons#l\r\n#L4#Projectiles#l\r\n#L5#Other#l\r\n#L6#Abyss Armors#l\r\n#L7#Abyss Weapons#l\r\n#L8#Fearless Armors#l\r\n#L9#Fearless Weapons#l\r\n ");
    } else if (status == 1) {
        select = selection;
        switch (selection) {
            case 0:
                cm.warp(bosspq);
                cm.dispose();
                break;
            case 2:
                cm.sendGetNumber("How many Bamboo Luck Sacks would you like to trade?\r\n", cm.itemQuantity(3993002), 1, 100);
                break;
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
                chooseItem(selection - 3);
                break;
        }
    } else if (status == 2) {
        sel = selection;
        if (select == 2) {
            if (cm.haveItem(3993002, selection)) {
                intPoints += (1000 * cm.itemQuantity(3993002));
                record.setCustomData(""+intPoints+"");
                cm.gainItem(3993002, -selection);
                cm.sendOk("Enjoy your rewards :P");
            } else {
                cm.sendOk("Please check if you have sufficient item, or inventory slot for it.");
            }
        } else if (select >= 3 && select <= 9) {
            gainReward(intPoints, record, select - 3);
        }
        cm.dispose();
    }
}

function chooseItem(index) {
    var choice = "Choose from any of the items below:#b";
    for (var i = 0; i < items[index].length; i++)
        choice += "\r\n#L" + i + "##i" + items[index][i][0] + "# Trade " + items[index][i][1] + " points (#z" + items[index][i][0] + "#)#l";
    choice += "\r\n "
    cm.sendSimple(choice);
}

function gainReward(intPoints, record, index) {
    if (intPoints >= items[index][sel][1] && cm.canHold(items[index][sel][0])) {
        intPoints -= items[index][sel][1];
        record.setCustomData(""+intPoints+"");
        cm.gainItem(items[index][sel][0], index == 1 ? (sel == 4 ? 3000 : 1000) : 1); // 3000 for bullets, they're unrechargable
        cm.sendOk("Enjoy your rewards :P");
    } else {
        cm.sendOk("Please check if you have sufficient points, or inventory slot for it. #bCurrent Points : " + points);
    }
}