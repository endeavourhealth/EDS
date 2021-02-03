import {Component, Input} from "@angular/core";
import {LoggerService} from "eds-common-js";
import {NgbModal, NgbActiveModal, NgbModalOptions} from "@ng-bootstrap/ng-bootstrap";
import {Subscription} from "rxjs/Subscription";
import {OdsSearchDialog} from "../services/odsSearch.dialog";
import {ScheduledTasksService} from "./scheduledTasks.service";
import {ScheduledTaskAudit} from "./models/ScheduledTaskAudit";
import {DateTimeFormatter} from "../utility/DateTimeFormatter";

@Component({
    selector: 'ngbd-modal-content',
    template: require('./scheduledTaskHistoryDialog.html')
})
export class ScheduledTaskHistoryDialog  {

    //SD-338 - need to import the static formatting functions so they can be used by the HTML template
    formatYYYYMMDDHHMM = DateTimeFormatter.formatYYYYMMDDHHMM;

    @Input() audit: ScheduledTaskAudit;

    searchFromStr: string;
    searchToStr: string;
    searching: boolean;
    results: ScheduledTaskAudit[];

    constructor(private $modal: NgbModal,
                public activeModal: NgbActiveModal,
                private log: LoggerService,
                private scheduledTaskService: ScheduledTasksService) {

    }


    public static open(modalService: NgbModal, audit: ScheduledTaskAudit) {

        const modalRef = modalService.open(ScheduledTaskHistoryDialog, { backdrop : "static", size : "lg"} as NgbModalOptions);
        modalRef.componentInstance.audit = audit;

        //default search from last month
        var d = new Date();
        d.setDate(d.getDate() - 30);

        var s = d.getFullYear() + '-';
        if (d.getMonth()+1 < 10) {
            s += '0';
        }
        s += (d.getMonth()+1) + '-';
        if (d.getDate() < 10) {
            s += '0';
        }
        s += d.getDate();

        //console.log('setting to [' + s + ']');
        modalRef.componentInstance.searchFromStr = s;
        modalRef.componentInstance.searchToStr = '';

        return modalRef;
    }


    close() {
        this.activeModal.dismiss();
        //console.log('Cancel Pressed');
    }

    search() {
        var vm = this;

        var dFrom;
        if (vm.searchFromStr) {
            //console.log('From ' + vm.searchFromStr);
            dFrom = new Date(vm.searchFromStr);
        } else {
            //console.log('From dawn of time');
            dFrom = new Date('1900-01-01');
        }

        var dTo;
        if (vm.searchToStr) {
            dTo = new Date(vm.searchToStr);
        } else {
            dTo = new Date('9999-12-31');
        }


        vm.searching = true;

        var appName = vm.audit.applicationName;
        var taskName = vm.audit.taskName;
        vm.scheduledTaskService.getScheduledTaskHistory(appName, taskName, dFrom, dTo).subscribe(
            (result) => {
                vm.searching = false;
                vm.results = result;
            },
            (error) => {
                vm.searching = false;
                vm.log.error('Error searching');
            }
        );
    }

}
