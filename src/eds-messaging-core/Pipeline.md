# Pipeline Component Configurations

This document provides detail on the individual pipeline components, their use, actions and configuration.

## Content
[General](#general)

[OpenEnvelope](#openenvelope)

[PostMessageToLog](#postmessagetolog)

[PostMessageToExchange](#postmessagetoexchange)

[PostToRest](#posttorest)

<a id="general"></a>
## General

All pipeline components allow the setting of exchange headers in their configuration.

```xml
<ExchangeHeaders>
    <Header Key="keyName">value</Header>
</ExchangeHeaders>
```

<a id="openenvelope"></a>
## OpenEnvelope

##### Configuration Options
None

##### Input Headers
| Key          | Use      | Description        |
| ------------ | ---      | ------------------ |
| content-type | Optional | Inbound message format, "text/xml" or "text/json" (default) |
##### Input Body
A FHIR message bundle containing a MessageHeader resource and a Binary resource.

##### Processing
Extracts the relevant information from the MessageHeader resource into the exchange headers and replaces the exchange body with the content of the Binary resource.

##### Output Headers
| Key          | Description            |
| ------------ | ---------------------- |
| MessageId    | Header.MessageId       |
| Sender       | Header.Source.System   |
| ResponseUri  | Header.Source.Endpoint |
| SourceSystem | Header.Source.Software |
##### Output Body
Base64 decoded content of the Binary resource.

<a id="postmessagetolog"></a>
## PostMessageToLog

##### Configuration Options
| Tag          | Use | Description            |
| ------------ | --- | ---------------------- |
| EventType    | Mandatory | Audit event type (Receive/Validate/Send)       |

##### Input Headers
Headers to be audited
##### Input Body
Body to be audited

##### Processing
Inserts an audit entry (of the given type) into the database, containing the exchange headers and body.

##### Output Headers
Unchanged
##### Output Body
Unchanged

<a id="postmessagetoexchange"></a>
## PostMessageToExchange

##### Configuration Options
| Tag          | Use | Description            |
| ------------ | --- | ---------------------- |
| Credentials    | Mandatory | RabbitMQ logon credentials |
| Nodes    | Mandatory | Comma separated list of rabbit node addresses       |
| Exchange    | Mandatory | The name of the exchange to post to |
| RoutingHeader    | Mandatory | The key of the header to use for determining the routing key |
| MulticastHeader | Optional | The key of the header containing a comma separated list of values to multicast |

##### Input Headers
Headers to be posted
##### Input Body
Body to be posted

##### Processing
Uses the content of the exchange header specified by **RoutingHeader** to determine a routing key, then posts the exchange
headers and a generated messageId to the specified Rabbit exchange.
If a **MulticastHeader** key is provided, a post is made for each value in the given headers list (the header list is
replaced with relevant value for each post).

##### Output Headers
If a **MulticastHeader** is supplied then that headers value list will be replaced with the relevant individual value
for each post.
##### Output Body
Unchanged

<a id="posttorest"></a>
## PostToRest

##### Configuration Options
| Tag          | Use | Description            |
| ------------ | --- | ---------------------- |
| SendHeaders    | Optional | Comma separated list of header keys to send.  If omitted, all headers are sent |

##### Input Headers
| Tag          | Use | Description            |
| ------------ | --- | ---------------------- |
| DestinationAddress | Mandatory | Comma separated list of header keys to send.  If omitted, all headers are sent |
| content-type | Optional | Outbound message format, "text/xml" or "text/json" (default) |

##### Input Body
Body to be posted

##### Processing
Posts the exchange headers and body to the address specified in the **DestinationAddress** header.

##### Output Headers
Unchanged
##### Output Body
Unchanged
