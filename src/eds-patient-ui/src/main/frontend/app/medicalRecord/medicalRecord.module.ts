import {MedicalRecordComponent} from "./medicalRecord.component";
import {MedicalRecordRoute} from "./medicalRecord.route";
import {MedicalRecordService} from "./medicalRecord.service";
import {upgradeAdapter} from "../upgradeAdapter";

angular.module('app.medicalRecord', ['ui.router'])
	.config(MedicalRecordRoute)

	// Hybrid
	.directive('medicalRecordComponent', <angular.IDirectiveFactory>upgradeAdapter.downgradeNg2Component(MedicalRecordComponent));
upgradeAdapter.upgradeNg1Provider('MedicalRecordService');