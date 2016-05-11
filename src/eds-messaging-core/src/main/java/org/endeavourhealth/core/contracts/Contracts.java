package org.endeavourhealth.core.contracts;

import org.endeavourhealth.core.messaging.EDSMethod;

public final class Contracts {
	public static boolean SenderHasPermissionForMethod(String sender, EDSMethod method) {
		// Access/check in contracts config (DB)
		return true;
	}
}
