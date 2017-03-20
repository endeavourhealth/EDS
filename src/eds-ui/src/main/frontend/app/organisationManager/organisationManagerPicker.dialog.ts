import {Component, Input} from "@angular/core";
import {Organisation} from "./models/Organisation";
import {OrganisationManagerService} from "./organisationManager.service";
import {LoggerService} from "../common/logger.service";
import {NgbModal, NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
    selector: 'ngbd-modal-content',
    template: require('./organisationManagerPicker.html')
})
export class OrganisationManagerPickerDialog {
    public static open(modalService: NgbModal, organisations : Organisation[], searchType : string) {
        const modalRef = modalService.open(OrganisationManagerPickerDialog, { backdrop : "static"});
        modalRef.componentInstance.resultData = jQuery.extend(true, [], organisations);
        modalRef.componentInstance.searchType = searchType;

        return modalRef;
    }

    @Input() resultData : Organisation[];
    searchData : string;
    searchResults : Organisation[];
    searchType : string;

    constructor(public activeModal: NgbActiveModal,
                private log:LoggerService,
                private organisationManagerService : OrganisationManagerService) {}

    private search() {
        var vm = this;
        if (vm.searchData.length < 3)
            return;
        vm.organisationManagerService.search(vm.searchData, vm.searchType)
            .subscribe(
                (result) => vm.searchResults = result,
                (error) => vm.log.error(error)
            );
    }

    private addToSelection(match : Organisation) {
        if ($.grep(this.resultData, function(o:Organisation) { return o.uuid === match.uuid; }).length === 0)
            this.resultData.push(match);
    }

    private removeFromSelection(match : Organisation) {
        var index = this.resultData.indexOf(match, 0);
        if (index > -1)
            this.resultData.splice(index, 1);
    }

    ok() {
        this.activeModal.close(this.resultData);
        console.log('OK Pressed');
    }

    cancel() {
        this.activeModal.dismiss('cancel');
        console.log('Cancel Pressed');
    }
}
