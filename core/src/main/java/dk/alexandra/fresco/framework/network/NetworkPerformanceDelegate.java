package dk.alexandra.fresco.framework.network;

import dk.alexandra.fresco.framework.PerformanceLogger;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.util.Pair;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NetworkPerformanceDelegate extends PerformanceLogger implements Network {

  private Network network;
  private ConcurrentMap<Integer, Pair<Integer, Integer>> networkLogger = new ConcurrentHashMap<>();
  private int minBytesReceived = Integer.MAX_VALUE;
  private int maxBytesReceived = 0;

  public NetworkPerformanceDelegate(Network network, int myId) {
    super(myId);
    this.network = network;
  }

  @Override
  public byte[] receive(int channelId, int partyId) throws IOException {
    byte[] res = this.network.receive(channelId, partyId);
    int noBytes = res.length;
    if (!networkLogger.containsKey(partyId)) {
      networkLogger.put(partyId, new Pair<>(1, noBytes));
    } else {
      Pair<Integer, Integer> p = networkLogger.get(partyId);
      networkLogger.put(partyId, new Pair<>(p.getFirst() + 1, p.getSecond() + noBytes));
    }

    if (minBytesReceived > noBytes) {
      minBytesReceived = noBytes;
    }
    if (maxBytesReceived < noBytes) {
      maxBytesReceived = noBytes;
    }
    return res;
  }

  @Override
  public void init(NetworkConfiguration conf, int channelAmount) {
    this.network.init(conf, channelAmount);
  }

  @Override
  public void connect(int timeoutMillis) throws IOException {
    this.network.connect(timeoutMillis);
  }

  @Override
  public void send(int channelId, int partyId, byte[] data) throws IOException {
    this.network.send(channelId, partyId, data);
  }

  @Override
  public void close() throws IOException {
    this.network.close();
  }

  @Override
  public void printPerformanceLog() {
    log.info("=== P"+this.myId+": Network logged - results ===");
    if (networkLogger.isEmpty()) {
      log.info("No network activity logged");
    } else {
      long totalNoBytes = 0;
      int noNetworkBatches = 0;
      for (Integer partyId : networkLogger.keySet()) {
        Pair<Integer, Integer> p = networkLogger.get(partyId);
        log.info("Received " + p.getSecond() + " bytes from party " + partyId);
        totalNoBytes += p.getSecond();
        noNetworkBatches += p.getFirst();
      }
      log.info("Received data " + noNetworkBatches + " times in total (including from ourselves)");
      log.info("Total amount of bytes received: " + totalNoBytes);
      log.info("Minimum amount of bytes received: " + minBytesReceived);
      log.info("maximum amount of bytes received: " + maxBytesReceived);
      double avg = totalNoBytes / (double) noNetworkBatches;
      log.info("Average amount of bytes received: " + df.format(avg));
    }
  }

  @Override
  public void reset() {
    networkLogger.clear();
    minBytesReceived = Integer.MAX_VALUE;
    maxBytesReceived = 0;
  }
}
