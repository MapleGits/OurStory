var talk = "#rPick your empress equips:\r\n#k"; 
var chosen = 1;
var Error;
var one;
var menu = ["Warrior Set - 20,000 DP","Mage Set - 20,000 DP","Bowman Set - 20,000 DP","Thief Set - 20,000 DP","Pirate Set - 20,000 DP\r\n","Warrior Weapons - 10,000 DP(You get only 1 weapon)","Magician Weapons - 10,000 DP(You get only 1 weapon)","Archer Weapons - 10,000 DP(You get only 1 weapon)","Thief Weapons - 10,000 DP(You get only 1 weapon)","Pirate Weapons - 10,000 DP(You get only 1 weapon)"]; 
var empeq = [
/*warrioreq */[1003172, 1082295, 1052314, 1072485, 1102275],
/*mageeq*/[1003173, 1082296, 1052315, 1072486, 1102276],
/*boweq */[1003174, 1082297, 1052316, 1072487, 1102277],
/*thiefeq*/ [1003175, 1082298, 1052317, 1072488, 1102278],
/*pirateeq*/ [1003176, 1082299, 1052318, 1072489, 1102279],

/*Warriorwep*/[1302152,1312065,1322096,1402095,1412065,1422066,1432086,1442116],
/*MageWep*/   [1372084, 1382104],
/*Bowmanwep*/ [1462099, 1522018, 1452111],
/*Thiefwep*/  [1342036, 1362019, 1472122, 1332130],
/*Piratewep*/ [1492085, 1482084, 1532018]];
var c; 
status = 0; 
var lolthis = 0;
function start() {
    Error = "You do not have enough Donor points!\r\nYou have #r"+cm.getDPoints()+" Donation points";
	
    cm.sendAcceptDecline("Hi #b#h ##k, I am the Donation NPC for #rViciousMS#k\r\n\r\n#b1)When you donate you can get various rewards as a thank you gift from us!\r\n2)To be able to use donor commands you need to donate at least $20\r\n3)Along with donor commands you get blue text ingame\r\n4)When you donate you also get donor rank on forums, at least $5\r\n5)You can see list of all donor commands at forums in donations section\r\n6)There are also donor ocupations, for more info about that check website and click on donate page!\r\n#rClick Accept, if you accept to these terms");
} 
function action(m,t,selection) { 
    chosen = selection;
    if (m != 1) { 
        cm.dispose(); 
        return; 
    }else{ 
        status++; 
    } 
    if (status == 1) {
        cm.sendSimple ("You have #b"+cm.getDPoints()+"#k Donation Points#k\r\n#L0##bNX Prizes#l\r\n#L1#Empress Set#l\r\n#L2#Cubes and Scrolls#l\r\n#L3#Change NPC Pack - 20,000 DP\r\n#L4#Name Change - 10,000 DP\r\n#L5#Reserved\r\n#L6#Angelic Blessing Ring#l\r\n#L7#Android + Heart#l\r\n#L8#Add 1 slot to choice of an equip#l\r\n#L1000#Exchange 1,000 DP for a Maple Coin#l\r\n#L1001#Exchange 1 Maple Coin for 1,000 DP#l\r\n#L1002#Exchange 10,000 DP for a red luck sack#l\r\n#L1003#Exchange a red luck sack for 10,000 DP#l\r\n");
    }
    else if (status == 2) { 
        if (selection == 0) {//NX
            cm.sendSimple("You have #b"+cm.getDPoints()+"#k Donation Points\r\n#b#L100#12k NX - 1000 DP#l\r\n#L110#75k NX - 3000 DP#l\r\n#L120#250k NX - 4000 DP#l\r\n#L130#1 Million NX - 15000 DP#l");
        }else if (selection == 1) {//Empress
            var text = "Hello #r#h ##k. You have #b"+cm.getDPoints()+"#k Donation Points\r\n"+talk+""; 
            var text = "Hello #r#h ##k. You have #b"+cm.getDPoints()+"#k Donation Points\r\n"+talk+""; 
            for (var z = 0; z < menu.length; z++) 
                text+= "#L"+z+"##b"+menu[z]+"#l\r\n"; 
            one = false;
            cm.sendSimple(text); 
        } else if (selection == 2) {//Cubes
            cm.sendSimple("You have #b"+cm.getDPoints()+"#k Donation Points\r\n#b#L200#Cube coin x10 - 1,000 DP#l\r\n#L201#Cube Coin x50 - 5,000 DP#l\r\n#L202#Cube Coin x150 - 10,000 DP#l\r\n#L203#Scroll shop x10 - 1,250 DP#l\r\n#L204#Scroll shop x50 - 5,000 DP#l\r\n#L205#Scroll shop x150 - 10,000 DP#l");
        }else if (selection == 3) {//Change NPC Pack
            cm.sendSimple("If you buy this, you can change any NPC you want to Player NPC with your name and looks\r\n#e#rGive this wish ticket to either Apollo or Ivan so that they can make the Player NPC\r\n#n#r#L500#I Accept#l");
        }else if (selection == 4) {//Name Changer
            if (cm.getDPoints() > 9999) {  
                cm.sendGetText("What would you like your name to be?\r\n#eNO SPECIAL CHARACTERS #n(as in numbers)#e, OR DIE#n\r\nBe #e#rSUPER#k#n careful typing, you can't redo this!");
            }else {
                cm.sendOk(Error);
                cm.dispose();
            }
        }
        else if (selection == 5) {//100b mesarz
            if (cm.getDPoints() > 1000) {  
                cm.sendSimple("Not finished yet.");
				cm.dispose();
            }else  {
                cm.sendOk(Error);
                cm.dispose();
            }
        }else if (selection == 6) {//Angelic Blessing
            cm.sendSimple("Pick an Angelic Blessing Ring:\r\n#L300##i1112585# - 5000 DP#l\r\n#L302##i1112663# - 15000 DP");
        }else if (selection == 7) {//Android
            cm.sendSimple("The Android comes with a heart in the set.\r\nPick an Android:\r\n#L400##i1662002# + #i1672005# - 10,000 DP\r\n#L401##i1662003# + #i1672005# - 10,000 DP#l");
		} else if (selection == 8) {
			if (cm.getDPoints() >= 5000) {
				cm.sendSimple("Choose the equip you would like a slot on:\r\n"+cm.EquipList(cm.getC()));
				lolthis = 1000;
			} else {
				cm.sendOk("You don't have 5000 DP for a slot.");
				cm.dispose();
			}
		} else if (selection == 1000) {
			if (cm.getDPoints() >= 1000) {
				cm.gainItem(4001129,1);
				cm.setDPoints(-1000);
				cm.sendOk("You have gained 1 maple coin.");
				cm.dispose();
			} else {
				cm.sendOk("You don't have 1000 DP.");
				cm.dispose();
			}
		} else if (selection == 1001) {
			if (cm.haveItem(4001129, 1)) {
				cm.gainItem(4001129, -1);
				cm.setDPoints(1000);
				cm.sendOk("You have lost 1 maple coin and gained 1000 DP.");
				cm.dispose();
			} else {
				cm.sendOk("You don't have 1 maple coin!");
				cm.dispose();
			}
		} else if (selection == 1002) {
			if (cm.getDPoints() >= 10000) {
				cm.gainItem(3993003, 1);
				cm.setDPoints(-10000);
				cm.sendOk("You have gained 1 red luck sack.");
				cm.dispose();
			} else {
				cm.sendOk("You don't have 10,000 DP.");
				cm.dispose();
			}
		} else if (selection == 1003) {
			if (cm.haveItem(3993003, 1)) {
				cm.gainItem(3993003, -1);
				cm.setDPoints(10000);
				cm.sendOk("You have lost 1 red luck sack and gained 10,000 DP.");
				cm.dispose();
			} else {
				cm.sendOk("You have no red luck sacks!");
			}
		}
    } else if (status == 3) {
        var name = cm.getText();
        //Starting NX
		if (lolthis == 1000) {
			if (!cm.checkSlots(chosen)) {
				cm.setDPoints(-5000);
				cm.giveSlots(chosen, 1);
				cm.getPlayer().fakeRelog();
				cm.sendOk("You have gotten your item upgraded..");
				cm.dispose();
			} else {
				cm.sendOk("You can't upgrade this item anymore...");
				cm.dispose();
			}
		}
        if (selection == 100) {
            if (cm.getDPoints() > 999) {      
                cm.setDPoints(-1000);                    
                cm.gainNXCredit(24000);
                cm.dispose();
            } else {
                cm.sendOk(Error);
                cm.dispose();
            }
        } else if (selection == 110) {
            if (cm.getDPoints() > 2999) {      
                cm.setDPoints(-3000);                    
                cm.gainNXCredit(140000);
                cm.dispose();
            } else {
                cm.sendOk(Error);
                cm.dispose();
            }
        } else if (selection == 120) {
            if (cm.getDPoints() > 3999) {      
                cm.setDPoints(-4000);                    
                cm.gainNXCredit(500000);
                cm.dispose();
            } else {
                cm.sendOk(Error);
                cm.dispose();
            }
        } else if (selection == 130) {
            if (cm.getDPoints() > 14999) {      
                cm.setDPoints(-15000);                    
                cm.gainNXCredit(2000000);//1million
                cm.dispose();
            } else {
                cm.sendOk(Error);
                cm.dispose();
            }
        }
        //End of NX      
        //Start of Cubes
        else if (selection == 200) {
            if (cm.getDPoints() > 999) {      
                cm.setDPoints(-1000);                    
                cm.gainItem(4310025, 10);
                cm.dispose();
            } else {
                cm.sendOk(Error);
                cm.dispose();
            }
        }
        else if (selection == 201) {
            if (cm.getDPoints() > 4999) {      
                cm.setDPoints(-5000);                    
                cm.gainItem(4310025, 50);
                cm.dispose();
            } else {
                cm.sendOk(Error);
                cm.dispose();
            }
        }else if (selection == 202) {
            if (cm.getDPoints() > 9999) {      
                cm.setDPoints(-10000);                    
                cm.gainItem(4310025, 150);
                cm.dispose();
            } else {
                cm.sendOk(Error);
                cm.dispose();
            }
        }
        else if (selection == 203) {//enlightening cubes
            if (cm.getDPoints() > 1249) {      
                cm.setDPoints(-1250);                    
                cm.gainItem(4310011, 10);
                cm.dispose();
            } else {
                cm.sendOk(Error);
                cm.dispose();
            }
        }
        else if (selection == 204) {
            if (cm.getDPoints() > 4999) {      
                cm.setDPoints(-5000);                    
                cm.gainItem(4310011, 50);
                cm.dispose();
            } else {
                cm.sendOk(Error);
                cm.dispose();
            }
        }
        else if (selection == 205) {
            if (cm.getDPoints() > 9999) {      
                cm.setDPoints(-10000);                    
                cm.gainItem(4310011, 150);
                cm.dispose();
            } else {
                cm.sendOk(Error);
                cm.dispose();
            }
        //End of cubes
        //Angelic Blessing Rings
        }else if (selection == 300) {
            if (cm.getDPoints() > 4999) {      
                cm.setDPoints(-5000);                    
                cm.gainItem(1112585, 1);
                cm.dispose();
            } else {
                cm.sendOk(Error);
                cm.dispose();
            }
        }else if (selection == 302) {
            if (cm.getDPoints() > 14999) {      
                cm.setDPoints(-15000);                    
                cm.gainItem(1112663, 1);
                cm.dispose();
            } else {
                cm.sendOk(Error);
                cm.dispose();
            }
        }
        //End of Angelic Blessing Rings
        //Start of Android
        else if (selection == 400) {
            if (cm.getDPoints() > 9999) {      
                cm.setDPoints(-10000);                    
                cm.gainItem(1662002, 1);
                cm.gainItem(1672005, 1);
                cm.dispose();
            } else {
                cm.sendOk(Error);
                cm.dispose();
            }
        }else if (selection == 401) {
            if (cm.getDPoints() > 9999) {      
                cm.setDPoints(-10000);                    
                cm.gainItem(1662003, 1);
                cm.gainItem(1672005, 1);
                cm.dispose();
            } else {
                cm.sendOk(Error);
                cm.dispose();
            //End of Android
            }
        }else if(selection == 500) {
            if (cm.getDPoints() > 19999) {     
                cm.gainItem(4031544 , 1);
                cm.setDPoints(-20000);
                cm.dispose();
            }else {
                cm.sendOk(Error);
                cm.dispose();
            }	
        //End of Android
        }else {
            //100b mesos
            if (one == true) {
                cm.depositMesosToBank(100000000000);
                cm.reloadChar();
                cm.setDPoints(-1000);
                cm.dispose();	
            }
            //Name Changer
            if (name != null) {
                if(name.contains(" ")) {
                    cm.sendOk("Your name contains a space in it, please enter a name without a space");
                    cm.dispose();
                }else {
                    if (cm.isValid(name) == true) {
                        if (cm.ifNameExist(name) == false) {
                            //Changing the name here
                            cm.setDPoints(-10000); 
                            cm.setName(name);
                            cm.dispose();
                        }else {
                            cm.sendOk("You entered a name that already exists");
                            cm.dispose();
                        }
                    }else {
                        cm.sendOk("You entered a name with special characters");
                        cm.dispose();
                    }
                }
            }
            //Empress equips
            if (one == false) {
                c = selection; 
                for (var i = 0; i < empeq[c].length; i++) 
                    talk+="#L"+i+"##e#i"+empeq[c][i]+":##k#l"; 
                cm.sendSimple("You will be recieving all "+empeq[c].length+" items\r\n#r#eYou have to click any of these items to recieve it.#k#n\r\n"+talk);
                one = false;
            }
        }
    }else if (status == 4) {
        //Empress item getting process
        if (one == false) {
            if (c >= 5) {
                if (cm.getDPoints() > 9999) {//Weapons
                    cm.setDPoints(-10000); 
                    var w = empeq[c][selection];
                    //cm.sendOk("C= "+c+" selection= "+selection+"\r\n"+w);
                    cm.gainItem(w, 1);
                    cm.dispose(); 
                }else {
                    cm.sendOk(Error);
                    cm.dispose();
                }
            }else {
                if (cm.getDPoints() > 19999) {//Armor
                    cm.setDPoints(-20000); 
                    for (var i = 0; i < empeq[c].length; i++) {
                    var w = empeq[c][i];					
                        cm.gainItem(w, 1);
                    }
                    //var w = empeq[c][selection];
                    //cm.sendOk("C= "+c+" selection= "+selection+"\r\n"+w);
                    cm.dispose(); 
			
                }else {
                    cm.sendOk(Error);
                    cm.dispose();  
                }              
            }	
        }
    }
}