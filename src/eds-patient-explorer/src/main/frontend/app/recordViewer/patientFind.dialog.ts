import {UIPatient} from "./models/resources/admin/UIPatient";
import {RecordViewerService} from "./recordViewer.service";
import {UIService} from "./models/UIService";
import {linq} from "../common/linq";
import {Component} from "@angular/core";
import {NgbModal, NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {SecurityService} from "../security/security.service";

// enum KeyCodes {
// 		ReturnKey = 13,
// 		Escape = 27,
// 		LeftArrow = 37,
// 		UpArrow = 38,
// 		RightArrow = 39,
// 		DownArrow = 40
// }

@Component({
    selector: 'ngbd-modal-content',
    template: require('./patientFind.html')
})
export class PatientFindDialog {

    selectedService: UIService = null;
    loading : boolean = false;
    searchTerms: string;
    searchedTerms: string;
    foundPatients: UIPatient[];
    selectedPatient: UIPatient;

    public static open(modalService: NgbModal) {
        const modalRef = modalService.open(PatientFindDialog, {backdrop: "static", size: 'lg'});
        return modalRef;
    }

    constructor(protected activeModal: NgbActiveModal,
                protected recordViewerService: RecordViewerService,
                protected securityService: SecurityService) {

        this.loadServices();
    }

    ok() {
        this.activeModal.close(this.selectedPatient);
        console.log('OK Pressed');
    }

    cancel() {
        this.activeModal.close(null);
        console.log('Cancel Pressed');
    }

    private loadServices(): void {
        var vm = this;
        vm.recordViewerService.getServices()
          .subscribe(
            (result) => {
                vm.selectedService = linq(result)
                  .Where(t => t.serviceId === vm.securityService.currentUser.organisation)
                  .First();
            });
    }

    findPatient() {
        this.searchedTerms = this.searchTerms;
        this.foundPatients = null;
        this.loading = true;

        var vm = this;
        vm
          .recordViewerService
          .findPerson(vm.searchedTerms)
          .subscribe((result: UIPatient[]) => {
							vm.foundPatients = linq(result).OrderBy(t => t.name.familyName).ToArray()
							vm.loading = false;
						}
					);
    }

    selectPatient(patient: UIPatient, close: boolean) {
        if (close) {
            this.selectedPatient = patient;
            this.ok();
        }
        else {
            if (this.selectedPatient == patient)
                this.selectedPatient = null;
            else
                this.selectedPatient = patient;
        }
    }

    // keydown($event: KeyboardEvent) {
    //     if ($event.keyCode == KeyCodes.UpArrow)
    //         this.selectPreviousPatient();
    //     else if ($event.keyCode == KeyCodes.DownArrow)
    //         this.selectNextPatient();
    // }

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
