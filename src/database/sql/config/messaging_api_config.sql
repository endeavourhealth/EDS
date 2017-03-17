INSERT INTO config
(app_id, config_id, config_data)
VALUES
('messaging-api', 'keycloak', '{
  "realm": "endeavour",
  "realm-public-key": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7GdjckqAZgjxp/o7717ei5RgkW3mtG3W+LfmlboBt20NQ/Jz6yb00Xoe9dBCLsqiiompePWuBNxGdwUNHzJcng7hpTvsi7Zp8PtTJDts/EinroKEv+Gac2VB1k8aLneDOtU6FYdi7uQ4vVU4xJ4D4s1ubG0VQXqUnSUvwwRN5UDdGYLrV2KueajgsNJ3mML4aJ2rLDyUF5uvKQV1UbZAwvCUo0tIeUYoN6PMkpaUrBagWeLhNhrNU9HsiDbMUjVttDRgMlgCtYvu4GapI+0cVecAUWfg0MdTCYuUJwUtTZoatf3d2bietsS+cYPFfs9eCIm1/7GLZWwv6qFDN1a4ewIDAQAB",
  "auth-server-url": "https://devauth.endeavourhealth.net/auth",
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