package userequipment;

import communication.CommunicatorTCP;
import communication.CommunicatorUDP;
import communication.CommunicatorUE;
import distribution.Distribution;
import distribution.DistributionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import types.EntityType;
import types.EventType;

import java.io.InputStream;
import java.util.Properties;

public class UserEquipment extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserEquipment.class);
    private static final String PROP_FILE_NAME = "config.properties";

    private final CommunicatorUE communicator;
    private final Distribution mobilityDist;
    private final Distribution sizeDist;
    private final Distribution delayDist;
    private double x;
    private double y;
    private double size = -1.0;
    private double delay = 0.0;

    public UserEquipment() {
        final Properties prop = new Properties();

        try (final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PROP_FILE_NAME)) {
            prop.load(inputStream);
        } catch (Exception e) {
            LOGGER.error("Error loading the properties. Execution completed", e);
            System.exit(-1);
        }

        final char mobilityDistributionModeChar = prop.getProperty("mobilityDistributionMode").charAt(0);
        final DistributionMode mobilityDistributionMode = DistributionMode.getDistributionModeByCode(mobilityDistributionModeChar);
        final double mobilityDistributionParam1 = Double.parseDouble(prop.getProperty("mobilityDistributionParam1"));
        final double mobilityDistributionParam2 = Double.parseDouble(prop.getProperty("mobilityDistributionParam2"));
        final char sizeDistributionModeChar = prop.getProperty("sizeDistributionMode").charAt(0);
        final DistributionMode sizeDistributionMode = DistributionMode.getDistributionModeByCode(sizeDistributionModeChar);
        final double sizeDistributionParam1 = Double.parseDouble(prop.getProperty("sizeDistributionParam1"));
        final double sizeDistributionParam2 = Double.parseDouble(prop.getProperty("sizeDistributionParam2"));
        final char delayDistributionModeChar = prop.getProperty("delayDistributionMode").charAt(0);
        final DistributionMode delayDistributionMode = DistributionMode.getDistributionModeByCode(delayDistributionModeChar);
        final double delayDistributionParam1 = Double.parseDouble(prop.getProperty("delayDistributionParam1"));
        final double delayDistributionParam2 = Double.parseDouble(prop.getProperty("delayDistributionParam2"));
        final long seed = Long.parseLong(prop.getProperty("seed"));

        final String ipBroker = prop.getProperty("ipBroker");
        final int portBroker = Integer.parseInt(prop.getProperty("portBroker"));
        final boolean communicatorModeTCP = Boolean.parseBoolean(prop.getProperty("tcp"));
        x = Double.parseDouble(prop.getProperty("x"));
        y = Double.parseDouble(prop.getProperty("y"));
        mobilityDist = new Distribution(mobilityDistributionMode, mobilityDistributionParam1, mobilityDistributionParam2);
        sizeDist = new Distribution(sizeDistributionMode, sizeDistributionParam1, sizeDistributionParam2);
        delayDist = new Distribution(delayDistributionMode, delayDistributionParam1, delayDistributionParam2);

        if (seed != 0) {
            mobilityDist.setSeed(seed);
            sizeDist.setSeed(seed);
            delayDist.setSeed(seed);
        }
        communicator = (communicatorModeTCP) ?
                new CommunicatorUE(new CommunicatorTCP(EntityType.USER_EQUIPMENT, ipBroker, portBroker, x, y)) :
                new CommunicatorUE(new CommunicatorUDP(EntityType.USER_EQUIPMENT, ipBroker, portBroker, x, y));

        LOGGER.info("Started");
        LOGGER.info("communicator: {}", communicator);
        LOGGER.info("position: x={} y={}", x, y);
        LOGGER.info("mobility: {}", mobilityDist);
        LOGGER.info("size: {}", sizeDist);
        LOGGER.info("delay: {}", delayDist);
        LOGGER.info("seed: {}", seed);
    }

    public static void main(String[] args) {
        new UserEquipment().start();
    }

    @Override
    public void run() {

        delay = delayDist.getRandom();

        while (true) {
            final EventType action = communicator.receiveActionType();
            LOGGER.debug("Received request for {}", action);

            switch (action) {
                case TRAFFIC_INGRESS -> processTrafficIngress();
                case CLOSE -> {
                    communicator.close();
                    LOGGER.info("Execution completed");
                    return;
                }
                default -> {
                    LOGGER.error("Type {} not supported. Execution completed", action);
                    communicator.close();
                    System.exit(-1);
                }
            }
        }

    }

    private void processTrafficIngress() {
        communicator.sendTask(x, y, size, delay);
        LOGGER.debug("Generated task with SIZE={} DELAY={} in position X={} Y={} ", size, delay, x, y);
        x += mobilityDist.getRandom();
        y += mobilityDist.getRandom();
        size = sizeDist.getRandom();
        delay = delayDist.getRandom();
    }

}