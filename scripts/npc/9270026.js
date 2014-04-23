var status = 0;
var Error = "You do not have enough vote points!";

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
    cm.sendSimple ("Hello #r#h ##k, Welcome to the #rVotepoint Exchanger Npc.#k\r\nYou have #r[" + cm.getVPoints() +"]#k Votepoints#k." +
                "\r\n#L88##bBuy NX Cash" + 
                "\r\n#L94##bMiracle Cubes#l" + 
                "\r\n#L500#Rings#l" + 
                "\r\n#L2000#Scrolls#l" + 
                "\r\n#L3000#Buff Items#l" +
				"\r\n#L4000#Other#l");
	} else if (selection == 4000) {
                cm.sendSimple ("You Currently have#r [" + cm.getVPoints() + "]#k Vote Points." +
                "#k\r\nWhat would you like?" +
                "#k\r\n#L4001##bChair Gachapon x4 - 2 VP#k\r\n");
    } else if (selection == 3000) {
                cm.sendSimple ("You Currently have#r [" + cm.getVPoints() + "]#k Vote Points."+
                "#k\r\nWhat would you like?" +
                "#k\r\n#L3003##b4 Hour 2x EXP Coupon - 2 VP " +
                "#k\r\n#L3002##b1 Day 2x Exp Coupon- 5 VP" +//5530062
                "#k\r\n#L3004##bOnyx Apples x5 - 1 VP " +
				"#k\r\n#L3005##b4 Hour 2x Drop/Meso Coupon - 3 VP " +//2022463
				"#k\r\n#L3006##b1 Day 2x Drop/Meso Coupon - 10 VP ");//2022461
    } else if (selection == 88) {
                cm.sendSimple ("You Currently have#r [" + cm.getVPoints() + "]#k Vote Points."+
         "#k\r\nWhat would you like?" +
                "#k\r\n#L89##b5k NX Cash - 1 VP " +
                "#k\r\n#L90##b12k NX Cash - 2 VP" +
                "#k\r\n#L91##b25k NX Cash - 3 VP" +
                "#k\r\n#L92##b42k NX Cash - 4 VP" +
                "#k\r\n#L93##b60k NX Cash - 5 VP");
    } else if (selection == 94) {
                cm.sendSimple ("You Currently have#r [" + cm.getVPoints() + "]#k Vote Points."+
                "#k\r\nWhat would you like?" +
                "#k\r\n#L96##b#i5062000#Miracle Cube x10 - 1 VP Each" +
                "#k\r\n#L97##b#i5062001#Premium Miracle Cube x3 - 1 VP Each" +
				"#k\r\n#L98##b#i5062005#Enlightning Miracle Cube x2 - 1 VP Each" +
				"#k\r\n#L99##b#i5062003#Revolutionary Miracle Cube x1 - 1 VP Each");
                            
    } else if (selection == 500) {
                cm.sendSimple ("You Currently have#r [" + cm.getVPoints() + "]#k Vote Points."+
				"#k\r\nWhat would you like?" +

                "#k\r\n#L504##b#i1112238# Ink Wash Text Ring - 5 VP Each" +//1112238
                "#k\r\n#L505##b#i1112135# Ink Wash Name Label Ring - 5 VP Each" +//1112135
				"#k\r\n#L506##b#i1113020# Lightning God Ring [ULTRA RARE!] - 15 VP Each");//1112586
    } else if (selection == 2000) {
                cm.sendSimple ("You Currently have #r[" + cm.getVPoints() + "]#k Vote Points." +
				"#k\r\nHow much would you like?" +
                 "#k\r\n#L2003##bAdvanced Enhancements Scrolls x7 - 1 VP" +// 2049306
                 "#k\r\n#L2004##b100% Epic Potential Scrolls x5 - 1 VP" +// 2049700
                 "#k\r\n#L2005##bProtection Scrolls x7 - 1 VP" +// 2531000
                 "#k\r\n#L2006##bWhite scroll x7 - 3 VP" +//2340000
                 "#k\r\n#L2007##bMiraculous Chaos Scrolls x1 - 1 VP"//2049116
                 );
	} else if (selection == 4001) {
                
                if (cm.getVPoints() > 1) {      
                    cm.setVPoints(-2);                   
                    cm.gainItem(5680021, 4);// Chair Gachapon
                    cm.dispose();
                    } else {
                    cm.sendOk(Error);
                    cm.dispose();
    }
	} else if (selection == 4002) {
		if (cm.getVPoints() > 0) {
			cm.setVPoints(-1);
			cm.gainMeso(50000000);
			cm.sendOk("You are welcome!");
			cm.dispose();
			} else {
			cm.sendOk(Error);
			cm.dispose();
			}
	} else if (selection == 3002) {
                
                if (cm.getVPoints() > 0) {      
                    cm.setVPoints(-1);                   
                    cm.gainItemPeriod(5211046, 1, 1);// 2x Coupon
                    cm.dispose();
                    } else {
                    cm.sendOk(Error);
                    cm.dispose();
    }
    } else if (selection == 3003) {
                
                if (cm.getVPoints() > 0) {      
                    cm.setVPoints(-1);                   
                    cm.gainItemPeriod2(5211048, 1, 4);// 1.3 Coupon / 4 hours
                    cm.dispose();
                    } else {
                    cm.sendOk(Error);
                    cm.dispose();
    }
    } else if (selection == 3004) {
                
                if (cm.getVPoints() > 0) {      
                    cm.setVPoints(-1);                    
                    cm.gainItem(2022179, 5);// Onyx Apples
                    cm.dispose();
                    } else {
                    cm.sendOk(Error);
                    cm.dispose();
    }
	} else if (selection == 3005) {
                
                if (cm.getVPoints() > 0) {      
                    cm.setVPoints(-1);                    
                    cm.gainItemPeriod2(5360042, 1, 4);// 2x Drop Coupon
                    cm.dispose();
                    } else {
                    cm.sendOk(Error);
                    cm.dispose();
    }
	} else if (selection == 3006) {
                
                if (cm.getVPoints() > 0) {      
                    cm.setVPoints(-1);                    
                    cm.gainItemPeriod(5360000, 1, 1);// 2x Meso Coupon
                    cm.dispose();
                    } else {
                    cm.sendOk(Error);
                    cm.dispose();
    }
    } else if (selection == 2003) {
                
                if (cm.getVPoints() > 0) {      
                    cm.setVPoints(-1);                   
                    cm.gainItem(2049306, 7);// Advanced Enhancements
                    cm.dispose();
                    } else {
                    cm.sendOk(Error);
                    cm.dispose();
    }
    } else if (selection == 2004) {
                
                if (cm.getVPoints() > 0) {      
                    cm.setVPoints(-1);                   
                    cm.gainItem(2049700, 5);// Epic 100% scrolls
                    cm.dispose();
                    } else {
                    cm.sendOk(Error);
                    cm.dispose();
    }
    } else if (selection == 2005) {
                
                if (cm.getVPoints() > 0) {      
                    cm.setVPoints(-1);                   
                    cm.gainItem(2531000, 7);// Protection Scrolls
                    cm.dispose();
                    } else {
                    cm.sendOk(Error);
                    cm.dispose();
    }
    } else if (selection == 2006) {
                
                if (cm.getVPoints() > 2) {      
                    cm.setVPoints(-3);                   
                    cm.gainItem(2340000, 7);// White scrolls
                    cm.dispose();
                    } else {
                    cm.sendOk(Error);
                    cm.dispose();
    }
    } else if (selection == 2007) {
                
                if (cm.getVPoints() > 0) {      
                    cm.setVPoints(-1);                   
                    cm.gainItem(2049116, 1);// Miraculous Chaos scrolls
                    cm.dispose();
                    } else {
                    cm.sendOk(Error);
                    cm.dispose();
    }
    
    } else if (selection == 504) {
                
                if (cm.getVPoints() > 4) {      
                    cm.setVPoints(-5);                    
                    cm.gainItem(1112238, 1);//Speech Bubble Ring
                    cm.dispose();
                    } else {
                    cm.sendOk(Error);
                    cm.dispose();
                    }
    } else if (selection == 505) {
                
                if (cm.getVPoints() > 4) {      
                    cm.setVPoints(-5);                    
                    cm.gainItem(1112135, 1);//Name Label Ring
                    cm.dispose();
                    } else {
                    cm.sendOk(Error);
                    cm.dispose();
                    }
    } else if (selection == 506) { 
                
                if (cm.getVPoints() > 14) {      
                    cm.setVPoints(-15);                    
                    cm.gainItem(1113020, 1);//Lightning God Ring
                    cm.dispose();
                    } else {
                    cm.sendOk(Error);
                    cm.dispose();
                    }
    
    } else if (selection == 89) {
                var price = 5000000;
                if (cm.getVPoints() > 0) {      
                    cm.setVPoints(-1);                   
                   cm.gainNXCredit(10000);
                   cm.dispose();
                } else {
                   cm.sendOk(Error);
                   cm.dispose();
                   }
    } else if (selection == 90) {
                var price = 10000000;
                if (cm.getVPoints() > 1) {      
                    cm.setVPoints(-2);                  
                    cm.gainNXCredit(24000);
                   cm.dispose();
                     } else {
                   cm.sendOk(Error);
                   cm.dispose();
                   }
    } else if (selection == 91) {
                var price = 15000000;
                if (cm.getVPoints() > 2) {      
                    cm.setVPoints(-3);                    
                   cm.gainNXCredit(50000);
                   cm.dispose();
                     } else {
                   cm.sendOk(Error);
                   cm.dispose();
                   }
    } else if (selection == 92) {
                var price = 20000000;
                if (cm.getVPoints() > 3) {      
                    cm.setVPoints(-4);                    
                  cm.gainNXCredit(84000);
                   cm.dispose();
                     } else {
                   cm.sendOk(Error);
                   cm.dispose();
                   }
                   
    } else if (selection == 93) {
                if (cm.getVPoints() > 4) {      
                    cm.setVPoints(-5);                    
                   cm.gainNXCredit(120000);
                   cm.dispose();
                     } else {
                   cm.sendOk(Error);
                   cm.dispose();
                   }
            
    } else if (selection == 96) {
                
                if (cm.getVPoints() > 0) {      
                    cm.setVPoints(-1);                   
                    cm.gainItem(5062000, 10);//Miracle Cube
                    cm.gainItem(2460003, 10);//Magnifying glass
                    cm.dispose();
                     } else {
                    cm.sendOk(Error);
                    cm.dispose();
                    }
                   
    } else if (selection == 97) {
                
                if (cm.getVPoints() > 0) {      
                    cm.setVPoints(-1);                   
                    cm.gainItem(5062001, 3);//Premium Miracle Cube
                    cm.gainItem(2460003, 3);//Magnifying glass
                    cm.dispose();
                     } else {
                    cm.sendOk(Error);
                    cm.dispose();
                   }
                }else if (selection == 98) {
                
                if (cm.getVPoints() > 0) {      
                    cm.setVPoints(-1);                   
                    cm.gainItem(5062005, 2);//Enlightning Miracle Cube
                    cm.gainItem(2460003, 2);//Magnifying glass
                    cm.dispose();
                     } else {
                    cm.sendOk(Error);
                    cm.dispose();
                   }
                }else if (selection == 99) {
                
                if (cm.getVPoints() > 0) {      
                    cm.setVPoints(-1);                   
                    cm.gainItem(5062003, 1);//Revolutionary Miracle Cube
                    cm.gainItem(2460003, 1);//Magnifying glass
                    cm.dispose();
                     } else {
                    cm.sendOk(Error);
                    cm.dispose();
                   }
                }
                }
				}