import {MedicalRecordController} from "./medicalRecord.controller";
import {MedicalRecordRoute} from "./medicalRecord.route";

angular.module('app.medicalRecord', ['ui.router'])
	.controller('MedicalRecordController', MedicalRecordController)
	.config(MedicalRecordRoute);