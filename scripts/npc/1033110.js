/* * * * * * * * * * * * * * * \
*         Inventory Spy        *
*  By Hugo of MadStory/VoidMS  *
*      gugubro1@gmail.com      *
*         madstory.org         *
*          voidms.com          *
\ * * * * * * * * * * * * * * */

var name;
var status = 0;
var thing = 0;
var slot;
var p = null;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 2 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            if (cm.getPlayer().getGMLevel() >= 4) {
                cm.sendGetText("Hey #r#h ##k! I can check a player's inventory for you. \r\n\r\n#rPlease type in a players' name");
            } else {
				cm.warp(910000000, 0);
                cm.sendOk("Who brought you here, GTFO!!!!!!");
                cm.dispose();
            }
        } else if (status == 1) {
            name = cm.getText(); 
            p = cm.getCharByName(name);
            if (p != null) {
                cm.sendSimple("Choose an inventory#b\r\n#L0#Equip#l\r\n#L1#Use#l\r\n#L2#Set-up#l\r\n#L3#ETC#l\r\n#L4#Cash#l\r\n#L5#Equipped#l");
            } else {
                cm.sendOk("#rThe player you are trying to choose either is offline or not in your channel.");
            }
        } else if (status == 2) {
            string = "Click on an item to remove #rall#k of it.\r\n#n";
            thing = selection;
            if (selection == 0) {                
                cm.sendSimple(string+cm.EquipList(p.getClient()));
            } else if (selection == 1) {
                cm.sendSimple(string+cm.UseList(p.getClient()));
            } else if (selection == 2) {
                cm.sendSimple(string+cm.SetupList(p.getClient()));
            } else if (selection == 3) {
                cm.sendSimple(string+cm.ETCList(p.getClient()));
            } else if (selection == 4) {
                cm.sendSimple(string+cm.CashList(p.getClient()));
            } else if (selection == 5) {
				cm.sendSimple(string+cm.EquippedList(p.getClient()));
			}
        } else if (status == 3) {
            slot = selection;
            send = "The user has#r ";
            send2 = "#k of the item #i";
            if (thing == 0) {
                send += p.getItemQuantity(p.getEquipId(selection), true);
                send2 += p.getEquipId(selection);
            } else if (thing  == 1) {
                send += p.getItemQuantity(p.getUseId(selection), true);
                send2 += p.getUseId(selection);
            } else if (thing == 2) {
                send += p.getItemQuantity(p.getSetupId(selection), true);
                send2 += p.getSetupId(selection);
            } else if (thing == 3) {
                send += p.getItemQuantity(p.getETCId(selection), true);
                send2 += p.getETCId(selection);
            } else if (thing == 4) {
                send += p.getItemQuantity(p.getCashId(selection), true);
                send2 += p.getCashId(selection);
            } else if (thing == 5) {
				send += p.getItemQuantity(p.getEquippedId(selection), true);
				send2 += p.getEquippedId(selection);
			}
            var send3 = send + send2 + "# are you sure you want to delete #rall#k of that item?";
            cm.sendYesNo(send3);
        } else if (status == 4) {
            if (thing == 0) { 
                p.removeAll(p.getEquipId(slot));
            } else if (thing == 1) {
                p.removeAll(p.getUseId(slot));
            } else if (thing == 2) {
                p.removeAll(p.getSetupId(slot));
            } else if (thing == 3) {
                p.removeAll(p.getETCId(slot));
            } else if (thing == 4) {
                p.removeAll(p.getCashId(slot));
            } else if (thing == 5) {
				p.removeAll(p.getEquippedId(slot));
			}
            cm.sendOk("Successfully deleted " +  name + "'s item");
            cm.dispose();
        }
    }
}  