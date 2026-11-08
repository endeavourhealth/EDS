package org.endeavourhealth.queuereader.routines;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.GetResponse;
import org.endeavourhealth.core.queueing.ConnectionManager;
import org.endeavourhealth.core.queueing.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class SD865 {
    private static final Logger LOG = LoggerFactory.getLogger(SD865.class);

    public static void testRabbitNode(String node) {
        LOG.debug("Testing Rabbit node: "+node);

        try {
            Connection conn = createRabbitConnection(node);
            Channel channel = conn.createChannel();

            try {
                // non-durable, exclusive, auto-delete queue
                channel.queueDeclare("rabbitmq-java-test", false, true, true, null);
                channel.basicPublish("", "rabbitmq-java-test", null, "Hello, World".getBytes());

                GetResponse chResponse = channel.basicGet("rabbitmq-java-test", false);
                if (chResponse == null) {
                    System.out.println("No response received");
                } else {
                    byte[] body = chResponse.getBody();
                    System.out.println("Response received: " + new String(body));
                }
            }
            finally {
                channel.close();
                conn.close();
            }

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static Connection createRabbitConnection(String node) throws IOException, TimeoutException {

        //use standard Discovery Rabbit connection config but override the node with the test IP / DNS
        return ConnectionManager.getConnection(
                RabbitConfig.getInstance().getUsername(),
                RabbitConfig.getInstance().getPassword(),
                node,
                RabbitConfig.getInstance().getSslProtocol(),
                false);
    }
}