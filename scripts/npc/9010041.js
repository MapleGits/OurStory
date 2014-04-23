function start() {
    cm.sendSimple("Looking to get a gander at the fruits of your labor? All help for Part-Time Jobs will be handled by me, #bMs. Appropriation#k.\r\n#b#e#L0# Accept Part-Time Job Reward. #l");
}

function action(mode, type, selection) {
    if (mode != 1) {
        cm.dispose();
        return;
    }
    //if (cm.getPlayer().completedPartTime()) {
    //dunno
    //} else
    cm.sendOk("Hmm... Are you sure you completed the Part-Time Job? There are no rewards available right now.");
    cm.dispose();
}