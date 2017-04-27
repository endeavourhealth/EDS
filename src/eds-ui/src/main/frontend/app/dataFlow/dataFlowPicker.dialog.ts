import {Component, Input} from "@angular/core";
import {DataFlow} from "./models/DataFlow";
import {DataFlowService} from "./dataFlow.service";
import {NgbModal, NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {LoggerService} from "eds-common-js";

@Component({
    selector: 'ngbd-modal-content',
    template: require('./dataFlowPicker.html')
})
export class DataFlowPickerDialog {
    public static open(modalService: NgbModal, dataFlows : DataFlow[]) {
        const modalRef = modalService.open(DataFlowPickerDialog, { backdrop : "static"});
        modalRef.componentInstance.resultData = jQuery.extend(true, [], dataFlows);

        return modalRef;
    }

    @Input() resultData : DataFlow[];
    searchData : string;
    searchResults : DataFlow[];

    constructor(public activeModal: NgbActiveModal,
                private log:LoggerService,
                private dataFlowService : DataFlowService) {}

    private search() {
        var vm = this;
        if (vm.searchData.length < 3)
            return;
        vm.dataFlowService.search(vm.searchData)
            .subscribe(
                (result) => vm.searchResults = result,
                (error) => vm.log.error(error)
            );
    }

    private addToSelection(match : DataFlow) {
        if ($.grep(this.resultData, function(o:DataFlow) { return o.uuid === match.uuid; }).length === 0)
            this.resultData.push(match);
    }

    private removeFromSelection(match : DataFlow) {
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
