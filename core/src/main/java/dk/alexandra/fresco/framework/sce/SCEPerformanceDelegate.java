package dk.alexandra.fresco.framework.sce;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.PerformanceLogger;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class SCEPerformanceDelegate<
  ResourcePoolT extends ResourcePool, 
  Builder extends ProtocolBuilder
  >
  extends PerformanceLogger
  implements SecureComputationEngine<ResourcePoolT, Builder> {

  private SecureComputationEngine<ResourcePoolT, Builder> sce;
  private String protocolSuiteName;
  private List<RuntimeInfo> runtimeLogger = new ArrayList<>();

  public SCEPerformanceDelegate(SecureComputationEngine<ResourcePoolT, Builder> sce,
      ProtocolSuite<ResourcePoolT, Builder> suite, int myId) {
    super(myId);
    this.sce = sce;    
    this.protocolSuiteName = suite.getClass().getName();
  }

  @Override
  public <OutputT> OutputT runApplication(Application<OutputT, Builder> application,
      ResourcePoolT resources) {
    long then = System.currentTimeMillis();
    OutputT res = this.sce.runApplication(application, resources);
    long now = System.currentTimeMillis();
    long timeSpend = now - then;
      this.runtimeLogger.add(new RuntimeInfo(application, timeSpend, protocolSuiteName));
    return res;
  }

  @Override
  public <OutputT> Future<OutputT> startApplication(Application<OutputT, Builder> application,
      ResourcePoolT resources) {
    // TODO: If applications are started this way, no running time logging is applied. We need to
    // inject a decorator future which logs the timing before handing over the result.
    return this.sce.startApplication(application, resources);
  }

  @Override
  public void setup() {
    this.sce.setup();
  }

  @Override
  public void shutdownSCE() {
    this.sce.shutdownSCE();
  }

  @Override
  public void printPerformanceLog() {
    log.info("=== P"+this.myId+": Running times for applications ===");
    if (this.runtimeLogger.isEmpty()) {
      log.info("No applications were run, or they have not completed yet.");
    }
    for (RuntimeInfo info : this.runtimeLogger) {
      log.info("Protocol suite used: " + info.protocolSuite);
      log.info(
          "The application " + info.app.getClass().getName() + " took " + info.timeSpend + "ms.");
    }
  }
  
  private class RuntimeInfo {
    public Application<?, ?> app;
    public long timeSpend;
    public String protocolSuite;

    public RuntimeInfo(Application<?, ?> app, long timeSpend,
        String protocolSuite) {
      super();
      this.app = app;
      this.timeSpend = timeSpend;
      this.protocolSuite = protocolSuite;
    }

  }

  @Override
  public void reset() {
    this.runtimeLogger.clear();
  }
}
