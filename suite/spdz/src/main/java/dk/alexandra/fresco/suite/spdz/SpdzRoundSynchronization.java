package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.ProtocolCollectionList;
import dk.alexandra.fresco.framework.sce.evaluator.SceNetwork;
import dk.alexandra.fresco.suite.ProtocolSuite.RoundSynchronization;
import dk.alexandra.fresco.suite.spdz.gates.SpdzMacCheckProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzOutputProtocol;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;

/**
 * A default implementation of the round synchronization for spdz - mostly doing the MAC check if
 * needed.
 */
public class SpdzRoundSynchronization implements RoundSynchronization<SpdzResourcePool> {

  private static final int macCheckThreshold = 100000;
  private int gatesEvaluated = 0;
  private boolean doMacCheck = false;

  private void doMACCheck(SpdzResourcePool resourcePool) {
    SceNetwork sceNetwork =
        new SceNetwork(
            resourcePool.getNoOfParties(),
            resourcePool.getNetwork());
    SpdzStorage storage = resourcePool.getStore();
    int batchSize = 128;

    //Ensure that we have any values to do MAC check on
    if (!storage.getOpenedValues().isEmpty()) {
      SpdzMacCheckProtocol macCheck = new SpdzMacCheckProtocol(resourcePool.getSecureRandom(),
          resourcePool.getMessageDigest(), storage, null, resourcePool.getModulus());

      do {
        ProtocolCollectionList<SpdzResourcePool> protocolCollectionList =
            new ProtocolCollectionList<>(batchSize);
        macCheck.getNextProtocols(protocolCollectionList);
        BatchEvaluationStrategy<SpdzResourcePool> batchStrat = new BatchedStrategy<>();
        batchStrat.processBatch(protocolCollectionList, resourcePool, sceNetwork);
      } while (macCheck.hasNextProtocols());
    }
  }

  @Override
  public void finishedEval(SpdzResourcePool resourcePool) {
    doMACCheck(resourcePool);
  }

  @Override
  public void finishedBatch(int gatesEvaluated, SpdzResourcePool resourcePool) {
    this.gatesEvaluated += gatesEvaluated;
    if (this.gatesEvaluated > macCheckThreshold || doMacCheck) {
      doMACCheck(resourcePool);
      doMacCheck = false;
      this.gatesEvaluated = 0;
    }
  }

  @Override
  public void beforeBatch(
      ProtocolCollection<SpdzResourcePool> protocols, SpdzResourcePool resourcePool) {
    // If an output gate resides within the next batch, we need to do a MAC check on all previous
    // gates which lead to this output gate.
    protocols.forEach(p -> {
      if (p instanceof SpdzOutputProtocol) {
        doMacCheck = true;
      }
    });
    if (doMacCheck) {
      doMACCheck(resourcePool);
    }
  }
}
