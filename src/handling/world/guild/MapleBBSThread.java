package handling.world.guild;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class MapleBBSThread
        implements Serializable {

    public static final long serialVersionUID = 3565477792085301248L;
    public String name;
    public String text;
    public long timestamp;
    public int localthreadID;
    public int guildID;
    public int ownerID;
    public int icon;
    public Map<Integer, MapleBBSReply> replies = new HashMap();

    public MapleBBSThread(int localthreadID, String name, String text, long timestamp, int guildID, int ownerID, int icon) {
        this.localthreadID = localthreadID;
        this.name = name;
        this.text = text;
        this.timestamp = timestamp;
        this.guildID = guildID;
        this.ownerID = ownerID;
        this.icon = icon;
    }

    public final int getReplyCount() {
        return this.replies.size();
    }

    public final boolean isNotice() {
        return this.localthreadID == 0;
    }

    public static class ThreadComparator
            implements Comparator<MapleBBSThread>, Serializable {

        public int compare(MapleBBSThread o1, MapleBBSThread o2) {
            if (o1.localthreadID < o2.localthreadID) {
                return 1;
            }
            if (o1.localthreadID == o2.localthreadID) {
                return 0;
            }
            return -1;
        }
    }

    public static class MapleBBSReply
            implements Serializable {

        public int replyid;
        public int ownerID;
        public long timestamp;
        public String content;

        public MapleBBSReply(int replyid, int ownerID, String content, long timestamp) {
            this.ownerID = ownerID;
            this.replyid = replyid;
            this.content = content;
            this.timestamp = timestamp;
        }
    }
}