import IModalService = angular.ui.bootstrap.IModalService;
import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;
import IModalSettings = angular.ui.bootstrap.IModalSettings;

import {BaseDialogController} from "../baseDialog.controller";
import {ILoggerService} from "../../blocks/logger.service";
import {UIPatient} from "../../recordViewer/models/resources/admin/UIPatient";
import {IRecordViewerService} from "../../recordViewer/recordViewer.service";
import {UIService} from "../../recordViewer/models/UIService";
import {linq} from "../../blocks/linq";

enum KeyCodes {
		ReturnKey = 13,
		Escape = 27,
		LeftArrow = 37,
		UpArrow = 38,
		RightArrow = 39,
		DownArrow = 40
}

export class PatientFindController extends BaseDialogController {

    services: UIService[];
    selectedService: UIService;
    searchTerms: string;
    searchedTerms: string;
    foundPatients: UIPatient[];
    selectedPatient: UIPatient;

	public static open($modal : IModalService) : IModalServiceInstance {
		var options : IModalSettings = {
			template: require('./patientFind.html'),
			controller: 'PatientFindController',
			controllerAs: 'ctrl',
			backdrop: 'static',
            size: 'lg'
		};

		var dialog = $modal.open(options);
		return dialog;
	}

	static $inject = ['$uibModalInstance', '$uibModal', 'RecordViewerService', 'LoggerService'];

	constructor(protected $uibModalInstance : IModalServiceInstance,
                private $modal : IModalService,
                protected recordViewerService: IRecordViewerService,
                private log : ILoggerService) {

        super($uibModalInstance);

        this.loadServices();
	}

    ok() {
        this.resultData = this.selectedPatient;
        super.ok();
    }

    private loadServices(): void {
        var ctrl = this;
        ctrl
            .recordViewerService
            .getServices()
            .then((result: UIService[]) => ctrl.services = linq(result).OrderBy(t => t.name).ToArray());
    }

    private selectService(service: UIService): void {

        if (service != this.selectedService)
            this.searchTermsChanged();

        this.selectedService = service;
    }

	findPatient() {
        this.searchedTerms = this.searchTerms;
        this.foundPatients = null;

        var ctrl = this;
        ctrl
            .recordViewerService
            .findPatient(ctrl.selectedService, ctrl.searchedTerms)
            .then((result: UIPatient[]) => ctrl.foundPatients = result);
    }

    selectPatient(patient: UIPatient) {
        if (this.selectedPatient == patient)
            this.selectedPatient = null;
        else
            this.selectedPatient = patient;
    }

    keydown($event: KeyboardEvent) {
        if ($event.keyCode == KeyCodes.UpArrow)
            this.selectPreviousPatient();
        else if ($event.keyCode == KeyCodes.DownArrow)
            this.selectNextPatient();
    }

    selectNextPatient() {
        if (this.foundPatients == null)
            return;

        let selectedPatientIndex: number = this.foundPatients.indexOf(this.selectedPatient);

        if (++selectedPatientIndex < this.foundPatients.length)
            this.selectedPatient = this.foundPatients[selectedPatientIndex];
    }

    selectPreviousPatient() {
        if (this.foundPatients == null)
            return;

        let selectedPatientIndex: number = this.foundPatients.indexOf(this.selectedPatient);

        if (--selectedPatientIndex >= 0)
            this.selectedPatient = this.foundPatients[selectedPatientIndex];
    }

    searchTermsChanged() {
        this.searchedTerms = null;
        this.foundPatients = null;
        this.selectedPatient = null;
    }
}
