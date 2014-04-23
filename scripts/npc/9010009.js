rewards = [["Ryko", 1902018, 5], ["Wolf Saddle", 1912011, 5], ["Spectrum Goggles", 1022082, 5], ["Raccoon Mask", 1022058, 5], ["White Raccoon Mask", 1022060, 5], ["Archeologist Glasses", 1022089, 5], ["Silver Deputy Star", 1122014, 5], ["Blizzard Stick", 1702211, 10], ["Patriot Seraphim", 1702187, 10], ["Seraphim Cape", 1102222, 10], ["Timeless MoonLight", 1102172, 5], ["BlackFist Cloak", 1102206, 5], ["Angry Mask", 1012110, 7], ["Sad Mask", 1012111, 7], ["Crying Mask", 1012109, 7], ["Happy Mask", 1012108, 7], ["Strawberry Popsicle", 1012070, 10],
    ["Chocolate Popsicle", 1012071, 10]];

maps = [["Breath of Lava", 280020000], ["Ghost Chimney", 682000200], ["Fitness Test", 109040000], ["Forest of Tenacity", 910530000], ["Forest of Patience", 690000066]];

var option = null;
var status = 0;

function start() {
    if (cm.getPlayer().getClient().getChannel() != 1) {
        cm.sendOk("JumpQuests may only be attempted on channel 1.");
        cm.dispose();
        return;
    }
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {

    if (mode == -1) {
        cm.dispose();
    }
    else {
        if (status == 0 && mode == 0) {
            cm.dispose();
            //return;
        }
    }

    if (mode == 1)
        status++;

    else
        status--;
    if (status == 0) {
        cm.sendSimple("Welcome to the Jump Quest NPC. You have #r" + cm.getJQPoints() + "#k JQ Points.\r\n#b#L100#I want to go Jump for my life#l\r\n\r Rewards are removed while JQ event lasts, check forums for more info!");
    } else if (status == 1) {
        if (selection == 100) {
            var talk = "I am the Jump Quest Warper! What Jump Quest will it be?\r\n#rIt will cost 1,000 NX to challenge any JumpQuest#b";
            for (var j = 0; j < maps.length; j++)
                talk += "\r\n#L" + j + "#" + maps[j][0] + "#l";
            cm.sendSimple(talk);
            option = true;
        } else if (selection == 101) {
            var talk = "Welcome to the Jump Quest Point Trader. What would you like? Keep in mind it costs 5~10 JQ points each!\r\nYou have #r" + cm.getJQPoints() + "#k JQ Points#b";
            for (var i = 0; i < rewards.length; i++)
                talk += "\r\n#L" + i + "#" + rewards[i][0] + " #i" + rewards[i][1] + ":# - " + rewards[i][2] + " JQ Points#l";
            cm.sendSimple(talk);
            option = false;
        }
    } else if (status == 2) {
        if (option == true) {//Map Warper
            if (cm.checkNX() >= 1000) {
                cm.gainNXCredit(-1000)
                cm.warp(maps[selection][1], 0);
				cm.gainItem(4033039,1);
                cm.sendOk("Make it to the end of the map for a prize!");
                cm.dispose();
            } else {
                cm.sendOk("You don't have enough NX, you need 5000");
                cm.dispose();
            }
        } else if (option == false) {//Exchanger
            if (cm.getJQPoints() >= rewards[selection][2]) {
                cm.sendOk("Here you go!, enjoy your prize #h #.");
                cm.gainItem(rewards[selection][1], 1);
                cm.gainJQPoints(-rewards[selection][2]);
                cm.dispose();
            } else {
                cm.sendOk("You don't have enough JQ Points");
                cm.dispose();
            }
        }
    }
}