import {Routing} from "./Routing";
import {Component, Input} from "@angular/core";
import {NgbModal, NgbActiveModal, NgbModalOptions} from "@ng-bootstrap/ng-bootstrap";
import {RoutingOverride} from "./models/RoutingOverride";
import {ServicePickerDialog} from "../services/servicePicker.dialog";
import {Service} from "../services/models/Service";
import {LoggerService} from "eds-common-js/dist/index";

@Component({
    selector : 'ngbd-modal-content',
    template : require('./routingOverrideDialog.html')
})
export class RoutingOverrideDialog {


    @Input() routingOverride : RoutingOverride;
    @Input() routingExchangeNames: string[];
    @Input() routingMap: {};
    @Input() serviceMapById: {};
    serviceDesc: string;

    constructor(private $modal: NgbModal,
                public activeModal: NgbActiveModal,
                private log: LoggerService) {

    }

    public static open(modalService : NgbModal,
                       routing : RoutingOverride,
                       routingExchangeNames: string[],
                       routingMap: {},
                       serviceMapById : {}) {

        const modalRef = modalService.open(RoutingOverrideDialog, { backdrop : "static"} as NgbModalOptions);

        modalRef.componentInstance.routingOverride = jQuery.extend(true, [], routing);
        modalRef.componentInstance.routingExchangeNames = routingExchangeNames;
        modalRef.componentInstance.routingMap = routingMap;
        modalRef.componentInstance.serviceMapById = serviceMapById;

        return modalRef;
    }

    ngOnInit() {
        var vm = this;
        vm.populateServiceDesc();
    }


    ok() {

        var vm = this;
        //validate
        if (!vm.routingOverride.serviceId) {
            vm.log.error('No service selected');
            return;
        }
        if (!vm.routingOverride.exchangeName) {
            vm.log.error('No exchange selected');
            return;
        }
        if (!vm.routingOverride.routingKey) {
            vm.log.error('No routing key selected');
            return;
        }

        this.activeModal.close(this.routingOverride);
        //console.log('OK Pressed');
    }

    cancel() {
        this.activeModal.dismiss('cancel');
        //console.log('Cancel Pressed');
    }

    getRoutingKeysForExchange(): string[] {
        var vm = this;

        var ret = [];

        var selectedExchange = vm.routingOverride.exchangeName;
        if (!selectedExchange) {
            return ret;
        }

        var routings = vm.routingMap[selectedExchange] as Routing[];
        if (!routings) {
            return ret;
        }

        for (var i=0; i<routings.length; i++) {
            var r = routings[i];
            ret.push(r.routeKey);
        }

        return ret;
    }



    selectPublisher() {
        var vm = this;

        //need to add the selected service to an array to pass in
        var list = [] as Service[];
        if (vm.routingOverride.serviceId) {
            var service = vm.serviceMapById[vm.routingOverride.serviceId] as Service;
            list.push(service);
        }

        ServicePickerDialog.open(vm.$modal, list)
            .result.then(function (result : Service[]) {

            if (result.length == 0) {
                vm.routingOverride.serviceId = null;

            } else if (result.length > 1) {
                vm.log.error('Multiple services selected');

            } else {
                var service = result[0];
                vm.routingOverride.serviceId = service.uuid;
                vm.populateServiceDesc();
            }
        }).catch((reason) => {
            //console.log('caught ' + reason);
        });
    }

    private populateServiceDesc() {
        var vm = this;

        if (!vm.routingOverride.serviceId
            || !vm.serviceMapById) { //if not retrieved this yet
            vm.serviceDesc = '';
            return;
        }

        var service = vm.serviceMapById[vm.routingOverride.serviceId] as Service;
        if (!service) {
            vm.serviceDesc = 'Missing service for ' + vm.routingOverride.serviceId;
            return;
        }

        vm.serviceDesc = service.localIdentifier + ', ' + service.name;
    }
}
