package dev.oxoo2a.sim4da.clock;


import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * LogicClock is instantiated for each TimedNode. A LogicClock saves its NodeID, timestamp, and temporary timestamps
 * as received from other Nodes to synchronize. Further data and functionality is handled by Subclasses LamportClock/VectorClock.
 */

public abstract class LogicClock {

    protected int nodeId;
    protected int time;
    public final ClockType type;
    protected HashMap<Integer, Integer> tempTimestamps = new HashMap<>();

    public LogicClock(int nodeId, ClockType type){
        this.nodeId = nodeId;
        this.type = type;
        this.time =0;
    }

    public ClockType getType(){
        return this.type;
    }


    public void tick(){
        this.time++;
    }

    /**
     * Parent Method to synchronize a Clock with a Time String as received in a Message (raw payload).
     * synchronize() utilizes String Tokenization to extract all Timestamps from the Payload string. A Timestamp in the
     * Payload always leads with the Characters '%T' + NodeID.
     * The IDs and Times are saved in a Hashmap 'tempTimestamps' that is cleared with every new call of the method.
     * tempTimestamps is used in the child class method to access all timestamps. When using Lamport time,
     * the tempTimestamps Map always contains exactly one entry.
     * Subclasses Override this Function by adding statements regarding the handling of the extracted Time information.
     * @param payload the entire Payload string from the Message
     */
    public void synchronize(String payload){
        //First Tokenizer to collect all entrys from the payload
        StringTokenizer tokenizer = new StringTokenizer(payload, ",");
        // clear temporary timestamp field.
        tempTimestamps.clear();
        while (tokenizer.hasMoreTokens()){
            int senderId = -1;
            String token = tokenizer.nextToken();
            String s = "";
            // if token is marked as containing a timestamp
            if (token.contains("%T")) {
                // Seconds Tokenizer splits entry into Node ID and associated timestamp
                StringTokenizer subTokenizer = new StringTokenizer(token, ":");
                s = subTokenizer.nextToken();
                for (int i = 0; i < s.length(); i++) {
                    if (s.charAt(i) == 'T') {
                        senderId = Integer.parseInt(s.substring(i + 1, s.length() - 1));
                        break;
                    }

                }

                s = subTokenizer.nextToken();
                int senderTime = Integer.parseInt(s.substring(1, s.length()-1));
                tempTimestamps.put(senderId, senderTime);
            }
        }

    }
    protected void printTimeStamps(){}
    public int getTime(){
        return this.time;
    }

}
