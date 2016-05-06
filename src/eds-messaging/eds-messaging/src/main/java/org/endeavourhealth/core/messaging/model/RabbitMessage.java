package org.endeavourhealth.core.messaging.model;

import com.rabbitmq.client.AMQP;

import java.io.UnsupportedEncodingException;

public class RabbitMessage extends Message
{
    public static RabbitMessage fromRabbitMessage(AMQP.BasicProperties properties, byte[] bytes) throws UnsupportedEncodingException {
        RabbitMessage message = new RabbitMessage();
        message.body = new String(bytes, "UTF-8");

        for (String headerName : properties.getHeaders().keySet())
            message.headers.put(headerName, (String)properties.getHeaders().get(headerName));

        return message;
    }
}
