import {StateService} from "angular-ui-router";

import {ILoggerService} from "../blocks/logger.service";
import {IMedicalRecordService} from "../core/medicalRecord.service";
import {PatientService} from "../models/PatientService";

export class MedicalRecordController {
    static $inject = ['MedicalRecordService', 'LoggerService', '$state'];

    services : PatientService[];
    selectedService : string;

    constructor(private medicalRecordService:IMedicalRecordService,
                private logger:ILoggerService,
                private $state : StateService) {
        this.loadServiceList();
    }

    loadServiceList() {
        var vm = this;
        vm.services = null;
        vm.medicalRecordService.getServices()
          .then(function(data : PatientService[])
          {
             vm.services = data;
          }
        );
    }
}
