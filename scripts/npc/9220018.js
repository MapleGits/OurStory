/*
	Custom Exchange NPC by Wes
*/

var status;
var scrolls =[
[2043002, //Warrior
2043102, 
2044002, 
2044302, 
2044402], 
[2045202, //Bowman
2044502,
2044602],
[2043802, //Magician
2043702],
[2044702, //Thief
2043302, 
2043402, 
2043602], 
[2044802, //Pirate
2044902, 
2045302],
[2040805, //Common
2040760,
2040816]];
var items =[
1152059
];
var job;
var melon = 4031701;

function start() {
status = -1;
action(1, 0, 0);
}

function action(mode, type, selection) {

if (mode != 1) {
cm.dispose();
}
else {
if (status == 0 && mode == 0) {
cm.dispose();
return;
    }
}

if (mode == 1) 
   status++;

else 
   status--;
   
    if (status == 0) { 
        cm.sendNext("Silly me, I happened to drop my #i"+melon+"##r#t"+melon+"#s#k all over the maple world... If you could bring some back to me, I could give you some pretty fancy things!");
    }
    
	
    if (status == 1) {
        if (cm.haveItem(melon)) {
            cm.sendSimple("#bWhat would you like?#k\r\nYou have #r#c"+melon+"# #k#i"+melon+"##r#t"+melon+"#s#k." + 
            "\r\n#b#L0#10% Scrolls#l");
        } else {
            cm.sendOk("You don't have any #i"+melon+"##r#t"+melon+"#s#k.");
            cm.dispose();
        }
		
	
    } else if (status == 2) {
        if (selection == 0) {
			cm.sendSimple("What job are the scrolls related to?\r\n#b" +
                "#L0#Warrior#l\r\n" +
                "#L1#Bowman#l\r\n" +
                "#L3#Thief#l\r\n" +
                "#L2#Magician#l\r\n" +
                "#L4#Pirate#l\r\n" +
				"#L5#Common#l");
		} 
    } else if (status == 3) {
		job = selection;
        text = "These are the scrolls you desired to see...\r\n";
        for (var scroll = 0; scroll < scrolls[job][scroll]; scroll++)
        text += "#L"+scroll+"##i"+scrolls[job][scroll]+"# - #t"+scrolls[job][scroll]+"#\r\n";
		cm.sendSimple(text);		
	} else if (status == 4) {
		scroll = selection;
		if (cm.canHold(scrolls[job][scroll])) {
		scroll = selection;
        cm.sendOk("Thank you, good friend! You have been rewarded for your dilligence!");
		cm.gainItem(scrolls[job][scroll], 1);
        cm.gainItem(melon, -1);
        cm.dispose();
		} else {
		cm.sendOk("You don't have room in your inventory.");
		cm.dispose();
		}
	}
}

/*
var status;
var scrolls = [2040006, 2040007, 2040008];
var items = [1152060, 1152061, 1152062];

function start() {
status = -1;
action(1, 0, 0);
}

function action(mode, type, selection) {

if (mode != 1) {
cm.dispose();
}
else {
if (status == 0 && mode == 0) {
cm.dispose();
	return;
    }
}

if (mode == 1) 
   status++;

else 
   status--;
   
    if (status == 0) {
		cm.sendSimple("#L0##bScrolls#l" +
			"\r\n#L1#Items#l#k");
			var choice = selection;
			
	} else if (status == 1) {
		if (selection == 0) {
			msg = "Here are some scrolls.#b\r\n";
			for (var i = 0; i < scrolls.length; i++)
			msg += "#L"+i+"##t"+scrolls[i]+"##l\r\n";
			cm.sendSimple(msg + "#k");
			var scroll = selection;
		} else if (selection == 1) {
			msg = "Here are some items.#b\r\n";
			for (var e = 0; e < items.length; e++)
			msg += "#L"+e+"##t"+items[e]+"##l\r\n";
			cm.sendSimple(msg);
			var item = selection;
		}
	} else if () {
}
*/
/* Original NPC
function action(mode, type, selection) {
	cm.removeAll(4032248);
	    if (cm.getPlayer().getParty() == null || !cm.isLeader()) {
		cm.sendOk("The leader of the party must be here.");
	    } else {
		var party = cm.getPlayer().getParty().getMembers();
		var mapId = cm.getPlayer().getMapId();
		var next = true;
		var size = 0;
		var it = party.iterator();
		while (it.hasNext()) {
			var cPlayer = it.next();
			var ccPlayer = cm.getPlayer().getMap().getCharacterById(cPlayer.getId());
			if (ccPlayer == null || ccPlayer.getLevel() < 8) {
				next = false;
				break;
			}
			size += (ccPlayer.isGM() ? 2 : 1);
		}	
		if (next && size >= 2) {
			var em = cm.getEventManager("MV");
			if (em == null) {
				cm.sendOk("Please try again later.");
			} else {
		    var prop = em.getProperty("state");
		    if (prop.equals("0") || prop == null) {
			em.startInstance(cm.getPlayer().getParty(), cm.getPlayer().getMap());
		    } else {
			cm.sendOk("Another party quest has already entered this channel.");
		    }
			}
		} else {
			cm.sendOk("All 2+ members of your party must be here and above level 8.");
		}
	    }
	cm.dispose();
}
*/