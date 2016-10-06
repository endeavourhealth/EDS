import {PatientIdentityController} from "./patientIdentity.controller";
import {PatientIdentityRoute} from "./patientIdentity.route";

angular.module('app.patientIdentity', [])
	.controller('PatientIdentityController', PatientIdentityController)
	.config(PatientIdentityRoute);