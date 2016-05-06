package org.endeavourhealth.core.messaging.model;

public class SftpMessage extends Message
{
    public static SftpMessage fromMessageData(String messageData) {
        SftpMessage message = new SftpMessage();
        message.body = messageData;

        return message;
    }
}
