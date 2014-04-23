var status = -1;
var cat;
var prizes = [
[[1112585, 1, 1], [1112663, 3, 1], [1112238, 1, 1], [1112135, 2, 1]], // ring
[[5062000, 1, 30], [5062001, 1, 9], [5062005, 1, 6], [5062003, 1, 3]], // cube
[[2022179, 1, 15], [5680021, 1, 3], [2340000, 1, 5], [4000136, 1, 5]], // other
[[1662002, 3, 1], [1662003, 3, 1], [1672005, 2, 1]], //android
[[2213042, 3, 10], [2213032, 2, 10], [2213043, 2, 20], [2213017, 2, 30], [2213019, 1, 30]]//transform potions
];

function start() {
if (cm.haveItem(4000038,1)) {
	cm.sendYesNo("Welcome to ViciousMS! You currently have #c4000038# #i4000038#.\r\ Would you like to exchange them for something?");
	//cm.sendOk("#rThis NPC is disabled until we can find all the event trophies");
	//cm.dispose();
	}else {
	cm.sendOk("You dont have any #r#t4000038# #i4000038##k");
	cm.dispose();
}
}
function action(m, t, s) {
	if (m != 1) {
		cm.dispose();
	} else {
		status++;
		if (status == 0) {
			cm.sendSimple("What would you like to trade?\r\n#L0##gNX#l\r\n#L1##rRings#l\r\n#L2##dMiracle Cubes#l\r\n#L3##bOther#l\r\n#L4##rAndroid#l\r\n#L5##gTransformation pots#l");
		} else if (status == 1) {
			cat = s;
			if (cat == 0) {
				cm.sendSimple("What would you like?\r\n#L0#10k NX: #i4000038#\r\n#L1#50k NX: #i4000038##i4000038#\r\n#L2#100k NX: #i4000038##i4000038##i4000038#");
			} else if (cat == 1) {
				cm.sendSimple("What would you like?\r\n#L0##i1112585# Angelic Blessing: #i4000038#\r\n#L1##i1112663# White Angelic Blessing: #i4000038##i4000038##i4000038#\r\n#L2##i1112238# Ink Wash Text Ring: #i4000038#\r\n#L3##i1112135# Ink Wash Name Label Ring: #i4000038##i4000038#");
			} else if (cat == 2) {
				cm.sendSimple("What would you like?\r\n#L0##i5062000#30 Miracle Cubes: #i4000038#\r\n#L1##i5062001#9 Premium Miracle Cubes: #i4000038#\r\n#L2##i5062005#6 Enlightening Miracle Cubes: #i4000038#\r\n#L3##i5062003#3 Revolutionary Miracle Cube: #i4000038#");
			} else if (cat == 3) {
				cm.sendSimple("What would you like?\r\n#L0##i2022179# 15 Onyx Apples: #i4000038#\r\n#L1##i5680021# 3 Chair Gachapons: #i4000038#\r\n#L2##i2340000# 5 White Scrolls: #i4000038#\r\n");
			}
			else if (cat == 4) {
				cm.sendSimple("What would you like?\r\n#L0##i1662002# Male Android: #i4000038##i4000038##i4000038#\r\n#L1##i1662003# Female Android: #i4000038##i4000038##i4000038#\r\n#L2##i1672005# Crystal Heart: #i4000038##i4000038#");
			}
			else if (cat == 5) {
				cm.sendSimple("What would you like?\r\n#L0##i2213042# 10 Von leon transformation pots #i4000038##i4000038##i4000038#\r\n#L1##i2213032# 10 Balrog transformation pots: #i4000038##i4000038#\r\n#L2##i2213043# 20 Papulatus Transformations pots: #i4000038##i4000038#\r\n#L3##i2213017# 30 King Clang Transformations pots: #i4000038##i4000038#\r\n#L4##i2213019# 30 Muscle Stone Transformations pots: #i4000038#");
			}
		} else if (status == 2) {
			if (cat == 0) {
				if (s == 0) {
					if (cm.haveItem(4000038, 1)) {
						cm.gainItem(4000038, -1);
						cm.gainNXCredit(20000);
						cm.sendOk("Thanks!");
					} else {
						cm.sendOk("You don't have enough #i4000038#.");
					}
				} else if (s == 1) {
					if (cm.haveItem(4000038, 2)) {
						cm.gainItem(4000038, -2);
						cm.gainNXCredit(100000);
						cm.sendOk("Thanks!");
					} else {
						cm.sendOk("You don't have enough #i4000038#.");
					}
				} else if (s == 2) {
					if (cm.haveItem(4000038, 3)) {
						cm.gainItem(4000038, -3);
						cm.gainNXCredit(200000);
						cm.sendOk("Thanks!");
					} else {
						cm.sendOk("You don't have enough #i4000038#.");
					}
				}
				cm.dispose();
			} else if (s == 1000) {
				if (cm.haveItem(4000038, 1)) {
					cm.gainMeso(80000000);
					cm.gainItem(4000038, -1);
					cm.sendOk("Thanks!");
				} else {
					cm.sendOk("You don't have enough #i4000038#.");
				}
				cm.dispose();
			} else {
				var prizeInfo = prizes[cat - 1][s];
				if (!cm.canHold(prizeInfo[0])) {
					cm.sendOk("You don't have enough inventory space.");
					cm.dispose();
					return;
				}
				if (cm.haveItem(4000038, prizeInfo[1])) {
					cm.gainItem(4000038, -prizeInfo[1]);
					cm.gainItem(prizeInfo[0], prizeInfo[2]);
					cm.sendOk("Thanks!");
				} else {
					cm.sendOk("You don't have enough #i4000038#.");
				}
				cm.dispose();
			}
		}
	}
}