importPackage(net.sf.odinms.client);
var status;
var wui = 0;
var max = 32766;
var selected = 1182009;
var candy = 4031203;
var halloween = "Happy Halloween~"
var notenough = "Trick or treat, your trick failed. You need more candy if you want that prize."

function start() { 
    status = -1; 
    action(1, 0, 0); 
} 

function action(mode, type, selection) { 
    if (mode == 1) { 
        status++; 
    }else{ 
        status--; 
    }
	
	if (status == -1) {
		cm.dispose();
	}
    
    if (status == 0) { 
        cm.sendSimple("It's that time of year again. I'm back, with more rewards.\r\n#L0##r#i1182009##k Pumpkin badge(+100) for 100 Halloween Candies#l\r\n#L1##r#i1702203##k Halloween Teddy(+150)  for 150 Halloween Candies#l\r\n#L2##r#i1003693##k Halloween Hat(+200) for 200 Halloween Candies#l\r\n#L3##b#i1052172##k Pumpkin suit(+250) for 250 Halloween Candies#l\r\n#L4##b#i3010043##k Halloween chair for 500 Halloween Candies#l\r\n#");
    } else if (status == 1) { 
        if (selection == 0) { 
            if (cm.haveItem(candy, 100)) { 
                cm.gainItem(candy, -100)
				cm.MakeHItem(1182009, cm.getChar(), 100,10,10);
                cm.sendOk(halloween);
				cm.msiMessage("[Halloween] Congratulations to "+cm.getPlayer().getName()+" on his/her newly acquired Halloween Item");
                cm.dispose();
            }else{
                cm.sendOk(notenough);
                cm.dispose();
            }
        } else if (selection == 1) {
            if (cm.haveItem(candy, 150)) { 
                cm.gainItem(candy, -150)
				cm.MakeHItem(1702203, cm.getChar(), 150,50,50);
                cm.sendOk(halloween);
				cm.msiMessage("[Halloween] Congratulations to "+cm.getPlayer().getName()+" on his/her newly acquired Halloween Item");
                cm.dispose();
            }else{
                cm.sendOk(notenough); 
                cm.dispose();
            }
            } else if (selection == 2) {
            if (cm.haveItem(candy, 200)) { 
                cm.gainItem(candy, -200)
             	cm.MakeHItem(1003693, cm.getChar(), 200,100,100);
                cm.sendOk(halloween);
				cm.msiMessage("[Halloween] Congratulations to "+cm.getPlayer().getName()+" on his/her newly acquired Halloween Item");
                cm.dispose();
            }else{
                cm.sendOk(notenough); 
                cm.dispose();
           }
            } else if (selection == 3) {
            if (cm.haveItem(candy, 250)) {
                cm.gainItem(candy, -250)
                cm.MakeHItem(1052172, cm.getChar(), 250,150,150);
                cm.sendOk(halloween);
				cm.msiMessage("[Halloween] Congratulations to "+cm.getPlayer().getName()+" on his/her newly acquired Halloween Item");
                cm.dispose();
             }else{
                cm.sendOk(notenough);
                cm.dispose();
                }
            } else if (selection == 4) {
            if (cm.haveItem(candy, 500)) {
                cm.gainItem(candy, -500)
				cm.gainItem(3010043, 1)
                cm.dispose();
             }else{
                cm.sendOk(notenough);
                cm.dispose();
                }
            }
      }
}