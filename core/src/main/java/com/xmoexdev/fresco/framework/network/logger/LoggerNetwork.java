package com.xmoexdev.fresco.framework.network.logger;

import dk.alexandra.fresco.framework.network.Network;

import java.io.PrintStream;
import java.util.Arrays;

public class LoggerNetwork implements Network {

    private final int myId;
    private final Network wrapped;
    private final PrintStream out;

    public LoggerNetwork(int myId, Network wrapped, PrintStream out) {
        this.myId = myId;
        this.wrapped = wrapped;
        this.out = out;
    }

    public LoggerNetwork(int myId, Network wrapped) {
        this(myId, wrapped, System.out);
    }

    @Override
    public void send(int partyId, byte[] data) {
        out.printf("%d -> %d: %s%n", myId, partyId, Arrays.toString(data));
        wrapped.send(partyId, data);
    }

    @Override
    public byte[] receive(int partyId) {
        byte[] data = wrapped.receive(partyId);
        out.printf("%d <- %d: %s%n", myId, partyId, Arrays.toString(data));
        return data;
    }

    @Override
    public int getNoOfParties() {
        return wrapped.getNoOfParties();
    }
}
