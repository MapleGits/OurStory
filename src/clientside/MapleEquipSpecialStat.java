/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package clientside;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author Itzik
 */
public enum MapleEquipSpecialStat {

    TOTAL_DAMAGE(0x1),
    ALL_STAT(0x2),
    KARMA_COUNT(0x4),
    UNK8(0x8), //long
    UNK10(0x10); //int

    private final int value;

    private MapleEquipSpecialStat(int value) {
        this.value = value;
    }

    public final int getValue() {
        return value;
    }
}
