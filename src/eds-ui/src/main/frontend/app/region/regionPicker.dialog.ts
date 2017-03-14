import {Component, Input} from "@angular/core";
import {Region} from "./models/Region";
import {RegionService} from "./region.service";
import {LoggerService} from "../common/logger.service";
import {NgbModal, NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
    selector: 'ngbd-modal-content',
    template: require('./RegionPicker.html')
})
export class RegionPickerDialog {
    public static open(modalService: NgbModal, regions : Region[]) {
        const modalRef = modalService.open(RegionPickerDialog, { backdrop : "static"});
        modalRef.componentInstance.resultData = jQuery.extend(true, [], regions);

        return modalRef;
    }

    @Input() resultData : Region[];
    searchData : string;
    searchResults : Region[];

    constructor(public activeModal: NgbActiveModal,
                private log:LoggerService,
                private regionService : RegionService) {}

    private search() {
        var vm = this;
        if (vm.searchData.length < 3)
            return;
        vm.regionService.search(vm.searchData)
            .subscribe(
                (result) => vm.searchResults = result,
                (error) => vm.log.error(error)
            );
    }

    private addToSelection(match : Region) {
        if ($.grep(this.resultData, function(o:Region) { return o.uuid === match.uuid; }).length === 0)
            this.resultData.push(match);
    }

    private removeFromSelection(match : Region) {
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
