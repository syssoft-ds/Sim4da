package dev.oxoo2a.sim4da.times;

import dev.oxoo2a.sim4da.Time;

import java.util.Arrays;

/***
 * Implements the rules of the vector time
 */
public class VectorTime implements Time {

    private int[] time;
    private final int myId;

    public VectorTime(int myId, int n_nodes){
        time = new int[n_nodes];
        for(int i = 0; i < n_nodes; i++){
            time[i] = 0;
        }
        this.myId = myId;
    }

    @Override
    public void incrementMyTime() {
        time[myId]++;
    }

    @Override
    public String toString() {
        return Arrays.toString(time);
    }

    @Override
    public void updateTime(String time_sender) {
        // increment local time
        incrementMyTime();

        time_sender = time_sender.substring(1, time_sender.length()-1);
        String[] arr = time_sender.split(", ");
        // max function
        for(int i = 0; i < arr.length; i++){
            int sender_i_int = Integer.parseInt(arr[i]);
            if(sender_i_int > time[i])
                time[i] = sender_i_int;
        }
    }

    @Override
    public void updateTime(Time time_sender) {
        if(!( time_sender instanceof VectorTime))
            throw new IllegalArgumentException("Wrong time format");
        VectorTime v_time_sender = (VectorTime) time_sender;
        for(int i = 0; i < time.length; i++){
            if(v_time_sender.time[i] > time[i])
                time[i] = v_time_sender.time[i];
        }
    }
}
