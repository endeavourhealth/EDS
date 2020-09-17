import {linq, LoggerService} from "eds-common-js";
import {StateService} from "ui-router-ng2";
import {Component} from "@angular/core";
import {Subscription} from "rxjs/Subscription";
import {ServiceService} from "../services/service.service";
import {RabbitService} from "../queueing/rabbit.service";
import {Routing} from "../queueing/Routing";
import {ServiceListComponent} from "../services/serviceList.component";
import {RabbitNode} from "../queueing/models/RabbitNode";
import {Service} from "../services/models/Service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ScheduledTasksService} from "./scheduledTasks.service";
import {ScheduledTaskAudit} from "./models/ScheduledTaskAudit";

@Component({
    template : require('./scheduledTasks.html')
})
export class ScheduledTasksComponent {

    summary: ScheduledTaskAudit[];

    constructor(private $modal:NgbModal,
                private serviceService:ServiceService,
                private scheduledTasksService:ScheduledTasksService,
                private rabbitService:RabbitService,
                private logger:LoggerService,
                private $state:StateService) {


    }

    ngOnInit() {
        var vm = this;
        vm.refresh();
    }

    refresh() {

        var vm = this;
        vm.scheduledTasksService.getScheduledTaskSummary()
            .subscribe(
                data => {
                    vm.summary = data;
                },
                (error) => {
                    vm.logger.error('Failed to load summary', error);
                }
            );

    }


}