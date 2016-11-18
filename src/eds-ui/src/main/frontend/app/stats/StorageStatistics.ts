import {PatientStatistics} from "./PatientStatistics";
import {ResourceStatistics} from "./ResourceStatistics";

export class StorageStatistics {
	serviceId: string;
	systemId: string;
	patientStatistics: PatientStatistics;
	resourceStatistics: ResourceStatistics[];
}
