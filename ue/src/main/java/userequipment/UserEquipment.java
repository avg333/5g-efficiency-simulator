package userequipment;

import communication.Communicator;
import communication.CommunicatorTCP;
import communication.CommunicatorUDP;
import distribution.Distribution;
import distribution.DistributionMode;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import types.EntityType;
import types.EventType;

import java.io.InputStream;
import java.util.Properties;

public class UserEquipment extends Thread {
    private static final String PROP_FILE_NAME = "config.properties";
    private static final int MSG_LEN = 10;

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Communicator communicator;
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
            log.error("Error loading the properties. Execution completed", e);
            System.exit(-1);
        }

        final char sizeDistributionModeChar = prop.getProperty("sizeDistributionMode").charAt(0);
        final DistributionMode sizeDistributionMode = DistributionMode.getDistributionModeByCode(sizeDistributionModeChar);
        final double sizeDistributionParam1 = Double.parseDouble(prop.getProperty("sizeDistributionParam1"));
        final double sizeDistributionParam2 = Double.parseDouble(prop.getProperty("sizeDistributionParam2"));
        final char delayDistributionModeChar = prop.getProperty("delayDistributionMode").charAt(0);
        final DistributionMode delayDistributionMode = DistributionMode.getDistributionModeByCode(delayDistributionModeChar);
        final double delayDistributionParam1 = Double.parseDouble(prop.getProperty("delayDistributionParam1"));
        final double delayDistributionParam2 = Double.parseDouble(prop.getProperty("delayDistributionParam2"));
        final char mobilityDistributionModeChar = prop.getProperty("mobilityDistributionMode").charAt(0);
        final DistributionMode mobilityDistributionMode = DistributionMode.getDistributionModeByCode(mobilityDistributionModeChar);
        final double mobilityDistributionParam1 = Double.parseDouble(prop.getProperty("mobilityDistributionParam1"));
        final double mobilityDistributionParam2 = Double.parseDouble(prop.getProperty("mobilityDistributionParam2"));
        final long seed = Long.parseLong(prop.getProperty("seed"));

        final String ipBroker = prop.getProperty("ipBroker");
        final int portBroker = Integer.parseInt(prop.getProperty("portBroker"));
        final boolean communicatorModeTCP = Boolean.parseBoolean(prop.getProperty("tcp"));
        x = Double.parseDouble(prop.getProperty("x"));
        y = Double.parseDouble(prop.getProperty("y"));
        sizeDist = new Distribution(sizeDistributionMode, sizeDistributionParam1, sizeDistributionParam2);
        delayDist = new Distribution(delayDistributionMode, delayDistributionParam1, delayDistributionParam2);
        mobilityDist = new Distribution(mobilityDistributionMode, mobilityDistributionParam1, mobilityDistributionParam2);

        if (seed != 0) {
            sizeDist.setSeed(seed);
            delayDist.setSeed(seed + 1);
            mobilityDist.setSeed(seed + 2);
        }

        log.info("Started in position [x={} y={}] with distributions [seed={}]:\n" +
                "\tsize:[{}], delay:[{}], mobility:[{}]", x, y, seed, sizeDist, delayDist, mobilityDist);

        communicator = (communicatorModeTCP) ?
                new CommunicatorTCP(EntityType.USER_EQUIPMENT, ipBroker, portBroker, x, y) :
                new CommunicatorUDP(EntityType.USER_EQUIPMENT, ipBroker, portBroker, x, y);

        log.info("Registered in {}", communicator);
    }

    public static void main(String[] args) {
        new UserEquipment().start();
    }

    @Override
    public void run() {

        delay = delayDist.getRandom();

        while (true) {
            final EventType action = receiveActionType();
            log.debug("Received request for {}", action);

            switch (action) {
                case TRAFFIC_INGRESS -> processTrafficIngress();
                case CLOSE -> {
                    communicator.close();
                    log.info("Execution completed");
                    return;
                }
                default -> {
                    log.error("Type {} not supported. Execution completed", action);
                    communicator.close();
                    System.exit(-1);
                }
            }
        }

    }

    private void processTrafficIngress() {
        sendTask(x, y, size, delay);
        log.debug("Generated task with SIZE={} DELAY={} in position X={} Y={} ", size, delay, x, y);
        x += mobilityDist.getRandom();
        y += mobilityDist.getRandom();
        size = sizeDist.getRandom();
        delay = delayDist.getRandom();
    }

    private EventType receiveActionType() {
        try (final MessageUnpacker messageUnpacker = communicator.receiveMessage(MSG_LEN)) {
            final int action = messageUnpacker.unpackInt();
            return EventType.getActionTypeByCode(action);
        } catch (Exception e) {
            log.error("Error unpacking the message. Execution completed", e);
            communicator.close();
            System.exit(-1);
        }
        return null;
    }

    private void sendTask(final Double x, final Double y, final Double size, final Double delay) {
        try (final MessageBufferPacker messageBufferPacker = MessagePack.newDefaultBufferPacker()) {
            messageBufferPacker.packDouble(x);
            messageBufferPacker.packDouble(y);
            messageBufferPacker.packDouble(size);
            messageBufferPacker.packDouble(delay);
            communicator.sendMessage(messageBufferPacker);
        } catch (Exception e) {
            log.error("Error packing the message. Execution completed", e);
            communicator.close();
            System.exit(-1);
        }
    }

}