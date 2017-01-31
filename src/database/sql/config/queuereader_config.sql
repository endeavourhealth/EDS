INSERT INTO config
(app_id, config_id, config_data)
VALUES
('queuereader', 'inbound', '<?xml version="1.0" encoding="UTF-8"?>
<QueueReaderConfiguration xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="../../../../eds-messaging-core/src/main/resources/QueueReaderConfiguration.xsd">
    <Queue>EdsInbound-GPs_Eng_Wls</Queue>
    <!--<Queue>EdsInbound-A-M</Queue>-->
    <Pipeline>
        <PostMessageToLog>
            <EventType>Transform_Start</EventType>
        </PostMessageToLog>
        <MessageTransformInbound>
            <SharedStoragePath>C:\SFTPData</SharedStoragePath>
            <FilingThreadLimit>10</FilingThreadLimit>
        </MessageTransformInbound>
        <PostMessageToLog>
            <EventType>Transform_End</EventType>
        </PostMessageToLog>
        <!--<PostMessageToExchange>
            <Exchange>EdsResponse</Exchange>
            <RoutingHeader>SenderLocalIdentifier</RoutingHeader>
        </PostMessageToExchange>-->
        <PostMessageToExchange>
            <Exchange>EdsProtocol</Exchange>
            <RoutingHeader>SenderLocalIdentifier</RoutingHeader>
            <MulticastHeader>BatchIds</MulticastHeader>
        </PostMessageToExchange>
    </Pipeline>
</QueueReaderConfiguration>');

INSERT INTO config
(app_id, config_id, config_data)
VALUES
('queuereader', 'protocol', '<?xml version="1.0" encoding="UTF-8"?>
<QueueReaderConfiguration xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="../../../../eds-messaging-core/src/main/resources/QueueReaderConfiguration.xsd">
    <Queue>EdsProtocol-A-M</Queue>
    <Pipeline>
        <RunDataDistributionProtocols/>
        <PostMessageToExchange>
            <Exchange>EdsTransform</Exchange>
            <RoutingHeader>SenderLocalIdentifier</RoutingHeader>
            <MulticastHeader>TransformBatch</MulticastHeader>
        </PostMessageToExchange>
    </Pipeline>
</QueueReaderConfiguration>');

INSERT INTO config
(app_id, config_id, config_data)
VALUES
('queuereader', 'response', '<?xml version="1.0" encoding="UTF-8"?>
<QueueReaderConfiguration xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="../../../../eds-messaging-core/src/main/resources/QueueReaderConfiguration.xsd">
    <Queue>EdsResponse-N-Z</Queue>
    <Pipeline>
        <PostToRest>
            <SendHeaders>Content-Type, MessageId</SendHeaders>
        </PostToRest>
    </Pipeline>
</QueueReaderConfiguration>');

INSERT INTO config
(app_id, config_id, config_data)
VALUES
('queuereader', 'subscriber', '<?xml version="1.0" encoding="UTF-8"?>
<QueueReaderConfiguration xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="../../../../eds-messaging-core/src/main/resources/QueueReaderConfiguration.xsd">
    <Queue>EdsSubscriber-A-M</Queue>
    <Pipeline>
        <PostMessageToLog>
            <EventType>Send</EventType>
        </PostMessageToLog>
        <PostToSubscriberWebService/>
        <PostToRest/>
    </Pipeline>
</QueueReaderConfiguration>');

INSERT INTO config
(app_id, config_id, config_data)
VALUES
('queuereader', 'transform', '<?xml version="1.0" encoding="UTF-8"?>
<QueueReaderConfiguration xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="../../../../eds-messaging-core/src/main/resources/QueueReaderConfiguration.xsd">
    <Queue>EdsTransform-A-M</Queue>
    <Pipeline>
        <PostMessageToLog>
            <EventType>Transform_Out_Start</EventType>
        </PostMessageToLog>
        <MessageTransformOutbound/>
        <PostMessageToLog>
            <EventType>Transform_Out_End</EventType>
        </PostMessageToLog>
        <PostMessageToExchange>
            <Exchange>EdsSubscriber</Exchange>
            <RoutingHeader>SenderLocalIdentifier</RoutingHeader>
            <MulticastHeader>SubscriberBatch</MulticastHeader>
        </PostMessageToExchange>
        <PostMessageToLog>
            <EventType>Posted_To_Subscriber_Queue</EventType>
        </PostMessageToLog>
    </Pipeline>
</QueueReaderConfiguration>');
