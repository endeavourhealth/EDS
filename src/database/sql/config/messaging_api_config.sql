INSERT INTO config
(app_id, config_id, config_data)
VALUES
('messaging-api', 'keycloak', '{
  "realm": "endeavour",
  "realm-public-key": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlnMTOPmUhfZVgd/qCV1svJe0AWFTm6hs3zaas2HNNHhR1rIGH02YBXeDazrbMHvJz1Q/weXn1j0tyfTNf53cwH4KQy3+OXjnh1vXOlR26XzmjdkvG+Hoy5L3+JbpIV5ktflFRxstml1CU3p8jZSXMyjLcCn1I1IbCWG/YsO1ST34ZOSI0K+11Y3N/fYZnsZW7OIPTc6zTpUIq0/jOySgSD1xwOS9q/MPJ6gq8B2LyDDYDR+pJPzlxQ3JPk2gbvqYYTSQcpwZwNZiyVhatiX4lxutDXzZd0FFMF2WVv9uFUcQkt1GkKxs9mN9u5EdbaWTb2noznBvBvdvbdePuSJdpQIDAQAB",
  "auth-server-url": "https://keycloak.eds.c.healthforge.io/auth",
  "ssl-required": "external",
  "resource": "eds-ui",
  "public-client": true
}' );

INSERT INTO config
(app_id, config_id, config_data)
VALUES
('messaging-api', 'api-configuration', '<?xml version="1.0" encoding="UTF-8"?>
<ApiConfiguration xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:noNamespaceSchemaLocation="../../../../eds-messaging-core/src/main/resources/ApiConfiguration.xsd">
    <GetData>
        <Pipeline>
            <ValidateMessageType/>
        </Pipeline>
    </GetData>
    <GetDataAsync>
        <Pipeline>
            <ValidateMessageType/>
        </Pipeline>
    </GetDataAsync>
    <PostMessage>
        <Pipeline>
            <ValidateMessageType/>
        </Pipeline>
    </PostMessage>
    <PostMessageAsync>
        <Pipeline>
            <OpenEnvelope/>
            <PostMessageToLog>
                <EventType>Receive</EventType>
            </PostMessageToLog>
            <DetermineRelevantProtocolIds/>
            <ValidateSender/>
            <ValidateMessageType/>
            <PostMessageToLog>
                <EventType>Validate</EventType>
            </PostMessageToLog>
            <PostMessageToExchange>
                <Exchange>EdsInbound</Exchange>
                <RoutingHeader>SenderLocalIdentifier</RoutingHeader>
            </PostMessageToExchange>
            <ReturnResponseAcknowledgement/>
        </Pipeline>
    </PostMessageAsync>
</ApiConfiguration>');