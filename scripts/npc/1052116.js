var status = -1;
var map = [271030201,271030202,271030203,271030204,271030205]; //Hallowed Map Empress Quest Maps

function start() {
	status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1 || mode == 0 && status == 0) {
        cm.dispose();
        return;
    }
		mode == 1 ? status++ : status--;
    if (status == 0) {
		var text = "What map would you like to move to?";
		for (var i = 0; i < map.length; i++)
        text += "\r\n#L" + i + "##m" + map[i] + "##l";
		cm.sendSimple(text);
    } else if (status == 1) {
		cm.warp(map[selection]);
		cm.dispose();
    }
}