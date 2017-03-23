import {UIEncounter} from "./models/resources/clinical/UIEncounter";
import {RecordViewerService} from "./recordViewer.service";
import {UIProblem} from "./models/resources/clinical/UIProblem";
import {linq} from "../common/linq";
import {UIPatientRecord} from "./models/UIPatientRecord";
import {UIDiary} from "./models/resources/clinical/UIDiary";
import {UIObservation} from "./models/resources/clinical/UIObservation";
import {Component, ViewChild, Input} from "@angular/core";
import {UIAllergy} from "./models/resources/clinical/UIAllergy";
import {UIImmunisation} from "./models/resources/clinical/UIImmunisation";
import {UIFamilyHistory} from "./models/resources/clinical/UIFamilyHistory";
import {UIMedicationStatement} from "./models/resources/clinical/UIMedicationStatement";
import {LoggerService} from "../common/logger.service";

@Component({
	selector : 'gpView',
	template : require('./gpView.html')
})
export class GPViewComponent {
	@ViewChild('recordTabs') recordTabs: any;
	private activeId : string = "summary";
	private _patient: UIPatientRecord;

	@Input()
	set patientRecord(patient: UIPatientRecord) {
		this._patient = patient;

		if (this._patient != null)
			this.loadDataForTab(this.activeId);
	}
	get patientRecord() : UIPatientRecord { return this._patient; }

	constructor(protected logger: LoggerService,
							protected recordViewerService: RecordViewerService) {
	}

	loadDataForTab(tabId: string) {
		this.activeId = tabId;
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
		if (this._patient.encounters != null)
			return;

		let ctrl = this;
		ctrl
			.recordViewerService
			.getEncounters(ctrl._patient.patient.patientId)
			.subscribe(
				(result: UIEncounter[]) => ctrl._patient.encounters = result
			);
	}

	public loadMedication(): void {
		if (this._patient.medication != null)
			return;

		let ctrl = this;
		this.recordViewerService.getMedication(ctrl._patient.patient.patientId)
			.subscribe(
				(result: UIMedicationStatement[]) => ctrl._patient.medication = linq(result)
					.OrderByDescending(t => t.dateAuthorised.date)
					.ToArray()
			);
	}

	public loadProblems(): void {
		if (this._patient.problems != null)
			return;

		let vm = this;
		vm
			.recordViewerService
			.getProblems(vm._patient.patient.patientId)
			.subscribe((result: UIProblem[]) =>
				vm._patient.problems = linq(result)
					.OrderByDescending(t => t.effectiveDate.date)
					.ToArray());
	}

	public loadObservations(): void {
		if (this._patient.observations != null)
			return;

		let vm = this;
		vm
			.recordViewerService
			.getObservations(vm._patient.patient.patientId)
			.subscribe((result: UIObservation[]) =>
				vm._patient.observations = linq(result)
					.OrderByDescending(t => t.effectiveDate.date)
					.ToArray());
	}

	public loadDiary(): void {
		if (this._patient.diary != null)
			return;

		let vm = this;
		vm
			.recordViewerService
			.getDiary(vm._patient.patient.patientId)
			.subscribe((result: UIDiary[]) =>
				vm._patient.diary = linq(result)
					.OrderByDescending(t => t.effectiveDate.date)
					.ToArray());
	}

	public loadAllergies(): void {
		if (this._patient.allergies != null)
			return;

		let vm = this;
		vm
			.recordViewerService
			.getAllergies(vm._patient.patient.patientId)
			.subscribe((result: UIAllergy[]) =>
				vm._patient.allergies = linq(result)
					.OrderByDescending(t => t.effectiveDate.date)
					.ToArray());
	}

	public loadImmunisations(): void {
		if (this._patient.immunisations != null)
			return;

		let vm = this;
		vm
			.recordViewerService
			.getImmunisations(vm._patient.patient.patientId)
			.subscribe((result: UIImmunisation[]) =>
				vm._patient.immunisations = linq(result)
					.OrderByDescending(t => t.effectiveDate.date)
					.ToArray());
	}

	public loadFamilyHistory(): void {
		if (this._patient.familyHistory != null)
			return;

		let vm = this;
		vm
			.recordViewerService
			.getFamilyHistory(vm._patient.patient.patientId)
			.subscribe((result: UIFamilyHistory[]) =>
				vm._patient.familyHistory = linq(result)
					.OrderByDescending(t => t.recordedDate.date)
					.ToArray());
	}
}
