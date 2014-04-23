/*
 * @Author:      Itzik
 * @Purpose:     Boss Warper
 */
var status = -1;
var map = [
271030201,
271030202,
271030203,
271030204,
271030205
];

function start() {
	status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1 || mode == 0 && status == 0) {
        cm.sendOk("Aww, I guess you are not strong enough, poor #h #?");
        cm.dispose();
        return;
    }
    mode == 1 ? status++ : status--;
    if (status == 0) {
        cm.sendYesNo("Would you like to hunt a few bosses?");
    } else if (status == 1) {
        var text = "What map would you like to move to?";
        for (var i = 0; i < map.length; i++)
            text += "\r\n#L" + i + "##m" + map[i] + "##l";
        cm.sendSimple(text);
    } else if (status == 2) {
        cm.warp(map[selection]);
        cm.dispose();
    }
}