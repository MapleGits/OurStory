/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client.messages.commands;

import constants.ServerConstants.PlayerGMRank;

/**
 *
 * @author Emilyx3
 */
public class SuperDonatorCommand {

    public static PlayerGMRank getPlayerLevelRequired() {
        return PlayerGMRank.SUPERDONATOR;
    }
}
