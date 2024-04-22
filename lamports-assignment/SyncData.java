import java.io.Serializable;

public class SyncData implements Serializable {
    public int timestamp;
    public int senderId;
    public Signal msgType;

    public SyncData(int timestamp, int senderId, Signal msgType) {
        this.timestamp = timestamp;
        this.senderId = senderId;
        this.msgType = msgType;
    }
}
