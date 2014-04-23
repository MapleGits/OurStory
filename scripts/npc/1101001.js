 /* 
	NPC Name: 		Divine Bird
	Map(s): 		Erev
	Description: 		Buff
*/
importPackage(Packages.tools.packet);
function start() {
    //cm.useItem(2022458);
    //cm.sendOk("Don't stop training. Every ounce of your energy is required to protect the world of Maple....");
    CWvsContext.serverNotice(0, 0, "String message", true);
}

function action(mode, type, selection) {
    cm.dispose();
}