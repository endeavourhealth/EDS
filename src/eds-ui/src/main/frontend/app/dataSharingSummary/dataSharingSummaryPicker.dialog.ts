import {Component, Input} from "@angular/core";
import {DataSharingSummary} from "./models/DataSharingSummary";
import {DataSharingSummaryService} from "./dataSharingSummary.service";
import {LoggerService} from "../common/logger.service";
import {NgbModal, NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
    selector: 'ngbd-modal-content',
    template: require('./dataSharingSummaryPicker.html')
})
export class DataSharingSummaryPickerDialog {
    public static open(modalService: NgbModal, dataSharingSummarys : DataSharingSummary[]) {
        const modalRef = modalService.open(DataSharingSummaryPickerDialog, { backdrop : "static"});
        modalRef.componentInstance.resultData = jQuery.extend(true, [], dataSharingSummarys);

        return modalRef;
    }

    @Input() resultData : DataSharingSummary[];
    searchData : string;
    searchResults : DataSharingSummary[];

    constructor(public activeModal: NgbActiveModal,
                private log:LoggerService,
                private dataSharingSummaryService : DataSharingSummaryService) {}

    private search() {
        var vm = this;
        if (vm.searchData.length < 3)
            return;
        vm.dataSharingSummaryService.search(vm.searchData)
            .subscribe(
                (result) => vm.searchResults = result,
                (error) => vm.log.error(error)
            );
    }

    private addToSelection(match : DataSharingSummary) {
        if ($.grep(this.resultData, function(o:DataSharingSummary) { return o.uuid === match.uuid; }).length === 0)
            this.resultData.push(match);
    }

    private removeFromSelection(match : DataSharingSummary) {
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
