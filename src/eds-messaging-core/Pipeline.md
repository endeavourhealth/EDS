# Pipeline Component Configurations

This document provides detail on the individual pipeline components, their use, actions and configuration.

## General

All pipeline components allow the setting of exchange headers in their configuration.

```xml
<ExchangeHeaders>
    <Header Key="keyName">value</Header>
</ExchangeHeaders>
```

## OpenEnvelope

### Input
#### Headers
| Key          | Use      | Description        |
| ------------ | ---      | ------------------ |
| content-type | Optional | Inbound message format, "text/xml" or "text/json" (default) |
#### Body
A FHIR message bundle containing a MessageHeader resource and a Binary resource.

### Processing
Extracts the relevant information from the MessageHeader resource into the exchange headers and replaces the exchange body with the content of the Binary resource.

### Output
#### Headers
| Key          | Description            |
| ------------ | ---------------------- |
| MessageId    | Header.MessageId       |
| Sender       | Header.Source.System   |
| ResponseUri  | Header.Source.Endpoint |
| SourceSystem | Header.Source.Software |
#### Body
Base64 decoded content of the Binary resource.