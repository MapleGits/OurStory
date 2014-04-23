/*
	Encrypted Slate of the Squad - Leafre Cave of life
*/

var status;

function start() {
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
return;
    }
}

if (mode == 1) 
   status++;

else 
   status--;
    if (status == 0) { 
	cm.sendSimple("Which Horntail would you like to face#b\r\n#L0#Horntail#l\r\n#L1#Chaos Horntail#l");
	}
	else if (status == 1) {
	if (selection == 0) {
		cm.warp(240050400,0);
		cm.dispose();
	} else if (selection == 1) {
		cm.dispose();
		cm.openNpc(2083000);
	}
	}
	}