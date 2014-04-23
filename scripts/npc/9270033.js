function start() {
    cm.sendOk("Cubing NPC is currently disabled.");
    cm.dispose();
    }
/*
var cube = "Which cube would you like";
var cubeslot = 0;
var equip = "Pick the equip you'd like to cube. \r\n";
function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1)
        status++;
    else {
        cm.dispose();
        return;
    }
	if (status == 0) {
	cm.sendSimple(cube+cm.CashList(cm.getClient()));
	} else if (status == 1) {
	cubeslot = selection;
	if (cm.areCubes(cm.getPlayer(), selection) != 1) {
	cm.sendOk("Please pick a cube");
	cm.dispose();
	} else {
	cm.sendSimple(equip+cm.EquipList(cm.getClient()));
	}
	} else if (status == 2) {
	cm.CubingShit(cm.getPlayer(), selection, cubeslot);
	cm.dispose();
	}
}
*/