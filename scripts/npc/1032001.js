/*
Npc Author : DaTicTac
Npc : Random Gachapon Npc
*/

var status;
var random;
var randomNumb;
var rewards;
var MSI_rewards;
var reqitem1;
var reqitem2;
var reqitem3;

status = 0;
random = Math.floor((Math.random()*500)+1);
randomNumb = Math.floor((Math.random()*100)+1);
reqitem1 = "" ;
reqitem2 = "";
reqitem3 = "";
rewards = [ ];
MSI_rewards = [ ];

function action(mode,type,selection)
{
	if(mode == 1)
	{
		status++;
	}
	else if(mode == 0)
	{
		status--;
	}
	else if(mode == -1)
	{
		cm.sendOk("I'm a awesome NPC!");
		cm.dispose();
	}
		if(status == 0)
		{
			cm.sendSimple("Hey" + cm.getPlayer().getName() + "\r\n Do you want to give it a try? \r\n\r\n #L0#Giving it a SHOT#l \r\n #L1#No Thanks.#l");
			cm.dispose();
		}
		else if(selection == 0)
		{
			cm.forceStartQuest();
			cm.sendOk("Here are the requirements : \r\n\r\n" + #ireqitem1# + #zreqitem1# + "\r\n" +#ireqitem2# + #zreqitem2# + "\r\n" +#ireqitem3# + #zreqitem3# + "\r\n\r\n Guess the NUMBER correctly to get a random item.");
			cm.dipose();
		}
		else{ 
		if(selection == 1)
		{
			cm.sendOk("Please do come back and try out.");
			cm.dispose();
		}
		}
}

function action(mode,type,selection)
{
	if(mode == 1)
	{
		status++;
	}
	else if(mode == 0)
	{
		status--;
	}
	else if(mode == -1)
	{
		cm.sendOk("I'm a awesome NPC!");
		cm.dispose();
	}
		if(status == 0)
		{
			cm.sendSimple("Do you have the item? \r\n & prepared for a guessing game? \r\n\r\n #L2#Yes#l \t #L3#No#l");
			cm.dispose();
		}
		else if(selection == 2)
		{
			if(cm.haveItem(reqitem1) && cm.haveItem(reqitem2) && cm.haveItem(reqitem3))
			{
				cm.sendGetNumber("Guess the random number");
				cm.dispose();
			}
			else if(cm.sendGetNumber == randomNumb)
			{
				if(cm.haveItem(reqitem1) && cm.haveItem(reqitem2) && cm.haveItem(reqitem3))
				{
				cm.gainItem(-reqitem1);
				cm.gainItem(-reqitem2);
				cm.gainItem(-reqitem3);
				cm.gainItem(rewards[random],100) || cm.makeProItem(MSI_rewards[random], 32767);
				cm.sendOk("You've got the number right & got yourself an Item");
				cm.dispose();
				cm.forceCompleteQuest();
				cm.reloadChar();
				cm.worldMessage(6, "[ServerName] " + cm.getPlayer().getName() + " had just make it through the gachapon and got item");
				}
			}
			else
			{
				cm.sendOk("Try again later but you've got yourself 500k mesos for trying out");
				cm.gainMeso(500000);
				cm.dispose();
				cm.forceCompleteQuest();
				cm.reloadChar();
				cm.worldMessage(6, "[ServerName] " + cm.getPlayer().getName() + " Will have to try again later");
			}
		}
}