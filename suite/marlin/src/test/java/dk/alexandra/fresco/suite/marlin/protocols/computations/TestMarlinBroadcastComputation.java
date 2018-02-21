package dk.alexandra.fresco.suite.marlin.protocols.computations;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.marlin.AbstractMarlinTest;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt128;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt128Factory;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt64;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Test;

public class TestMarlinBroadcastComputation extends AbstractMarlinTest<UInt64, UInt64, CompUInt128> {

  @Test
  public void testBroadcast() {
    runTest(new TestTest<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2,
        false);
  }

  @Test
  public void testBroadcastThree() {
    runTest(new TestTest<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 3,
        false);
  }

  @Override
  protected CompUIntFactory<UInt64, UInt64, CompUInt128> createFactory() {
    return new CompUInt128Factory();
  }

  private static class TestTest<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          int noParties = conf.getResourcePool().getNoOfParties();
          List<byte[]> inputs = new ArrayList<>();
          Random random = new Random(42);
          for (int i = 1; i <= noParties; i++) {
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            inputs.add(bytes);
          }
          Application<List<byte[]>, ProtocolBuilderNumeric> testApplication =
              root -> new MarlinBroadcastComputation(
                  inputs.get(root.getBasicNumericContext().getMyId() - 1)).buildComputation(root);
          List<byte[]> actual = runApplication(testApplication);
          assertEquals(inputs.size(), actual.size());
          for (int i = 0; i < actual.size(); i++) {
            assertArrayEquals(inputs.get(i), actual.get(i));
          }
        }
      };
    }
  }

}