var status = -1;

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
	cm.sendSimple("Hi #r#h ##k, I can do all these things for you\r\n#b#L0#Max Skills#l \r\n#L1#Reset AP (don't use if you have over 30 000 stats!)#l\r\n#L2#Set your unused SkillPoints to 0#l");
	}else if (status == 1) {
	if (selection == 0) {
	cm.maxSkillsByJob();
	cm.teachSkill(80001000,1,1);
	cm.sendOk("I have maxed your skills successfully");
	cm.dispose();
}
else if (selection == 1) {
cm.getPlayer().resetStats(4, 4, 4, 4);
cm.sendOk("I have reset your AP successfully");
cm.dispose();
}
else if (selection == 2) {
cm.sendOk("I have set your unused SP to 0");
cm.getPlayer().resetSP(0);
cm.dispose();
}
}
}