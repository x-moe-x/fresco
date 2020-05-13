package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.demo.cli.CmdLineUtil;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.real.RealNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * A demo computing the 3 dimensional distance (center angle) between two points identified by latitude and longitude.
 */
public class Distance3DDemo implements Application<BigDecimal, ProtocolBuilderNumeric> {
    private static final Logger log = LoggerFactory.getLogger(DistanceDemo.class);

    private final int id;
    private final double theta;
    private final double lambda;

    /**
     * Construct a new DistanceDemo.
     *
     * @param id     The party id
     * @param theta  The latitude coordinate
     * @param lambda The longitude coordinate
     */
    Distance3DDemo(int id, double theta, double lambda) {
        this.id = id;
        this.theta = theta;
        this.lambda = lambda;
    }

    @Override
    public DRes<BigDecimal> buildComputation(ProtocolBuilderNumeric producer) {
        return producer.par(par -> {
            // Input points
            RealNumeric numericIo = par.realNumeric();

            DRes<SReal> sinThetaA = (id == 1)
                    ? numericIo.input(Math.sin(theta), 1) : numericIo.input(0.0, 1);
            DRes<SReal> cosThetaA = (id == 1)
                    ? numericIo.input(Math.cos(theta), 1) : numericIo.input(0.0, 1);
            DRes<SReal> sinLambdaA = (id == 1)
                    ? numericIo.input(Math.sin(lambda), 1) : numericIo.input(0.0, 1);
            DRes<SReal> cosLambdaA = (id == 1)
                    ? numericIo.input(Math.cos(lambda), 1) : numericIo.input(0.0, 1);

            DRes<SReal> sinThetaB = (id == 2)
                    ? numericIo.input(Math.sin(theta), 2) : numericIo.input(0.0, 2);
            DRes<SReal> cosThetaB = (id == 2)
                    ? numericIo.input(Math.cos(theta), 2) : numericIo.input(0.0, 2);
            DRes<SReal> sinLambdaB = (id == 2)
                    ? numericIo.input(Math.sin(lambda), 2) : numericIo.input(0.0, 2);
            DRes<SReal> cosLambdaB = (id == 2)
                    ? numericIo.input(Math.cos(lambda), 2) : numericIo.input(0.0, 2);

            Pair<Pair<DRes<SReal>, DRes<SReal>>, Pair<Pair<DRes<SReal>, DRes<SReal>>, Pair<Pair<DRes<SReal>, DRes<SReal>>, Pair<DRes<SReal>, DRes<SReal>>>>> inputs =
                    new Pair<>(
                            new Pair<>(sinThetaA, sinThetaB),
                            new Pair<>(
                                    new Pair<>(cosThetaA, cosThetaB),
                                    new Pair<>(
                                            new Pair<>(sinLambdaA, sinLambdaB),
                                            new Pair<>(cosLambdaA, cosLambdaB)
                                    )));

            return () -> inputs;
        }).pairInPar(
                // sin(thetaA) * sin(thetaB)
                (seq, sinThetaAndRest) -> seq.realNumeric().mult(sinThetaAndRest.getFirst().getFirst(), sinThetaAndRest.getFirst().getSecond()),
                (seq, sinThetaAndRest) -> seq.par(par -> sinThetaAndRest::getSecond).pairInPar(
                        // cos(thetaA) * cos(thetaB)
                        (seq2, cosThetaAndRest) -> seq2.realNumeric().mult(cosThetaAndRest.getFirst().getFirst(), cosThetaAndRest.getFirst().getSecond()),
                        // cos(lambdaB - lambdaA) (rewritten to operate on precomputed sin and cos values only)
                        (seq2, cosThetaAndRest) -> seq2.par(par -> cosThetaAndRest::getSecond).pairInPar(
                                // sin(lambdaA) * cos(lambdaA)
                                (seq3, sinCosLambda) -> seq3.realNumeric().mult(sinCosLambda.getFirst().getFirst(), sinCosLambda.getFirst().getSecond()),
                                // sin(lambdaB) * cos(lambdaB)
                                (seq3, sinCosLambda) -> seq3.realNumeric().mult(sinCosLambda.getSecond().getFirst(), sinCosLambda.getSecond().getSecond())
                        ).seq((seq3, summands) -> seq3.realNumeric().add(summands.getFirst(), summands.getSecond()))
                ).seq((seq2, factors) -> seq2.realNumeric().mult(factors.getFirst(), factors.getSecond()))
        ).seq((seq, summands) -> seq.realNumeric().add(summands.getFirst(), summands.getSecond()))
                .seq((seq, cosCenterAngle) -> seq.realNumeric().open(cosCenterAngle));
    }

    /**
     * Main method for Distance3DDemo.
     *
     * @param args Arguments for the application
     * @throws IOException In case of network problems
     */
    public static <ResourcePoolT extends ResourcePool> void main(String[] args) throws IOException {
        CmdLineUtil<ResourcePoolT, ProtocolBuilderNumeric> cmdUtil = new CmdLineUtil<>();
        cmdUtil.addOption(Option.builder("theta").desc("The latitude coordinate of this party. "
                + "Note oply party 1 and 2 should supply this input.").hasArg().build());
        cmdUtil.addOption(Option.builder("lambda").desc("The longitude coordinate of this party. "
                + "Note oply party 1 and 2 should supply this input.").hasArg().build());
        CommandLine cmd = cmdUtil.parse(args);
        NetworkConfiguration networkConfiguration = cmdUtil.getNetworkConfiguration();

        if (networkConfiguration.getMyId() != 1 && networkConfiguration.getMyId() != 2) {
            if (cmd.hasOption("theta") || cmd.hasOption("lambda")) {
                throw new IllegalArgumentException("Only party 1 and 2 should submit input");
            }
        }

        if (!cmd.hasOption("theta") || !cmd.hasOption("lambda")) {
            cmdUtil.displayHelp();
            throw new IllegalArgumentException("Party 1 and 2 must submit input");
        }
        double theta_degrees = Double.parseDouble(cmd.getOptionValue("theta"));
        double lambda_degrees = Double.parseDouble(cmd.getOptionValue("lambda"));
        final double toRad = Math.PI / 180;

        Distance3DDemo distDemo = new Distance3DDemo(networkConfiguration.getMyId(), theta_degrees * toRad, lambda_degrees * toRad);
        SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce = cmdUtil.getSce();
        ResourcePoolT resourcePool = cmdUtil.getResourcePool();
        BigDecimal bigDecimal = sce.runApplication(distDemo, resourcePool, cmdUtil.getNetwork());
        double centerAngle = Math.acos(bigDecimal.doubleValue());
        log.info("Center angle between party 1 and 2 is: " + centerAngle + " rad");
        cmdUtil.closeNetwork();
        sce.shutdownSCE();
    }
}
