/*
DB Skills NPC by Wes
/*
	Mom and Dad, 9201003
	DB Skills NPC made by Wes
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
		cm.sendYesNo("If you are a Blade Master, I can give you #rPhantom Blow#k and\r\n#rFinal Cut#k and #rSharpness#k if you don't already have them.\r\n\r\n#bWould you like to learn these skills?#k");
	}
	
	if (status == -1) {
		cm.sendSimple("Goodbye!");
		cm.dispose();
   }
   
	if (status == 1) {
	//&& !cm.getPlayer().getSkillLevel(4341009) > 0 && !cm.getPlayer().getSkillLevel(4341002) > 0
		if (cm.getPlayer().getJob() == 434) {
			cm.sendSimple("You now have the skills!");
			cm.teachSkill(4341009, 30, 30);
			cm.teachSkill(4341002, 30, 30);
			cm.teachSkill(4340010, 10, 10);
			cm.teachSkill(4341000, 30, 30);
			cm.dispose();
		} else {
			cm.sendSimple("Either you aren't a Blade Master, or you already have these skills...");
			cm.dispose();
		}
		
	}
	
}