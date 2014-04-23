package handling.world.guild;

import clientside.MapleCharacter;
import clientside.MapleClient;
import java.io.Serializable;

public class MapleGuildCharacter
        implements Serializable {

    public static final long serialVersionUID = 2058609046116597760L;
    private byte channel = -1;
    private byte guildrank;
    private byte allianceRank;
    private short level;
    private int id;
    private int jobid;
    private int guildid;
    private int guildContribution;
    private boolean online;
    private String name;

    public MapleGuildCharacter(MapleCharacter c) {
        this.name = c.getName();
        this.level = c.getLevel();
        this.id = c.getId();
        this.channel = ((byte) c.getClient().getChannel());
        this.jobid = c.getJob();
        this.guildrank = c.getGuildRank();
        this.guildid = c.getGuildId();
        this.guildContribution = c.getGuildContribution();
        this.allianceRank = c.getAllianceRank();
        this.online = true;
    }

    public MapleGuildCharacter(int id, short lv, String name, byte channel, int job, byte rank, int guildContribution, byte allianceRank, int gid, boolean on) {
        this.level = lv;
        this.id = id;
        this.name = name;
        if (on) {
            this.channel = channel;
        }
        this.jobid = job;
        this.online = on;
        this.guildrank = rank;
        this.allianceRank = allianceRank;
        this.guildContribution = guildContribution;
        this.guildid = gid;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(short l) {
        this.level = l;
    }

    public int getId() {
        return this.id;
    }

    public void setChannel(byte ch) {
        this.channel = ch;
    }

    public int getChannel() {
        return this.channel;
    }

    public int getJobId() {
        return this.jobid;
    }

    public void setJobId(int job) {
        this.jobid = job;
    }

    public int getGuildId() {
        return this.guildid;
    }

    public void setGuildId(int gid) {
        this.guildid = gid;
    }

    public void setGuildRank(byte rank) {
        this.guildrank = rank;
    }

    public byte getGuildRank() {
        return this.guildrank;
    }

    public void setGuildContribution(int c) {
        this.guildContribution = c;
    }

    public int getGuildContribution() {
        return this.guildContribution;
    }

    public boolean isOnline() {
        return this.online;
    }

    public String getName() {
        return this.name;
    }

    public void setOnline(boolean f) {
        this.online = f;
    }

    public void setAllianceRank(byte rank) {
        this.allianceRank = rank;
    }

    public byte getAllianceRank() {
        return this.allianceRank;
    }
}