package org.endeavourhealth.core.messaging.exchange;

public abstract class HeaderKeys {

	// General
	public static final String ContentType = "content-type";

	// Message source
	public static final String MessageId = "MessageId";
	public static final String SenderLocalIdentifier = "SenderLocalIdentifier"; //the ODS code
	public static final String SourceSystem = "SourceSystem";
	public static final String SystemVersion = "SystemVersion";
	public static final String MessageEvent = "MessageEvent";
	public static final String ResponseUri = "ResponseUri";

	// Derrived from the SenderLocalIdentifier
	public static final String SenderServiceUuid = "SenderServiceUuid";
	public static final String SenderOrganisationUuid = "SenderOrganisationUuid";
	public static final String SenderSystemUuid = "SenderSystemUuid";

	public static final String MessageFormat = "MessageFormat";

	// Transform
	public static final String BatchIdsJson = "BatchIdsJson";
	public static final String DestinationAddress = "DestinationAddress";
	public static final String TransformBatch = "TransformBatch";
	public static final String Protocols = "Protocols";
	public static final String SubscriberBatch = "SubscriberBatch";
}
