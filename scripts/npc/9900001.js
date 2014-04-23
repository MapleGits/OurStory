/** Author: nejevoli
	NPC Name: 		NimaKin
	Map(s): 		Victoria Road : Ellinia (180000000)
	Description: 		Maxes out your stats and able to modify your equipment stats
*/
importPackage(java.lang);

var status = 0;
var slot = Array();
var stats = Array("Strength", "Dexterity", "Intellect", "Luck", "HP", "MP", "Weapon Attack", "Magic Attack", "Weapon Defense", "Magic Defense", "Accuracy", "Avoidability", "Hands", "Speed", "Jump", "Slots", "Vicious Hammer", "Used slot", "Enhancements", "Potential stat 1", "Potential stat 2", "Potential stat 3", "Potential stat 4", "Potential stat 5", "Owner");
var selected;
var statsSel;

function start() {
	status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (status >= 0 && mode == 0) {
        cm.dispose();
        return;
    }
    if (mode == 1)
        status++;
    else
        status--;

    if (status == 0) {
	var options = "What do you want from me?#b\r\n#L8#Level Up my Honour!\r\n#L0#Max my stats!#l\r\n#L4#Set AP/SP to 0#l\r\n#L7#Clear my stats!#l\r\n#L6#Max Skills by Job#l";
        
	    if (cm.getPlayer().getGMLevel() >= 0 && cm.getPlayer().getGMLevel() <= 3) {
	// for players
		cm.warp(910000000, 0);
		cm.sendOk("You should not be in here, im warping you out of here")
        cm.dispose();
    } else if (cm.getPlayer().getGMLevel() <= 2) {
	// for regular Gm's Works for Interns and Regular Gm's
        cm.sendSimple(options);
	}else if (cm.getPlayer().getGMLevel() <= 4) {
		options+= "#b\r\n#L5#Clear Skills#l\r\n#L1#Max all skills#l";
		cm.sendSimple(options);
	} else {
	// for everyone else Works for Super GM's, Admins and Owners
		options+= "\r\n#L5#Clear Skills#l\r\n#L1#Max all skills#l";
		options+= "#r\r\n#L2#Modify my equip's stats!#l\r\n#L3#Look at potential values#l";
        cm.sendSimple(options);
    }
	} else if (status == 1) {
        if (selection == 0) {
            cm.maxStats();
            cm.sendOk("I have maxed your Stats. Happy Mapling!");
            cm.dispose();
		} else if (selection == 1) {
            //Beginner
            cm.maxAllSkills();
			cm.sendOk("I have maxed all of your Skills. Happy Mapling!");
            cm.dispose();
        } else if (selection == 7) {
            cm.getPlayer().resetStats(4, 4, 4, 4);
            //cm.getPlayer().setHP(50);
            //cm.getPlayer().setMP(50);
            cm.sendOk("I have cleared your stats. Happy Mapling!");
            cm.dispose();
        } else if (selection == 2) {
            var avail = "";
            for (var i = -1; i > -199; i--) {
                if (cm.getInventory(-1).getItem(i) != null) {
                    avail += "#L" + Math.abs(i) + "##t" + cm.getInventory(-1).getItem(i).getItemId() + "##l\r\n";
                }
                slot.push(i);
            }
            cm.sendSimple("Which one of your equips would you like to modify?\r\n#b" + avail);
        } else if (selection == 3) {
            var eek = cm.getAllPotentialInfo();
            var avail = "#L0#Search for potential item#l\r\n";
            for (var ii = 0; ii < eek.size(); ii++) {
                avail += "#L" + eek.get(ii) + "#Potential ID " + eek.get(ii) + "#l\r\n";
            }
            cm.sendSimple("What would you like to learn about?\r\n#b"+ avail);
            status = 9;
        } else if (selection == 4) {
            cm.getPlayer().resetAPSP();
            cm.dispose();
        } else if (selection == 5) {
            cm.clearSkills();
            cm.dispose();
        } else if (selection == 6) {
            cm.maxSkillsByJob();
            cm.dispose();
        } else if (selection == 8) {
            cm.getPlayer().honourLevelUp();
            cm.dispose();
		} else {
		cm.dispose();
		}
}	else if (status == 2) {
        selected = selection - 1;
        var text = "";
        for (var i = 0; i < stats.length; i++) {
            text += "#L" + i + "#" + stats[i] + "#l\r\n";
        }
        cm.sendSimple("You have decided to modify your #b#t" + cm.getInventory(-1).getItem(slot[selected]).getItemId() + "##k.\r\nWhich stat would you like to modify?\r\n#b" + text);
    } else if (status == 3) {
        statsSel = selection;
        if (selection == 24) {
            cm.sendGetText("What would you like to set your #b#t" + cm.getInventory(-1).getItem(slot[selected]).getItemId() + "##k's " + stats[statsSel] + " to?");
        } else {
            cm.sendGetNumber("What would you like to set your #b#t" + cm.getInventory(-1).getItem(slot[selected]).getItemId() + "##k's " + stats[statsSel] + " to?", 0, 0, 60004);
        }
    } else if (status == 4) {
        cm.changeStat(slot[selected], statsSel, selection);
        cm.sendOk("Your #b#t" + cm.getInventory(-1).getItem(slot[selected]).getItemId() + "##k's " + stats[statsSel] + " has been set to " + selection + ".");
        cm.dispose();
    } else if (status == 10) {
        if (selection == 0) {
            cm.sendGetText("What would you like to search for? (e.g. STR %)");
            return;
        }
        cm.sendSimple("#L3#" + cm.getPotentialInfo(selection) + "#l");
        status = 0;
    } else if (status == 11) {
        var eek = cm.getAllPotentialInfoSearch(cm.getText());
        for (var ii = 0; ii < eek.size(); ii++) {
            avail += "#L" + eek.get(ii) + "#Potential ID " + eek.get(ii) + "#l\r\n";
        }
        cm.sendSimple("What would you like to learn about?\r\n#b"+ avail);
        status = 9;
    } else {
        cm.dispose();
    }
}