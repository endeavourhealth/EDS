import {MedicalRecordComponent} from "./medicalRecord.component";
import {MedicalRecordRoute} from "./medicalRecord.route";
import {upgradeAdapter} from "../upgradeAdapter";

angular.module('app.medicalRecord', ['ui.router'])
	.config(MedicalRecordRoute)

	// Hybrid
	.directive('medicalRecordComponent', <angular.IDirectiveFactory>upgradeAdapter.downgradeNg2Component(MedicalRecordComponent));
