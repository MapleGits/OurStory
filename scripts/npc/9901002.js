var status;

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
    
    if (status == 0) { 
        cm.sendSimple("#rWarning if you click end chat use @dispose#k \r\n Make sure you have enough slots for the items you want#g\r\nIt's that time of year again. I'm back, with more rewards.\r\n#L0##rBeer Shield#k for 150 Clovers#l\r\n#L4##bSaint Paddy's Set M#k for 800 Saint Paddy's Clovers#l\r\n#L5##bSaint Paddy's Set F#k for 800 Saint Paddy's Clovers#l\r\n#L2##d(300)Power Elixirs#k for 200 Saint Paddy's Clovers#l"); 
    } else if (status == 1) { 
        if (selection == 0) { 
            if (cm.haveItem(3994247, 150)) { 
                cm.gainItem(3994247, -150)
                cm.gainItem(1092108, 1)
                cm.sendOk("Enjoy!!");
                cm.dispose();
            }else{
                cm.sendOk("you dont have enough #v3994247#");
                cm.dispose();
            }
		 } else if (selection == 2) {
            if (cm.haveItem(3994247, 200)) { 
                cm.gainItem(3994247, -200)
                cm.gainItem(2000005, 300)
                cm.sendOk("Enjoy :)");
                cm.dispose();
            }else{
                cm.sendOk("you dont have enough #v3994247#");
                cm.dispose();
            }
        } else if (selection == 4) {
            if (cm.haveItem(3994247, 800)) {
                cm.gainItem(3994247, -800)				
                cm.gainItem(1002342, 1)//skull hat
                cm.gainItem(1022036, 1)//witch cloths
                cm.gainItem(1042164, 1)//black high tops
                cm.gainItem(1062009, 1)//skull gloves (not in cs)
                cm.gainItem(1072333, 1)//dark seraphim
                cm.sendOk("Enjoy!!");
                cm.dispose();
            }else{
                cm.sendOk("you dont have enough #v3994247#"); 
                cm.dispose();
            }
            } else if (selection == 3) {
            if (cm.haveItem(3994247, 800)) {
                cm.gainItem(3994247, -800)			
                cm.gainItem(1002891, 1)//skull hat
                cm.gainItem(1022007, 1)//ghost uniform
                cm.gainItem(1042236, 1)//black high tops
                cm.gainItem(1062093, 1)//skull gloves (not in cs)
                cm.gainItem(1072235, 1)//dark seraphim
                cm.sendOk("Enjoy!!");
                cm.dispose();
            }else{
                cm.sendOk("you dont have enough #v3994247#"); 
                cm.dispose();
           }
        }
    }
} 