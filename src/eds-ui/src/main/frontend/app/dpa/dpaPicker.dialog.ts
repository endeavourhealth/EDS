import {Component, Input} from "@angular/core";
import {Dpa} from "./models/Dpa";
import {DpaService} from "./dpa.service";
import {LoggerService} from "../common/logger.service";
import {NgbModal, NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
    selector: 'ngbd-modal-content',
    template: require('./dpaPicker.html')
})
export class DpaPickerDialog {
    public static open(modalService: NgbModal, dpas : Dpa[]) {
        const modalRef = modalService.open(DpaPickerDialog, { backdrop : "static"});
        modalRef.componentInstance.resultData = jQuery.extend(true, [], dpas);

        return modalRef;
    }

    @Input() resultData : Dpa[];
    searchData : string;
    searchResults : Dpa[];

    constructor(public activeModal: NgbActiveModal,
                private log:LoggerService,
                private dpaService : DpaService) {}

    private search() {
        var vm = this;
        if (vm.searchData.length < 3)
            return;
        vm.dpaService.search(vm.searchData)
            .subscribe(
                (result) => vm.searchResults = result,
                (error) => vm.log.error(error)
            );
    }

    private addToSelection(match : Dpa) {
        if ($.grep(this.resultData, function(o:Dpa) { return o.uuid === match.uuid; }).length === 0)
            this.resultData.push(match);
    }

    private removeFromSelection(match : Dpa) {
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
