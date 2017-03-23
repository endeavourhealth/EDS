import {UIPatient} from "./models/resources/admin/UIPatient";
import {PatientFindDialog} from "./patientFind.dialog";
import {RecordViewerService} from "./recordViewer.service";
import {UIPatientRecord} from "./models/UIPatientRecord";
import {Component} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {LoggerService} from "../common/logger.service";
import {SecurityService} from "../security/security.service";
import {UIPerson} from "./models/resources/admin/UIPerson";
import {UIEpisodeOfCare} from "./models/resources/clinical/UIEpisodeOfCare";

@Component({
		template : require('./recordViewer.html')
})
export class RecordViewerComponent {
		public person : UIPerson;
		public episode : UIEpisodeOfCare;
		public patientRecord: UIPatientRecord;

		constructor(private $modal: NgbModal,
								protected logger : LoggerService,
								protected recordViewerService: RecordViewerService,
								protected securityService : SecurityService) {
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// patient find
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		public showPersonFind(): void {
				let ctrl = this;
				if (ctrl.securityService.currentUser.organisation) {
					PatientFindDialog
						.open(ctrl.$modal)
						.result
						.then((result: UIPatient) => { if (result) ctrl.setPerson(result) });
					} else {
					ctrl.logger.warning('Select a service', null, 'No service selected');
				}
		}

		public setPerson(person : UIPerson) {
			this.person = person;
		}

		public clearPerson() {
			this.clearEpisode();
			this.person = null;

		}

		public clearEpisode() {
			this.clearPatientRecord();
			this.episode = null;
		}

		onEpisodeSelect(episode : UIEpisodeOfCare) {
			let vm = this;
			vm.episode = episode;
			vm.recordViewerService.getPatient(episode.patient.patientId).subscribe(
				(result) => this.setPatientRecord(result),
				(error) => this.logger.error("Failed to load episode", error, "Error")
			);
		}

		public setPatientRecord(patient: UIPatient): void {
				this.clearPatientRecord();
				this.patientRecord = new UIPatientRecord(patient);
		}

		public clearPatientRecord(): void {
			this.patientRecord = null;
		}
}
