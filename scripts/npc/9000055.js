var buy = null;//true = Buy GML and Sell GML || false = Buying Items
var convert = null; // true = Buy GML function || false = Sell GML Function
/*DO NOT EDIT THIS^^*/
var menu = ["Hats - 70 000 NX", "Top - 80 000 NX", "Capes - 100 000 NX", "Pants - 80 000 NX", "Shoes - 50 000 NX", "Overalls - 100 000 NX", "Gloves - 50 000 NX", "Weapons - 100 000 NX", "Super Rares - 200 000 NX\r"];
var options = [
    /*Hat*/[1003743, 1003463, 1003506, 1003763, 1003760, 1003597, 1003541, 1000031, 1003074, 1003077, 1003078, 1003079, 1003080, 1003083, 1003084, 1003089, 1003092, 1003101, 1003103, 1003109, 1003120, 1003122, 1003123, 1003131, 1003132, 1003135, 1003136, 1003144, 1003145, 1003146, 1003147, 1003148, 1003149, 1003161, 1003163, 1003170, 1003171, 1003186, 1003208, 1003187, 1003387,
        /*New Hats*/1003082, 1003241, 1003398, 1003399, 1003400, 1003401, 1003402, 1003404, 1002225, 1000026, 1012007, 1002999, 1003560, 1003559, 1003575, 1003754, 1003758, 1001090, 1002312, 1003220, 1003279, 1003520, 1003596, 1003010],
    /*Top*/[1042188, 1042189, 1042190, 1042193, 1042194, 1042198, 1042199, 1042200, 1042202, 1042203, 1042204, 1042206, 1042207, 1042208, 1042209, 1042210, 1042212, 1042213, 1042214, 1042215, 1042216, 1042217, 1042218, 1042219, 1042220, 1042221, 1042222, 1042230, 1042232, 1042178, 1049000,
        /*New tops*/1042075, 1042162, 1042237],
    /*Capes*/[1102373, 1102097, 1102148, 1102149, 1102096, 1102095, 1102261, 1102213, 1102214, 1102222, 1102223, 1102253, 1102301,
        /*New Capes*/1102290, 1102291, 1102310, 1102325, 1102511, 1102326, 1102389, 1102390, 1102356, 1102486, 1102184, 1102450, 1102451, 1102452, 1102453, 1102487, 1102188, 1102236, 1102251, 1102270, 1102271],
    /*Pants*/ [1062121, 1062122, 1062123, 1062124, 1062126, 1062129, 1062130, 1062131, 1062133, 1062134, 1062135, 1062136, 1062137, 1062138, 1062139, 1062145, 1062147,
        /*New pants*/1062112, 1061148],
    /*Shoes*/ [1072622, 1072438, 1072439, 1072440, 1072441, 1072443, 1072444, 1072454, 1072457, 1072461, 1072462, 1072464, 1072465, 1072466, 1072467, 1072469, 1072470, 1072478, 1072482, 1072483, 1072484, 1072507, 1072509, 1072514, 1072515, 1072516, 1072517, 1072536, 1072537,
        /*New Shoes*/1072330, 1072333, 1072334, 1072341, 1072349, 1070005, 1071003],
    /*Overalls*/[1052416, 1052245, 1052246, 1052248, 1052253, 1052255, 1052268, 1052275, 1052282, 1052283, 1052284, 1052286, 1052289, 1052290, 1052291, 1052292, 1052293, 1052294, 1052295, 1052296, 1052298, 1052306, 1052309, 1052330, 1052331, 1052332, 1052338, 1052339, 1052340, 1052348, 1052354, 1052355, 1052356, 1052367, 1052368, 1052370,
        /*New overall*/1052000, 1052001, 1052002, 1050019, 1051131, 1052211, 1051189, 1051295, 1052224, 1050154, 1051190],
    /*Gloves*/   [1082263, 1082272, 1082273, 1082282, 1082312, 1082407, 1082408, 1082423,
        /*New Gloves*/1082101],
    /*Weapons*/ [1702351, 1702342, 1702371, 1702381, 1702374, 1702372, 1702274, 1702275, 1702330, 1702340, 1702324, 1702295, 1702259, 1702261, 1702263, 1702264, 1702276, 1702277, 1702278, 1702284, 1702285, 1702287, 1702288, 1702289, 1702291, 1702293, 1702296, 1702299, 1702301, 1702302, 1702303, 1702304, 1702305, 1702306, 1702308, 1702309,
        /*New Weapons*/1702335, 1342069],
    /*SuperRares*/  [1102376, 1102377, 1102378, 1102466, 1102532, 1702235, 1000050,1001076, 1102380]
];
var c;
var price;
var currency = 4310029;
var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    var Error = "You don't have enough #rNX#k, You have #r" + cm.checkNX() + "#k NX";

    if (mode == -1) {
        cm.dispose();
    }
    else {
        if (status == 0 && mode == 0) {
            cm.dispose();
            //return;
        }
    }

    if (mode == 1)
        status++;

    else
        status--;
    if (status == 0) {
        var text = "Hello #r#h ##k. You have #b" + cm.checkNX() + "#k NX\r\n#r#L100#Help#l\r\n#L101#Trade NX for #i" + currency + "#  \r\n#L102#Trade  #i" + currency + "# for NX \r\n";
        for (var z = 0; z < menu.length; z++)
            text += "#L" + z + "##b" + menu[z] + "#l\r\n";
        cm.sendSimple(text);
    } else if (status == 1) {
        c = selection;
        if (c == 100) {
            cm.sendOk("#b In order to obtain Rebirth Points you will need to rebirth. Each rebirth gives you 50 points which you can use on items or JQ");
            cm.dispose();
        } else if (c == 101) {//Buy GML
                convert = true;
                cm.sendGetNumber("I can Exchange #r1000 NX#k for #r1 #t" + currency + "# #i" + currency + "##k\r\nHow many #r#t" + currency + "##k would you like\r\nYou have #b" + cm.checkNX() + "#k NX\r\n#e#rNOTE: (Input is 1000x)#k#n", 1000, 1000, 100000);
        } else if (c == 102) {//Sell GML
                convert = false;
                cm.sendGetNumber("I can Exchange #r1 #t" + currency + "# #i" + currency + "##k for #r1000 NX#k\r\nHow many #r#t" + currency + "##k would you like\r\nYou have #b" + cm.checkNX() + "#k NX\r\n#e#rNOTE: (Input is 1000x)#k#n", 1, 1, 100);
        } else {
            buy = false;
            talk = "#rPick your equips:\r\n#k";
            for (var i = 0; i < options[c].length; i++)
                talk += "#L" + i + "##e#i" + options[c][i] + ":##k#l";
            cm.sendSimple("#r#eYou have to click any of these items to recieve it.#k#n\r\n" + talk);
        }
    } else if (status == 2) {
        if (buy == false) {
            if (c == 0) {//Hats
                price = 70000;
            } else if (c == 1 || c == 3) {//Top Or pants
                price = 80000;
            } else if (c == 2 || c == 5 || c == 7) {//Capes Or Overalls or Weapons
                price = 100000;
            } else if (c == 4 || c == 6) {//Shoes Or Gloves
                price = 50000;
            } else if (c == 8) {//Super Rares
                price = 200000;
            } else {
                price = null;
            }
                if (price != null) {
				if (cm.canHold(currency)) {
                    if (cm.checkNX() >= price) {
                        cm.gainNXCredit(-price);
                        var id = options[c][selection];
                        //cm.sendOk("C= "+c+" selection= "+selection+"\r\nID="+id+"\r\nprice="+price);
                        cm.gainItem(id, 1);
                        cm.dispose();
						
                    } else {
                        cm.sendOk(Error);
					}
				}else {
				    cm.sendOk("Your Inventory is full");
                    cm.dispose();
				}
                } else {
                    cm.sendOk("#bA price was not assigned for this equiplist, please tell a coder");
                }
                cm.dispose();
                return;
        } else {
            if (convert == true) {
                var amount = selection;
                var CurAmount = amount / 1000;
                var whole = CurAmount % 1 == 0;
                if (whole == true) {
				if (cm.canHold(currency)) {
                    if (cm.checkNX() >= amount) {
                        //cm.sendOk("amount: "+ amount+" GML: "+CurAmount+" Whole: "+whole);
                        cm.gainItem(currency, CurAmount);
                        cm.gainNXCredit(-amount);
                        cm.dispose();
                    } else {
                        cm.sendOk(Error);
                        cm.dispose();
                    }
					}else {
					    cm.sendOk("Your Inventory is full");
                        cm.dispose();
					}
                } else {
                    cm.sendOk("#e#rYou Cannot recieve " + CurAmount + " Golden Maple Leaves");
                    cm.dispose();
                }
            } else {
                var amount = selection;
                var FMPoints = amount * 2000;
                if (cm.haveItem(currency, amount)) {
                    //cm.sendOk("amount: " + amount + " FMPoints: " + FMPoints);
                    cm.gainItem(currency, -amount);
                    cm.gainNXCredit(FMPoints);
                    cm.dispose();
					
                } else {
                    cm.sendOk("You have #r#c" + currency + "# #t" + currency + "##k");
                    cm.dispose();
                }
            }
        }
    }
}