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
import {UIMedicationOrder} from "./models/resources/clinical/UIMedicationOrder";
import {UIAllergy} from "./models/resources/clinical/UIAllergy";
import {UIImmunization} from "./models/resources/clinical/UIImmunization";
import {UIFamilyHistory} from "./models/resources/clinical/UIFamilyHistory";

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
				case 'immunizations' :
					this.loadImmunizations();
					break;
				case 'familyHistory' :
					this.loadFamilyHistory();
					break;
				case 'summary' :
					this.loadProblems();
					this.loadAllergies();
					this.loadMedication();
					this.loadImmunizations();
					break;
			}
		}

		public loadConsultations(): void {
				if (this.patient.encounters != null)
						return;

				let ctrl = this;
				ctrl
						.recordViewerService
						.getEncounters(ctrl.patient.patient.patientId)
						.subscribe(
							(result: UIEncounter[]) => ctrl.patient.encounters = result
						);
		}

		public loadMedication(): void {
			if (this.patient.medicationOrders != null)
				return;

			let ctrl = this;
			this.recordViewerService.getMedication(ctrl.patient.patient.patientId)
				.subscribe(
					(result : UIMedicationOrder[]) => ctrl.patient.medicationOrders = linq(result)
						.OrderByDescending(t => t.dateAuthorized.date)
						.ToArray()
				);
		}

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

		public loadAllergies(): void {
			if (this.patient.allergies != null)
				return;

			let vm = this;
			vm
				.recordViewerService
				.getAllergies(vm.patient.patient.patientId)
				.subscribe((result: UIAllergy[]) =>
					vm.patient.allergies = linq(result)
						.OrderByDescending(t => t.effectiveDate.date)
						.ToArray());
		}

	public loadImmunizations(): void {
		if (this.patient.immunizations != null)
			return;

		let vm = this;
		vm
			.recordViewerService
			.getImmunizations(vm.patient.patient.patientId)
			.subscribe((result: UIImmunization[]) =>
				vm.patient.immunizations = linq(result)
					.OrderByDescending(t => t.effectiveDate.date)
					.ToArray());
	}

	public loadFamilyHistory(): void {
		if (this.patient.familyHistory != null)
			return;

		let vm = this;
		vm
			.recordViewerService
			.getFamilyHistory(vm.patient.patient.patientId)
			.subscribe((result: UIFamilyHistory[]) =>
				vm.patient.familyHistory = linq(result)
					.OrderByDescending(t => t.recordedDate.date)
					.ToArray());
	}
}
