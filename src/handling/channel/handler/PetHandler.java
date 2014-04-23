package handling.channel.handler;

import clientside.MapleCharacter;
import clientside.MapleClient;
import clientside.MapleDisease;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.PetCommand;
import constants.GameConstants;
import handling.world.MaplePartyCharacter;
import java.awt.Point;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.life.MapleMonster;
import server.maps.FieldLimitType;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.movement.LifeMovementFragment;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.PetPacket;

public class PetHandler {

    public static final void SpawnPet(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        slea.readInt();
        chr.spawnPet(slea.readByte(), slea.readByte() > 0);
    }

    public static final void Pet_AutoPotion(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        slea.skip(GameConstants.GMS ? 9 : 1);
        slea.readInt();
        short slot = slea.readShort();
        if ((chr == null) || (!chr.isAlive()) || (chr.getMapId() == 749040100) || (chr.getMap() == null) || (chr.hasDisease(MapleDisease.POTION))) {
            return;
        }
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);

        if ((toUse == null) || (toUse.getQuantity() < 1) || (toUse.getItemId() != slea.readInt())) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        long time = System.currentTimeMillis();
        if (chr.getNextConsume() > time) {
            chr.dropMessage(5, "You may not use this item yet.");
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if (!FieldLimitType.PotionUse.check(chr.getMap().getFieldLimit())) {
            if (MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId()).applyTo(chr)) {
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
                if (chr.getMap().getConsumeItemCoolTime() > 0) {
                    chr.setNextConsume(time + chr.getMap().getConsumeItemCoolTime() * 1000);
                }
            }
        } else {
            c.getSession().write(CWvsContext.enableActions());
        }
    }

    public static final void PetChat(int petid, short command, String text, MapleCharacter chr) {
        if ((chr == null) || (chr.getMap() == null) || (chr.getPet(petid) == null)) {
            return;
        }
        chr.getMap().broadcastMessage(chr, PetPacket.petChat(chr.getId(), command, text, (byte) petid), true);
    }

    public static final void PetCommand(MaplePet pet, PetCommand petCommand, MapleClient c, MapleCharacter chr) {
        if (petCommand == null) {
            return;
        }
        byte petIndex = chr.getPetIndex(pet);
        boolean success = false;
        if (Randomizer.nextInt(99) <= petCommand.getProbability()) {
            success = true;
            if (pet.getCloseness() < 30000) {
                int newCloseness = pet.getCloseness() + petCommand.getIncrease() * c.getChannelServer().getTraitRate();
                if (newCloseness > 30000) {
                    newCloseness = 30000;
                }
                pet.setCloseness(newCloseness);
                if (newCloseness >= GameConstants.getClosenessNeededForLevel(pet.getLevel() + 1)) {
                    pet.setLevel(pet.getLevel() + 1);
                    c.getSession().write(CField.EffectPacket.showOwnPetLevelUp(petIndex));
                    chr.getMap().broadcastMessage(PetPacket.showPetLevelUp(chr, petIndex));
                }
                c.getSession().write(PetPacket.updatePet(pet, chr.getInventory(MapleInventoryType.CASH).getItem((short) (byte) pet.getInventoryPosition()), true));
            }
        }
        chr.getMap().broadcastMessage(PetPacket.commandResponse(chr.getId(), (byte) petCommand.getSkillId(), petIndex, success, false));
    }

    public static final void PetFood(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        int previousFullness = 100;
        MaplePet pet = null;
        if (chr == null) {
            return;
        }
        for (MaplePet pets : chr.getPets()) {
            if ((pets.getSummoned())
                    && (pets.getFullness() < previousFullness)) {
                previousFullness = pets.getFullness();
                pet = pets;
            }
        }

        if (pet == null) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }

        slea.readInt();
        short slot = slea.readShort();
        int itemId = slea.readInt();
        Item petFood = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
        if ((petFood == null) || (petFood.getItemId() != itemId) || (petFood.getQuantity() <= 0) || (itemId / 10000 != 212)) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        boolean gainCloseness = false;

        if (Randomizer.nextInt(99) <= 50) {
            gainCloseness = true;
        }
        if (pet.getFullness() < 100) {
            int newFullness = pet.getFullness() + 30;
            if (newFullness > 100) {
                newFullness = 100;
            }
            pet.setFullness(newFullness);
            byte index = chr.getPetIndex(pet);

            if ((gainCloseness) && (pet.getCloseness() < 30000)) {
                int newCloseness = pet.getCloseness() + 1;
                if (newCloseness > 30000) {
                    newCloseness = 30000;
                }
                pet.setCloseness(newCloseness);
                if (newCloseness >= GameConstants.getClosenessNeededForLevel(pet.getLevel() + 1)) {
                    pet.setLevel(pet.getLevel() + 1);

                    c.getSession().write(CField.EffectPacket.showOwnPetLevelUp(index));
                    chr.getMap().broadcastMessage(PetPacket.showPetLevelUp(chr, index));
                }
            }
            c.getSession().write(PetPacket.updatePet(pet, chr.getInventory(MapleInventoryType.CASH).getItem((short) (byte) pet.getInventoryPosition()), true));
            chr.getMap().broadcastMessage(c.getPlayer(), PetPacket.commandResponse(chr.getId(), (byte) 1, index, true, true), true);
        } else {
            if (gainCloseness) {
                int newCloseness = pet.getCloseness() - 1;
                if (newCloseness < 0) {
                    newCloseness = 0;
                }
                pet.setCloseness(newCloseness);
                if (newCloseness < GameConstants.getClosenessNeededForLevel(pet.getLevel())) {
                    pet.setLevel(pet.getLevel() - 1);
                }
            }
            c.getSession().write(PetPacket.updatePet(pet, chr.getInventory(MapleInventoryType.CASH).getItem((short) (byte) pet.getInventoryPosition()), true));
            chr.getMap().broadcastMessage(chr, PetPacket.commandResponse(chr.getId(), (byte) 1, chr.getPetIndex(pet), false, true), true);
        }
        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, true, false);
        c.getSession().write(CWvsContext.enableActions());
    }

    public static final void MovePet(LittleEndianAccessor slea, MapleCharacter chr) {
        if (chr == null) {
            return;
        }

        int petId = (int) slea.readLong();
        slea.skip(13);
        List<LifeMovementFragment> res = MovementParse.parseMovement(slea, 3);
        List<MapleMapObject> objects;
        if ((res != null) && (chr != null) && (res.size() != 0) && (chr.getMap() != null)) {
            MaplePet pet = chr.getPet(GameConstants.GMS ? chr.getPetIndex(petId) : petId);
            if (pet == null) {
                return;
            }
            pet.updatePosition(res);
            chr.getMap().broadcastMessage(chr, PetPacket.movePet(chr.getId(), petId, chr.getPetIndex(petId), res), false);
            if ((chr.hasBlockedInventory()) || (chr.getStat().pickupRange <= 0.0D) || (chr.inPVP())) {
                return;
            }
            chr.setScrolledPosition((short) 0);
            objects = chr.getMap().getMapObjectsInRange(chr.getTruePosition(), chr.getRange(), Arrays.asList(new MapleMapObjectType[]{MapleMapObjectType.ITEM}));
            for (LifeMovementFragment move : res) {
                Point pp = move.getPosition();
                boolean foundItem = false;
                for (MapleMapObject mapitemz : objects) {
                    if (((mapitemz instanceof MapleMapItem)) && ((Math.abs(pp.x - mapitemz.getTruePosition().x) <= chr.getStat().pickupRange) || (Math.abs(mapitemz.getTruePosition().x - pp.x) <= chr.getStat().pickupRange)) && ((Math.abs(pp.y - mapitemz.getTruePosition().y) <= chr.getStat().pickupRange) || (Math.abs(mapitemz.getTruePosition().y - pp.y) <= chr.getStat().pickupRange))) {
                        MapleMapItem mapitem = (MapleMapItem) mapitemz;
                        Lock lock = mapitem.getLock();
                        lock.lock();
                        try {
                            if (mapitem.isPickedUp()) {
                                lock.unlock();
                            } else if ((mapitem.getQuest() > 0) && (chr.getQuestStatus(mapitem.getQuest()) != 1)) {
                                lock.unlock();
                            } else if ((mapitem.getOwner() != chr.getId()) && (mapitem.isPlayerDrop())) {
                                lock.unlock();
                            } else if ((mapitem.getOwner() != chr.getId()) && (((!mapitem.isPlayerDrop()) && (mapitem.getDropType() == 0)) || ((mapitem.isPlayerDrop()) && (chr.getMap().getEverlast())))) {
                                lock.unlock();
                            } else if ((!mapitem.isPlayerDrop()) && ((mapitem.getDropType() == 1) || (mapitem.getDropType() == 3)) && (mapitem.getOwner() != chr.getId())) {
                                lock.unlock();
                            } else if ((mapitem.getDropType() == 2) && (mapitem.getOwner() != chr.getId())) {
                                lock.unlock();
                            } else if (mapitem.getMeso() > 0) {
                                if ((chr.getParty() != null) && (mapitem.getOwner() != chr.getId())) {
                                    List<MapleCharacter> toGive = new LinkedList();
                                    int splitMeso = mapitem.getMeso() * 40 / 100;
                                    for (MaplePartyCharacter z : chr.getParty().getMembers()) {
                                        MapleCharacter m = chr.getMap().getCharacterById(z.getId());
                                        if ((m != null) && (m.getId() != chr.getId())) {
                                            toGive.add(m);
                                        }
                                    }
                                    for (MapleCharacter m : toGive) {
                                        m.gainMeso(splitMeso / toGive.size(), true, true);
                                    }
                                    chr.gainMeso(mapitem.getMeso() - splitMeso, true, true);
                                } else {
                                    chr.gainMeso(mapitem.getMeso(), true, true);
                                }
                                InventoryHandler.removeItem_Pet(chr, mapitem, petId);
                                foundItem = true;
                            } else if (mapitem.getItem().getItemId() / 10000 != 291) {
                                if (InventoryHandler.useItem(chr.getClient(), mapitem.getItemId())) {
                                    InventoryHandler.removeItem_Pet(chr, mapitem, petId);
                                } else if (MapleInventoryManipulator.checkSpace(chr.getClient(), mapitem.getItem().getItemId(), mapitem.getItem().getQuantity(), mapitem.getItem().getOwner())) {
                                    if ((mapitem.getItem().getQuantity() >= 50) && (mapitem.getItem().getItemId() == 2340000)) {
                                        chr.getClient().setMonitored(true);
                                    }
                                    if (MapleInventoryManipulator.addFromDrop(chr.getClient(), mapitem.getItem(), true, mapitem.getDropper() instanceof MapleMonster)) {
                                        InventoryHandler.removeItem_Pet(chr, mapitem, petId);
                                        foundItem = true;
                                    }
                                }
                            }
                        } finally {
                            lock.unlock();
                        }
                    }
                }
                if (foundItem) {
                    return;
                }
            }
        }
    }
}