module app.models {
	'use strict';

	export class Demographics {
		serviceId: string;
		systemId: string;
        patientId: string;
		nhsNumber: string;
        displayName: string;
        dateOfBirthString: Date;
        genderString: string;
	}
}