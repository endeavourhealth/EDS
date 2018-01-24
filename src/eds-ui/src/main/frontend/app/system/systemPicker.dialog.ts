import {Component, Input} from "@angular/core";
import {System} from "./models/System";
import {LoggerService} from "eds-common-js";
import {NgbModal, NgbActiveModal, NgbModalOptions} from "@ng-bootstrap/ng-bootstrap";
import {Subscription} from "rxjs/Subscription";
import {Service} from "../services/models/Service";
import {SystemService} from "./system.service";

@Component({
    selector: 'ngbd-modal-content',
    template: require('./systemPickerDialog.html')
})
export class SystemPickerDialog  {

    @Input() service: Service;
    @Input() callback;
    systems: System[];

    constructor(public activeModal: NgbActiveModal,
                private log:LoggerService,
                private systemService : SystemService) {

    }


    public static open(modalService: NgbModal, service: Service, callback) {

        const modalRef = modalService.open(SystemPickerDialog, { backdrop : "static", size : "sm"} as NgbModalOptions);

        modalRef.componentInstance.service = service;
        modalRef.componentInstance.callback = callback;

        //start the loading of the lines
        modalRef.componentInstance.loadSystems();

        return modalRef;
    }

    private loadSystems() {

        var vm = this;
        vm.systemService.getSystems()
            .subscribe(
                (result) => {

                    vm.systems = [];

                    //filter systems according to service
                    for (var i=0; i<result.length; i++) {
                        var system = result[i];
                        var systemId = system.uuid;

                        for (var j=0; j<vm.service.endpoints.length; j++) {
                            var endpoint =  vm.service.endpoints[j];
                            var endpointSystemId = endpoint.systemUuid;

                            if (systemId == endpointSystemId) {
                                vm.systems.push(system);
                                break;
                            }
                        }
                    }
                },
                (error) => {
                    vm.log.error('Failed to load systems', error, 'Load systems');
                });
    }

    selectSystem(systemId: string) {
        var vm = this;

        //close the dialog
        vm.activeModal.dismiss();

        //invoke the callback
        vm.callback(vm.service, systemId);
    }

    close() {
        this.activeModal.dismiss();
        //console.log('Cancel Pressed');
    }
}
