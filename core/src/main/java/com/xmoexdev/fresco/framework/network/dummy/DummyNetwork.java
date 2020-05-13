package com.xmoexdev.fresco.framework.network.dummy;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class DummyNetwork implements Network {
    private final Map<Pair<Integer, Integer>, Queue<byte[]>> store;
    private final int myId;
    private final int noOfPerties;

    public DummyNetwork(Map<Pair<Integer, Integer>, Queue<byte[]>> store, int myId, int numParties) {
        this.store = store;
        this.myId = myId;
        this.noOfPerties = numParties;
    }

    @Override
    public void send(int toId, byte[] data) {
        final Pair<Integer, Integer> id = new Pair<>(myId, toId);
        synchronized (store) {
            if (!store.containsKey(id)) {
                store.put(id, new LinkedList<>());
            }
            store.get(id).add(data);
        }
    }

    @Override
    public byte[] receive(int fromId) {
        final Pair<Integer, Integer> id = new Pair<>(fromId, myId);
        for (int i = 0; i < 10; i++) {
            synchronized (store) {
                if (store.containsKey(id)) {
                    Queue<byte[]> queue = store.get(id);
                    if (!queue.isEmpty()) {
                        return queue.poll();
                    }
                }
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public int getNoOfParties() {
        return noOfPerties;
    }
}
