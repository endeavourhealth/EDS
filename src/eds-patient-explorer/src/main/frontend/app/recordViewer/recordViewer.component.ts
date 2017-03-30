import {UIPatient} from "./models/resources/admin/UIPatient";
import {PatientFindDialog} from "./patientFind.dialog";
import {RecordViewerService} from "./recordViewer.service";
import {UIPersonRecord} from "./models/UIPersonRecord";
import {Component} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {LoggerService} from "../common/logger.service";
import {SecurityService} from "../security/security.service";
import {UIPerson} from "./models/resources/admin/UIPerson";
import {UIEpisodeOfCare} from "./models/resources/clinical/UIEpisodeOfCare";
import {Observable} from "rxjs";
import {linq} from "../common/linq";

@Component({
		template : require('./recordViewer.html')
})
export class RecordViewerComponent {
		public person : UIPerson;
		public episodes : UIEpisodeOfCare[];
		public personRecord: UIPersonRecord;

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
			this.clearPerson();
			this.person = person;
		}

		public clearPerson() {
			this.clearEpisode();
			this.person = null;
		}

		public clearEpisode() {
			this.clearPatientRecord();
			this.episodes = null;
		}

		onEpisodeSelect(episodes : UIEpisodeOfCare[]) {
			let vm = this;
			vm.episodes = episodes;

			let o : Observable<UIPatient>[] = linq(vm.episodes)
				.Select(e => e.patient.patientId)
				.Distinct()
				.Select(p => vm.recordViewerService.getPatient(p))
				.ToArray();

			Observable.forkJoin(o)
				.subscribe(
					(result) => this.setPatientRecord(result),
					(error) => this.logger.error("Failed to load episode", error, "Error")
				);
		}

		public setPatientRecord(patients: UIPatient[]): void {
				this.clearPatientRecord();
				this.personRecord = new UIPersonRecord(patients);
		}

		public clearPatientRecord(): void {
			this.personRecord = null;
		}
}
