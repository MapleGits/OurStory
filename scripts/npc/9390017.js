var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (status >= 0 && mode == 0) {
	cm.sendOk("LELELELE");
	cm.dispose();
	return;
    }
    if (mode == 1)
	status++;
    else
	status--;
    if (status == 0) {
	cm.sendOk("The #bMaple Art Online#k Dungeon is not open yet.");
    } else if (status == 1) {
	cm.sendNext("Wut");
    } else if (status == 2) {
	cm.dispose();
    }
}