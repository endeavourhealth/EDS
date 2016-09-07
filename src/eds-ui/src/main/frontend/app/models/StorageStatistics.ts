module app.models {
	'use strict';

	export class StorageStatistics {
		serviceId: string;
		systemId: string;
		patientStatistics: PatientStatistics;
		resourceStatistics: ResourceStatistics[];
	}
}