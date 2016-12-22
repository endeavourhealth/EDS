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
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {UIAllergy} from "./models/resources/clinical/UIAllergy";
import {UIImmunisation} from "./models/resources/clinical/UIImmunisation";
import {UIFamilyHistory} from "./models/resources/clinical/UIFamilyHistory";
import {UIMedicationStatement} from "./models/resources/clinical/UIMedicationStatement";
import {Subscription} from "rxjs";

@Component({
		template : require('./recordViewer.html')
})
export class RecordViewerComponent {
		@ViewChild('recordTabs') recordTabs : any;
		public patient: UIPatientRecord;
		busy: Subscription;

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
				this.loadDataForTab('summary');
		}

		public clearPatient(): void {
				if (this.recordTabs)
						this.recordTabs.select('summary');
				this.patient = null;
		}

	loadDataForTab(tabId : string) {
			switch (tabId) {
				case 'consultations' :
					this.loadConsultations();
					break;
				case 'medication' :
					this.loadMedication();
					break;
				case 'problems' :
					this.loadProblems();
					break;
				case 'investigations' :
				case 'careHistory' :
					this.loadObservations();
					break;
				case 'diary' :
					this.loadDiary();
					break;
				case 'allergies' :
					this.loadAllergies();
					break;
				case 'immunisations' :
					this.loadImmunisations();
					break;
				case 'familyHistory' :
					this.loadFamilyHistory();
					break;
				case 'summary' :
					this.loadProblems();
					this.loadAllergies();
					this.loadMedication();
					this.loadImmunisations();
					break;
			}
		}

		public loadConsultations(): void {
				if (this.patient.encounters != null)
						return;

				let ctrl = this;
				this.busy = ctrl
						.recordViewerService
						.getEncounters(ctrl.patient.patient.patientId)
						.subscribe(
							(result: UIEncounter[]) => ctrl.patient.encounters = result
						);
		}

		public loadMedication(): void {
			if (this.patient.medication != null)
				return;

			let ctrl = this;
			this.busy = this.recordViewerService.getMedication(ctrl.patient.patient.patientId)
				.subscribe(
					(result : UIMedicationStatement[]) => ctrl.patient.medication = linq(result)
						.OrderByDescending(t => t.dateAuthorised.date)
						.ToArray()
				);
		}

		public loadProblems(): void {
				if (this.patient.problems != null)
						return;

				let vm = this;
			this.busy = vm
						.recordViewerService
						.getProblems(vm.patient.patient.patientId)
						.subscribe((result: UIProblem[]) =>
							vm.patient.problems = linq(result)
										.OrderByDescending(t => t.effectiveDate.date)
										.ToArray());
		}

		public loadObservations(): void {
				if (this.patient.observations != null)
						return;

				let vm = this;
			this.busy = vm
						.recordViewerService
						.getObservations(vm.patient.patient.patientId)
						.subscribe((result: UIObservation[]) =>
							vm.patient.observations = linq(result)
										.OrderByDescending(t => t.effectiveDate.date)
										.ToArray());
		}

		public loadDiary(): void {
				if (this.patient.diary != null)
						return;

				let vm = this;
			this.busy = vm
						.recordViewerService
						.getDiary(vm.patient.patient.patientId)
						.subscribe((result: UIDiary[]) =>
							vm.patient.diary = linq(result)
										.OrderByDescending(t => t.effectiveDate.date)
										.ToArray());
		}

		public loadAllergies(): void {
			if (this.patient.allergies != null)
				return;

			let vm = this;
			this.busy = vm
				.recordViewerService
				.getAllergies(vm.patient.patient.patientId)
				.subscribe((result: UIAllergy[]) =>
					vm.patient.allergies = linq(result)
						.OrderByDescending(t => t.effectiveDate.date)
						.ToArray());
		}

	public loadImmunisations(): void {
		if (this.patient.immunisations != null)
			return;

		let vm = this;
		this.busy = vm
			.recordViewerService
			.getImmunisations(vm.patient.patient.patientId)
			.subscribe((result: UIImmunisation[]) =>
				vm.patient.immunisations = linq(result)
					.OrderByDescending(t => t.effectiveDate.date)
					.ToArray());
	}

	public loadFamilyHistory(): void {
		if (this.patient.familyHistory != null)
			return;

		let vm = this;
		this.busy = vm
			.recordViewerService
			.getFamilyHistory(vm.patient.patient.patientId)
			.subscribe((result: UIFamilyHistory[]) =>
				vm.patient.familyHistory = linq(result)
					.OrderByDescending(t => t.recordedDate.date)
					.ToArray());
	}
}
