import {Component, Input} from "@angular/core";
import {LoggerService} from "eds-common-js";
import {NgbModal, NgbActiveModal, NgbModalOptions} from "@ng-bootstrap/ng-bootstrap";
import {Subscription} from "rxjs/Subscription";
import {DateTimeFormatter} from "../utility/DateTimeFormatter";
import {PublisherService} from "./models/PublisherService";
import {ServiceService} from "../services/service.service";
import {PublisherSystem} from "./models/PublisherSystem";

@Component({
    selector: 'ngbd-modal-content',
    template: require('./publisherStatusDialog.html')
})
export class PublisherStatusDialog {

    //SD-338 - need to import the static formatting functions so they can be used by the HTML template
    formatYYYYMMDDHHMM = DateTimeFormatter.formatYYYYMMDDHHMM;

    @Input() publisherService: PublisherService;
    /*
    searchFromStr: string;
    searchToStr: string;
    searching: boolean;
    results: SftpReaderChannelBatch[];
    //resultStr: string;*/

    constructor(private $modal: NgbModal,
                public activeModal: NgbActiveModal,
                private log: LoggerService,
                private serviceService : ServiceService) {

    }


    public static open(modalService: NgbModal, publisherService: PublisherService) {

        const modalRef = modalService.open(PublisherStatusDialog, { backdrop : "static", size : "lg"} as NgbModalOptions);
        modalRef.componentInstance.publisherService = publisherService;

        /*//default search from last 7 days
        var d = new Date();
        d.setDate(d.getDate() - 7);

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
        modalRef.componentInstance.searchToStr = '';*/

        return modalRef;
    }





    close() {
        this.activeModal.dismiss();
        //console.log('Cancel Pressed');
    }


    getTagNames(publisher: PublisherService): string[] {
        var vm = this;
        var ret = [];

        var tags = publisher.tags;
        if (!tags) {
            return ret;
        }

        var allTagNames = vm.serviceService.getTagNamesFromCache();
        for (var i=0; i<allTagNames.length; i++) {
            var tagName = allTagNames[i];
            if (tags.hasOwnProperty(tagName)) {
                ret.push(tagName);
            }
        }

        return ret;
    }

    getTagValueDesc(tagName: string, publisher: PublisherService): string {
        var tags = publisher.tags;
        if (!tags) {
            return '';
        }

        var ret = tags[tagName];
        if (!ret) {
            ret = '';
        }
        return ret;
    }


    /**
     * returns the panel "class" definition for the system, depending on it's status
     */
    getPanelClass(systemStatus: PublisherSystem): string {
        var vm = this;

        //if nothing has been received, then it's a weird case
        if (!systemStatus.lastReceivedExtractCutoff) {
            return 'panel panel-info';
        }

        //any transform error or if we've never processed
        if (systemStatus.processingInError
            || !systemStatus.lastProcessedInExtractCutoff
            || !systemStatus.lastProcessedOutExtractCutoff
            || systemStatus.writeOutError) {
            return 'panel panel-danger';
        }

        //if we're WAY behind then make it red
        if (systemStatus.inboundBehindDays > 1
            || systemStatus.outboundBehindDays > 1
            || systemStatus.outboundWriteBehindDays > 1) {
            return 'panel panel-danger';
        }

        //if we're less than a day behind, make it amber
        if (systemStatus.inboundBehindDays > 0
            || systemStatus.outboundBehindDays > 0
            || systemStatus.outboundWriteBehindDays > 0) {
            return 'panel panel-warning';
        }

        return 'panel panel-success';
    }

}
