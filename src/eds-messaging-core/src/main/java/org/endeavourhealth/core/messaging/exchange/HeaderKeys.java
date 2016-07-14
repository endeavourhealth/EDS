package org.endeavourhealth.core.messaging.exchange;

public abstract class HeaderKeys {
	// General
	public static final String ContentType = "content-type";

	// Message general
	public static final String MessageId = "MessageId";
	public static final String MessageEvent = "MessageEvent";

	// Message source
	public static final String SenderLocalIdentifier = "SenderLocalIdentifier";
	public static final String SenderUuid = "SenderUuid";
	public static final String SourceSystem = "SourceSystem";
	public static final String SystemVersion = "SystemVersion";
	public static final String MessageFormat = "MessageFormat";
	public static final String ResponseUri = "ResponseUri";

	// Pipeline
	public static final String DestinationAddress = "DestinationAddress";
	public static final String ProtocolIds = "ProtocolIds";
	public static final String Subscribers = "Subscribers";
	public static final String TransformTo = "TransformTo";
	public static final String ProtocolData = "ProtocolData";
}
