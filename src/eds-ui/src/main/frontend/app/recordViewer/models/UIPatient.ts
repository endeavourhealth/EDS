module app.models {
	'use strict';

	export class UIPatient {
		serviceId: string;
		systemId: string;
        patientId: string;
		nhsNumber: string;
        nhsNumberFormatted: string;
        displayName: string;
        dateOfBirthFormatted: string;
        genderFormatted: string;
        singleLineAddress: string;
	}
}