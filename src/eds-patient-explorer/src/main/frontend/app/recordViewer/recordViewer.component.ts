import {UIPatient} from "./models/resources/admin/UIPatient";
import {UIEncounter} from "./models/resources/clinical/UIEncounter";
import {PatientFindDialog} from "./patientFind.dialog";
import {RecordViewerService} from "./recordViewer.service";
import {UIProblem} from "./models/resources/clinical/UIProblem";
import {linq} from "../common/linq";
import {UIPatientRecord} from "./models/UIPatientRecord";
import {UIDiary} from "./models/resources/clinical/UIDiary";
import {UIObservation} from "./models/resources/clinical/UIObservation";
import {Component, ViewChild} from "@angular/core";
import {NgbModal, NgbTabChangeEvent} from "@ng-bootstrap/ng-bootstrap";

@Component({
		template : require('./recordViewer.html')
})
export class RecordViewerComponent {
		@ViewChild('recordTabs') recordTabs : any;
		public patient: UIPatientRecord;

		constructor(private $modal: NgbModal, protected recordViewerService: RecordViewerService) {
				this.showPatientFind();
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// patient find
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		public showPatientFind(): void {
				let ctrl = this;
				PatientFindDialog
						.open(ctrl.$modal)
						.result
						.then((result: UIPatient) => ctrl.setPatient(result));
		}

		public setPatient(patient: UIPatient): void {
				this.clearPatient();
				this.patient = new UIPatientRecord(patient);
		}

		public clearPatient(): void {
				if (this.recordTabs)
						this.recordTabs.select('summary');
				this.patient = null;
		}

		beforeTabChange($event: NgbTabChangeEvent) {
				switch ($event.nextId) {
						case 'consultations' :
								this.loadConsultations();
								break;
						case 'problems' :
								this.loadProblems();
								break;
						case 'investigations' :
								this.loadObservations();
								break;
						case 'diary' :
								this.loadDiary();
								break;
				}

		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// consultations
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		public loadConsultations(): void {
				if (this.patient.encounters != null)
						return;

				let ctrl = this;
				ctrl
						.recordViewerService
						.getEncounters(ctrl.patient.patient.patientId)
						.subscribe((result: UIEncounter[]) => ctrl.patient.encounters = result);
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// problems
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		public loadProblems(): void {
				if (this.patient.problems != null)
						return;

				let vm = this;
				vm
						.recordViewerService
						.getProblems(vm.patient.patient.patientId)
						.subscribe((result: UIProblem[]) =>
							vm.patient.problems = linq(result)
										.OrderByDescending(t => t.effectiveDate.date)
										.ToArray());
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// observations
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		public loadObservations(): void {
				if (this.patient.observations != null)
						return;

				let vm = this;
				vm
						.recordViewerService
						.getObservations(vm.patient.patient.patientId)
						.subscribe((result: UIObservation[]) =>
							vm.patient.observations = linq(result)
										.OrderByDescending(t => t.effectiveDate.date)
										.ToArray());
		}


		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// diary
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		public loadDiary(): void {
				if (this.patient.diary != null)
						return;

				let vm = this;
				vm
						.recordViewerService
						.getDiary(vm.patient.patient.patientId)
						.subscribe((result: UIDiary[]) =>
							vm.patient.diary = linq(result)
										.OrderByDescending(t => t.effectiveDate.date)
										.ToArray());
		}
}
