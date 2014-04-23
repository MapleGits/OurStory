
var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    cm.sendSimple("#dWelcome to OurStory v142.2 Please be aware we are new to 142 and there will be quite a few bugs which are mostly known");
    cm.dispose();
}