import {MedicalRecordController,MedicalRecordComponent} from "./medicalRecord.component";
import {MedicalRecordRoute} from "./medicalRecord.route";
import {MedicalRecordService} from "./medicalRecord.service";

angular.module('app.medicalRecord', ['ui.router'])
	.controller('MedicalRecordController', MedicalRecordController)
	.service('MedicalRecordService', MedicalRecordService)
	.component('medicalRecordComponent', new MedicalRecordComponent())
	.config(MedicalRecordRoute);