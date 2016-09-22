module app.models {
	'use strict';

	export class Patient {
		serviceId: string;
		systemId: string;
        patientId: string;
		nhsNumber: string;
        nhsNumberFormatted: string;
        title: string;
        forename: string;
        surname: string;
        displayName: string;
        dateOfBirthFormatted: string;
        genderFormatted: string;
        singleLineAddress: string;
	}
}