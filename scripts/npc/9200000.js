importPackage(java.lang);
var items = [
[1002510, 1003296, 1012286, 1032111, 1032040, 1082252, 1082343, 1102166, 1112670, 1122015, 1122152, 1092030, 1302033, 1302065, 1302067, 1302180, 1302058, 1302181, 1302020, 1342025, 1382009, 1452016, 1462014, 1472030, 1482020, 1492020], //level 39-
[1003242, 1052357, 1072521, 1082314, 1102294, 1132092, 1152060, 1302169, 1312068, 1322099, 1332144, 1362057, 1372096, 1382120, 1402106, 1412067, 1422069, 1432095, 1442132, 1452125, 1462113, 1472136, 1482098, 1492097, 1522065, 1532069, 1003349, 1012287, 1032041, 1032112, 1082344, 1102167, 1112671, 1122153, 1302030, 1332025, 1342026, 1382012, 1412011, 1422014, 1432012, 1442024, 1452022, 1462019, 1472032, 1482021, 1492021, 1332142, 1342037, 1372094, 1382118, 1402104, 1442130, 1452123, 1462111, 1472134, 1482096, 1492095], //level 40-59
[1082252, 1082343], //level 60-69
[1082252, 1082343], //level 70-79
[1082252, 1082343], //level 80-99
[1082252, 1082343], //level 100+
[1082252, 1082343]  //Legend Maple scrolls
];
var leafprice = [50, 100, 100, 175, 350, 250, 350];
var mesoprice = [5000, 10000, 25000, 50000, 100000, 250000, 500000];
var leaf = 4001126;
var equipneeded = 0;
var chat = -1;
var select, tab = 0;
var status = -1;

function start() {
    action(1, 0, 0);
	status = -1;
}

function action(mode, type, selection) {
    if (mode == 0 || mode == -1 && chat == 0) {
        cm.dispose();
        return;
    }
    mode == 1 ? chat++ : chat--;
    switch (chat) {
        case 0:
            cm.sendSimple("What would you like to do? \r\n\r\n" + 
                "\r\n#L0#Make level 39- equipments#l" + 
                "\r\n#L1#Make level 40-59 equipments#l" + 
                "\r\n#L2#Make level 60-69 equipments#l" + 
                "\r\n#L3#Make level 70-79 equipments#l" + 
                "\r\n#L4#Make level 80-99 equipments#l" + 
                "\r\n#L5#Make level 100+ equipments#l" + 
                "\r\n#L6#Make Legend Maple scrolls#l" + 
                "\r\n#L7#Trade for Experience#l" + 
                "");
            break;
        case 1:
            tab = selection;
            itemSelection(selection);
            break;
        case 2:
            select = selection;
            chooseItem(selection);
            break;
        case 3:
            if (hasRequieredMaterials(tab, select))
                gainRewards(tab, select);
            else
                cm.sendOk("You don't have the requiered materials.");
            cm.dispose();
            break;
    }
}

function itemSelection(index) {
    var choice = "So, what do you want?\r\n#b";
    for (var i = 0; i < items[index].length; i++)
        choice += "\r\n#L" + items[index][i] + "##i" + items[index][i] + "##z" + items[index][i] + "##l";
    choice += "\r\n " //So we can see the last item because it glitches sometimes
    cm.sendSimple(choice);
}

function chooseItem(itemid) {
    var choice = "Are you sure you want to make a #i" + itemid + "##z" + itemid + "#?\r\nThe following items and materials will be requiered...\r\n\r\n";
    switch (tab) {
        case 0:
            choice += "\r\n#i" + leaf + "# x" + leafprice[0] + "";
            choice += "\r\n#fUI/UIWindow.img/QuestIcon/7/0# " + mesoprice[0] + "";
            //choice += "\r\n#i" + getStimulator(itemid) + "##z" + getStimulator(itemid) + "# can also be used. #r(Optional)#k";
            cm.sendYesNo(choice);
            break;
        case 1:
            equipneeded = getRequieredItem(itemid);
            choice += "#i" + leaf + "# x" + leafprice[0] + "";
            choice += "\r\n#fUI/UIWindow.img/QuestIcon/5/0# " + mesoprice[0] + "";
            choice += "\r\n#i" + getRequieredItem(itemid) + "##z" + getRequieredItem(itemid) + "";
            //choice += "#i" + getStimulator(itemid) + "##z" + getStimulator(itemid) + "# can also be used. #r(Optional)#k";
            cm.sendYesNo(choice);
            break;
    }
}

function hasRequieredMaterials(index, itemid) {
    switch (index) {
        case 0:
            if (cm.getMeso() >= mesoprice[0] && cm.itemQuantity(leaf) >= leafprice[0])
                return true;
            return false;
        case 1:
            if (cm.getMeso() >= mesoprice[0] && cm.itemQuantity(leaf) >= leafprice[0] && cm.itemQuantity(getRequieredItem(itemid)) >= 1)
                return true;
            return false;
    }
}

function gainRewards(index, itemid) {
    switch (index) {
        case 0:
            cm.gainMeso(-mesoprice[0]);
            cm.gainItem(leaf, -leafprice[0]);
            if (equipneeded > 0)
                cm.gainItem(equipneeded, -1);
            cm.gainItem(itemid, 1);
            break;
        case 1:
            cm.gainMeso(-mesoprice[0]);
            cm.gainItem(leaf, -leafprice[0]);
            if (equipneeded > 0)
                cm.gainItem(equipneeded, -1);
            cm.gainItem(itemid, 1);
            break;
    }
    cm.sendOk("Enjoy your rewards :D");
}