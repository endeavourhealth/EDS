import {Component, Input} from "@angular/core";
import {Dsa} from "./models/Dsa";
import {DsaService} from "./dsa.service";
import {LoggerService} from "eds-common-js";
import {NgbModal, NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
    selector: 'ngbd-modal-content',
    template: require('./dsaPicker.html')
})
export class DsaPickerDialog {
    public static open(modalService: NgbModal, dsas : Dsa[]) {
        const modalRef = modalService.open(DsaPickerDialog, { backdrop : "static"});
        modalRef.componentInstance.resultData = jQuery.extend(true, [], dsas);

        return modalRef;
    }

    @Input() resultData : Dsa[];
    searchData : string;
    searchResults : Dsa[];

    constructor(public activeModal: NgbActiveModal,
                private log:LoggerService,
                private dsaService : DsaService) {}

    private search() {
        var vm = this;
        if (vm.searchData.length < 3)
            return;
        vm.dsaService.search(vm.searchData)
            .subscribe(
                (result) => vm.searchResults = result,
                (error) => vm.log.error(error)
            );
    }

    private addToSelection(match : Dsa) {
        if ($.grep(this.resultData, function(o:Dsa) { return o.uuid === match.uuid; }).length === 0)
            this.resultData.push(match);
    }

    private removeFromSelection(match : Dsa) {
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
