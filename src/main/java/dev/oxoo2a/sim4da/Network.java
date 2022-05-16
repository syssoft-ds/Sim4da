package dev.oxoo2a.sim4da;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class Network {
    
    private static final Random rgen = new Random();
    
    private final int n_nodes;
    private final Tracer tracer;
    private final MessageQueue[] mqueues;
    
    //private static Logger logger = Logger.getRootLogger();
    
    public Network(int n_nodes, Tracer tracer) {
        this.n_nodes = n_nodes;
        this.tracer = tracer;
        mqueues = new MessageQueue[n_nodes];
        for (int i = 0; i<n_nodes; ++i)
            mqueues[i] = new MessageQueue();
    }
    
    public int numberOfNodes() {
        return n_nodes;
    }
    
    public void unicast(int sender_id, int receiver_id, String message) {
        if (receiver_id < 0 || receiver_id >= n_nodes) {
            System.err.printf("Network::unicast: unknown receiver id %d\n", receiver_id);
            return;
        }
        if (sender_id < 0 || sender_id >= n_nodes) {
            System.err.printf("Network::unicast: unknown sender id %d\n", sender_id);
            return;
        }
        tracer.emit("Unicast:%d->%d",sender_id,receiver_id);
        Message raw = new Message(sender_id, receiver_id, MessageType.UNICAST, message);
        mqueues[receiver_id].put(raw);
    }
    
    public void broadcast(int sender_id, String message) {
        if (sender_id < 0 || sender_id >= n_nodes) {
            System.err.printf("Network::unicast: unknown sender id %d\n", sender_id);
            return;
        }
        tracer.emit("Broadcast:%d->0..%d",sender_id,n_nodes-1);
        Message raw = new Message(sender_id, -1, MessageType.BROADCAST, message);
        for ( int l=0; l<n_nodes; ++l) {
            if (l == sender_id) continue;
            raw.receiver_id = l;
            mqueues[l].put(raw);
        }
    }
    
    public Message receive(int receiver_id) {
        if (receiver_id < 0 || receiver_id >= n_nodes) {
            System.err.printf("Network::unicast: unknown receiver id %d\n", receiver_id);
            return null;
        }
        Message m = mqueues[receiver_id].await();
        if (m != null) {
            String m_type = m.type == MessageType.BROADCAST ? "Broadcast" : "Unicast";
            tracer.emit("Receive %s:%d<-%d",m_type,m.receiver_id,m.sender_id);
        }
        return m;
    }
    
    public void stop() {
        for (MessageQueue mq : mqueues)
            mq.stop();
    }
    
    public static class Message {
        public int sender_id;
        public int receiver_id;
        public MessageType type;
        public String payload;
        public Message(int sender_id, int receiver_id, MessageType type, String payload) {
            this.sender_id = sender_id;
            this.receiver_id = receiver_id;
            this.type = type;
            this.payload = payload;
        }
        @Override
        public String toString() {
            String r = "Network::Message(sender="+sender_id+",receiver="+receiver_id+",";
            r += type == MessageType.BROADCAST ? "Broadcast" : "Unicast";
            r += ",payload=<"+payload+">)";
            return r;
        }
    }
    
    private static class MessageQueue {
        private final LinkedList<Message> queue;
        private final Semaphore await_message;
        private boolean stop;
        public MessageQueue() {
            queue = new LinkedList<>();
            await_message = new Semaphore(0);
            stop = false;
        }
        public void put(Message r) {
            synchronized (queue) {
                queue.addLast(r);
                await_message.release();
            }
        }
        public Message await() {
            while (true) {
                if (stop) return null;
                try {
                    await_message.acquire();
                    synchronized (queue) {
                        if (!queue.isEmpty()) {
                            // Return a random message in queue avoiding FIFO order
                            if (queue.size() == 1)
                                return queue.removeFirst();
                            int c = rgen.nextInt(queue.size());
                            return queue.remove(c);
                        }
                    }
                } catch (InterruptedException ignored) {}
            }
        }
        public void stop() {
            stop = true;
            await_message.release();
        }
    }
    
    public enum MessageType {
        UNICAST, BROADCAST
    }
}
