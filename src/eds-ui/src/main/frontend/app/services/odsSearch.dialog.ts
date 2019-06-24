import {Component, Input} from "@angular/core";
import {LoggerService} from "eds-common-js";
import {NgbModal, NgbActiveModal, NgbModalOptions} from "@ng-bootstrap/ng-bootstrap";
import {Subscription} from "rxjs/Subscription";
import {ServiceService} from "./service.service";

@Component({
    selector: 'ngbd-modal-content',
    template: require('./odsSearchDialog.html')
})
export class OdsSearchDialog  {

    odsCode: string;
    resultStr: string;
    searching: boolean;

    constructor(public activeModal: NgbActiveModal,
                private log: LoggerService,
                private serviceService : ServiceService) {

    }


    public static open(modalService: NgbModal) {

        //const modalRef = modalService.open(OdsSearchDialog, { backdrop : "static", size : "md"} as NgbModalOptions);
        const modalRef = modalService.open(OdsSearchDialog, { backdrop : "static"} as NgbModalOptions);

        return modalRef;
    }


    close() {
        this.activeModal.dismiss();
        //console.log('Cancel Pressed');
    }

    search() {
        var vm = this;
        if (!vm.odsCode) {
            vm.log.error('Enter an ODS code');
            return;
        }

        vm.searching = true;
        vm.resultStr = 'searching...';

        vm.serviceService.getOpenOdsRecord(vm.odsCode).subscribe(
            (result) => {
                vm.searching = false;

                if (result) {
                    vm.resultStr = JSON.stringify(result, null, 2);
                } else {
                    vm.resultStr = 'no match found';
                }
            },
            (error) => {
                vm.searching = false;
                vm.log.error('Error searching');
            }
        );
    }
}
