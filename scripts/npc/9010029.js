var chat = -1;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1 /*End Chat*/) {
        cm.dispose();
        return;
    }
    if (mode == 0 && chat == 0 /*Due to no chat -1*/) {
        cm.sendOk("Aww.... guess you are afraid of events");
        cm.dispose();
        return;
    }
    if (mode == 1) //Next/Ok/Yes/Accept
        chat++;
    else if (mode == -1) //Previous/No/Delience
        chat--;
    startChat(selection, type);
}

function startChat(selection, type) {
    if (chat == 0)
        cm.sendYesNo("Hello! Would you like to hear about the new Events, and Updates?");
    else if (chat == 1)
        cm.sendOk("#eUpdate Highlights: #bMoonlight Revamp#k#n\r\nThe Moonlight Revamp is here!\r\nCheck the forums for additional information!");
    else
        cm.dispose();
}