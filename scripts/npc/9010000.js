var status, select;

function start() {
    status = 0;
    cm.sendSimple("Hello! Having fun exploring Maple World?\r\n\r\n#L0# End conversation.#l\r\n#L1#Exchange for Surprise Style Stamp.#l\r\n#L2#Exchange for Mystical Surprise Style Box#l\r\n#L3#Exchange unusable [Dual Blade] Mastery Book (You can perform exchanges untill 12/31/2012.)#l\r\n#L4#Exchange unusable [Explorer Warrior, Dawan Warrior]Mastery Book. (You can perform exchanges untill 12/31/2012.)#l\r\n#L5#Exchange unusable [Battle Mage] Mastery Book. (You can perform exchanges untill 12/31/2012.)#l\r\n#L6#Receive Mechanic-exclusive secondary weapon. (You can perform exchanges untill 12/31/2012. )#l\r\n#L7#Receive compensation for White Scroll removal. (Available untill 12/31.)#l");
}

function action(mode, type, selection) {
    if (mode != 1) {
        cm.dispose();
        return;
    }
    if (status == 0) {
        select = selection;
        switch (selection) {
            case 0:
                cm.dispose();
                return;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                break;
            case 7:
                cm.sendNext("Now that White Scrolls are going the way of the dinosaur, you can trade them in for Shield Scrolls.");
                break;
        }
    } else if (status == 1) {
        switch (selection) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                break;
            case 7:
                if (!cm.haveItem(2340000)) {
                    cm.sendPrev("You don't seem to have any items that can be exchanged for #i5064100##b#v5064100##k. You may only exchange #bWhite Scrolls#k.");
                    cm.dispose();
                    return;
                } else {
                    cm.sendGetNumber("How many #bWhite Scrolls would you like to trade?#k", cm.itemQuantity(2340000), 1, 100);
                }
                break;
        }
        status++;
    } else if (status == 2) {
        switch (selection) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                break;
            case 7:
                if (!cm.haveItem(2340000, selection)) {
                    cm.sendPrev("You don't seem to have enough #bWhite Scrolls#k that can be exchanged for #i2531000##b#v2531000##k.");
                    cm.dispose();
                    return;
                } else {
                    if (!cm.canHold(5064100)) {
                        cm.sendPrev("You don't seem to have enough space to hold #i2531000##b#v2531000##k.");
                        cm.dispose();
                        return;
                    } else {
                        cm.gainItem(2340000, -selection);
                        cm.gainItem(5064100, selection);
                        cm.dispose();
                        return;
                    }
                }
                break;
        }
        status++;
    }
}