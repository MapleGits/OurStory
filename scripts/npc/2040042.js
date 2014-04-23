var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 1) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
            if (status == 0) {
                cm.sendNext("Hello there! My name is Henki! I want to teach you how to script NPCs!");
            } else if (status == 1) {
                cm.sendSimple("So, why are you talking to me? \r\n #L0##bI want to learn NPC scripting#k#l \r\n #L1##bNevermind#k#l");
            } else if (status == 2) {
                if (selection == 0) {
                    cm.sendOk("Then you have to go to ragezone and find my tutorial. Cya in ragezone!");
                } else if (selection == 1) {
                    cm.dispose();
                }
            }
    } 
} 