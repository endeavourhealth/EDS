import {Component, Input, ViewChild} from "@angular/core";
import {NgbModal, NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {LoggerService} from "eds-common-js";
import {DataSet} from "./models/Dataset";
import {DataSetService} from "./dataSet.service";

@Component({
    selector: 'ngbd-modal-content',
    template: require('./dataSetPicker.html')
})
export class DataSetPickerDialog {
    public static open(modalService: NgbModal, datasets : DataSet[]) {
        const modalRef = modalService.open(DataSetPickerDialog, { backdrop : "static"});
        modalRef.componentInstance.resultData = jQuery.extend(true, [], datasets);

        return modalRef;
    }

    @Input() resultData : DataSet[];
    searchData : string;
    searchResults : DataSet[];
    @ViewChild('search') searchInput;

    constructor(public activeModal: NgbActiveModal,
                private log:LoggerService,
                private dataSetService : DataSetService) {}

    ngAfterViewInit() {
        this.searchInput.nativeElement.focus();
    }

    private search() {
        var vm = this;
        if (vm.searchData.length < 3)
            return;
        vm.dataSetService.search(vm.searchData)
            .subscribe(
                (result) => vm.searchResults = result,
                (error) => vm.log.error(error)
            );
    }

    private addToSelection(match : DataSet) {
        if ($.grep(this.resultData, function(o:DataSet) { return o.uuid === match.uuid; }).length === 0)
            this.resultData.push(match);
    }

    private removeFromSelection(match : DataSet) {
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
