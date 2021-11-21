package userequipment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import types.CommunicatorType;
import types.EventType;

import java.io.InputStream;
import java.text.MessageFormat;
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

        communicator = new CommunicatorUE(CommunicatorType.USER_EQUIPMENT, ipBroker, portBroker, this.x, this.y);

        final String msg = MessageFormat.format("""
                        {0} started with parameters:
                        \tcommunications: {1}
                        \tposition: x={2} y={3}
                        \tmobility: {4}
                        \tsize: {5}
                        \tdelay: {6}
                        \tseed={7}""",
                CommunicatorType.USER_EQUIPMENT, communicator.toString(), x, y,
                mobilityDist.toString(), sizeDist.toString(), delayDist.toString(), seed);
        LOGGER.info(msg);
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
                    LOGGER.error("Execution completed");
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
        LOGGER.debug("Send {} with X={} Y={} SIZE={} DELAY={}", EventType.TRAFFIC_INGRESS, x, y, size, delay);
        x += mobilityDist.getRandom();
        y += mobilityDist.getRandom();
        size = sizeDist.getRandom();
        delay = delayDist.getRandom();
    }

}