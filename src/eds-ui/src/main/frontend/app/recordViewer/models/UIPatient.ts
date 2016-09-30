module app.models {
	'use strict';

	export class UIPatient {
		serviceId: string;
		systemId: string;
        patientId: string;
		nhsNumber: string;
        name: UIHumanName;
        dateOfBirth: Date;
        genderFormatted: string;
        homeAddress: UIAddress;
	}
}