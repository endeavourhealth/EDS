import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {linq, LoggerService} from "eds-common-js";
import {StateService} from "ui-router-ng2";
import {Component} from "@angular/core";
import {Subscription} from "rxjs/Subscription";
import {ServiceService} from "../services/service.service";
import {SftpReaderService} from "./sftpReader.service";
import {SftpReaderChannelStatus} from "./models/SftpReaderChannelStatus";
import {SftpReaderBatchContents} from "./models/SftpReaderBatchContents";
import {OdsSearchDialog} from "../services/odsSearch.dialog";
import {SftpReaderHistoryDialog} from "./sftpReaderHistory.dialog";
import {SftpReaderConfiguration} from "./models/SftpReaderConfiguration";
import {SftpReaderOrgsDialog} from "./sftpReaderOrgs.dialog";
import {ServiceListComponent} from "../services/serviceList.component";
import {QueueReaderStatusService} from "../queueReaderStatus/queueReaderStatus.service";
import {QueueReaderStatus} from "../queueReaderStatus/queueReaderStatus";
import {DateTimeFormatter} from "../utility/DateTimeFormatter";

@Component({
    template : require('./sftpReader.html')
})
export class SftpReaderComponent {

    //SD-338 - need to import the static formatting functions so they can be used by the HTML template
    formatYYYYMMDDHHMMSS = DateTimeFormatter.formatYYYYMMDDHHMMSS;
    formatYYYYMMDDHHMM = DateTimeFormatter.formatYYYYMMDDHHMM;
    formatHHMMSS = DateTimeFormatter.formatHHMMSS;

    //SFTP configuration status
    configurations: SftpReaderConfiguration[];
    refreshingStatusMap: {};
    statusMap: {};
    statusesLastRefreshed: Date;

    //application status
    refreshingApplicationStatus: boolean;
    applicationStatusLastRefreshed: Date;
    applicationStatus: QueueReaderStatus[];


    constructor(private $modal: NgbModal,
                protected sftpReaderService: SftpReaderService,
                private queueReaderStatusService: QueueReaderStatusService,
                protected logger: LoggerService,
                protected $state: StateService) {


    }

    ngOnInit() {
        var vm = this;

        vm.refreshScreen();
    }

    refreshScreen() {
        var vm = this;
        vm.refreshInstances(true);
        vm.refreshApplicationStatus();
    }

    refreshApplicationStatus() {
        var vm = this;
        vm.refreshingApplicationStatus = true;
        vm.applicationStatusLastRefreshed = new Date();

        vm.queueReaderStatusService.getStatus('sftpreader').subscribe(
            (result) => {

                vm.processApplicationResults(result);
                vm.refreshingApplicationStatus = false;

            },
            (error) => {
                vm.logger.error('Failed get application status', error);
                vm.refreshingApplicationStatus = false;
            }
        );
    }

    processApplicationResults(results: QueueReaderStatus[]) {
        var vm = this;
        vm.applicationStatus = linq(results)
            .OrderBy(s => s.applicationInstanceName)
            .ThenBy(s => s.applicationInstanceNumber)
            .ToArray();
        //console.log('app status = ' + vm.applicationStatus);
    }

    refreshInstances(fullRefresh: boolean) {
        var vm = this;
        if (fullRefresh) {
            vm.configurations = null;
            vm.refreshingStatusMap = {};
            vm.statusMap = {};
        }

        vm.sftpReaderService.getSftpReaderInstances().subscribe(
            (result) => {
                vm.configurations = linq(result).OrderBy(s => s.configurationId).ToArray();
                if (fullRefresh) {
                    vm.refreshStatuses();
                }
            },
            (error) => {
                vm.logger.error('Failed get SFTP Reader instances', error, 'SFTP Reader');
            }
        )
    }



    refreshStatuses() {
        var vm = this;
        vm.statusesLastRefreshed = new Date();

        for (var i = 0; i < vm.configurations.length; i++) {
            var configuration = vm.configurations[i];
            vm.refreshStatus(configuration);
        }
    }

    refreshStatus(configuration: SftpReaderConfiguration) {
        var vm = this;

        var configurationId = configuration.configurationId;

        vm.refreshingStatusMap[configurationId] = true;

        vm.sftpReaderService.getSftpReaderStatus(configurationId).subscribe(
            (result) => {

                vm.calculateErrors(result); //must do this before the below fn
                vm.calculateIfWarning(result);
                vm.statusMap[configurationId] = result;
                vm.refreshingStatusMap[configurationId] = false;
            },
            (error) => {

                vm.refreshingStatusMap[configurationId] = false;
                vm.logger.error('Failed to get status for ' + configurationId, error);
            }
        )
    }

    /**
     * filters the batches to work out which are OK and which are in error
     */
    calculateErrors(status: SftpReaderChannelStatus) {
        var vm = this;

        var ok = [];
        var dpaError = [];
        var error = [];

        //if not set, just set to an empty array
        if (!status.completeBatchContents) {
            status.completeBatchContents = [];
        }

        for (var i=0; i<status.completeBatchContents.length; i++) {
            var content = status.completeBatchContents[i];
            if (content.notified) {
                ok.push(content);
            } else {
                var errorMsg = content.error;
                if (errorMsg
                    && (errorMsg.indexOf('No DPA found') > -1
                        || errorMsg.indexOf('no DPA exists') > -1)) {
                    dpaError.push(content);
                } else {
                    error.push(content);
                }
            }
        }

        status.okBatches = ok;
        status.dpaErrorBatches = dpaError;
        status.errorBatches = error;
    }

    calculateIfWarning(status: SftpReaderChannelStatus) {
        var vm = this;

        //any of these count as a warning
        if (vm.isLastPollAttemptTooOld(status)
            || !status.latestPollingStart
            || status.latestPollingException
            || vm.isLastExtractTooOld(status)
            || !status.latestBatchId
            || status.errorBatches.length > 0
            || status.dpaErrorBatches.length > 0) {

            status.warning = true;
        }
    }

    isRefreshing(configuration: SftpReaderConfiguration): boolean {
        var vm = this;

        var configurationId = configuration.configurationId;
        return vm.refreshingStatusMap[configurationId];
    }

    /**
     * returns the status for a configuration. Note this uses an array so that we can use the angular
     * for loop, and avoid having to call this function dozens of times.
     */
    getStatusToDisplay(configuration: SftpReaderConfiguration): SftpReaderChannelStatus[] {
        var vm = this;

        var ret = [];

        var configurationId = configuration.configurationId;
        var status = vm.statusMap[configurationId];
        if (status) {
            ret.push(status);
        }

        return ret;
    }

    getConfigurationsToDisplay(): SftpReaderConfiguration[] {
        var vm = this;
        if (!vm.configurations) { //if not retrieved yet
            return vm.configurations;
        }

        var ret = [];
        var i;
        for (i=0; i<vm.configurations.length; i++) {
            var configuration = vm.configurations[i];
            //console.log('checking configuration at ' + i);
            //console.log(configuration);

            if (vm.sftpReaderService.filterInstanceName) {
                var configInstanceName = configuration.instanceName;
                if (vm.sftpReaderService.filterInstanceName != configInstanceName) {
                    continue;
                }
            }

            if (vm.sftpReaderService.showWarningsOnly) {

                //always include configurations until we've got data back for them
                if (!vm.isRefreshing(configuration)) {

                    var configurationId = configuration.configurationId;
                    var status = vm.statusMap[configurationId];
                    if (!status.warning) { //if the warning boolean is false, skip it
                        continue;
                    }
                }
            }

            ret.push(configuration);
        }
        return ret;
    }

    /*filterOrgs(arr: SftpReaderBatchContents[], wantOk: boolean): SftpReaderBatchContents[] {
        var ret = [];

        var i;
        for (i=0; i<arr.length; i++) {
            var c = arr[i];
            if (c.notified == wantOk) {
                ret.push(c);
            }
        }
        return ret;
    }*/

    /*getPanelClass(status: SftpReaderChannelStatus): string {
        if (status.instanceName) {
            return "panel panel-primary";
        } else {
            return "panel panel-info";
        }
    }*/

    odsSearch() {
        var vm = this;
        OdsSearchDialog.open(vm.$modal);

    }

    isLastPollAttemptTooOld(status: SftpReaderChannelStatus): boolean {
        var vm = this;

        var lastPolled = status.latestPollingStart;
        if (!lastPolled) {
            return true;
        }
        var pollingFrequencySec = status.pollFrequencySeconds;
        var pollingFrequencyMs = pollingFrequencySec * 1000;
        pollingFrequencyMs = (pollingFrequencyMs * 2); //double the polling frequency to give a fair window

        //var now = new Date();
        var now = vm.statusesLastRefreshed; //use date of refresh rather than current date so it doesn't change if you don't refresh the page
        var lastPolledDiffMs = now.getTime() - lastPolled;

        return lastPolledDiffMs > pollingFrequencyMs;
    }

    isLastExtractTooOld(status: SftpReaderChannelStatus): boolean {
        var vm = this;

        var lastExtract = status.latestBatchReceived;
        if (!lastExtract) {
            return true;
        }

        var dataFrequencyDays = status.dataFrequencyDays;
        var dataFrequencyMs = dataFrequencyDays * 24 * 60 * 60 * 1000;
        dataFrequencyMs = (dataFrequencyMs * 1.5); //add 10% to the poling frequency so it's less touchy

        //var now = new Date();
        var now = vm.statusesLastRefreshed; //use date of refresh rather than current date so it doesn't change if you don't refresh the page
        var lastExtractDiffMs = now.getTime() - lastExtract;

        return lastExtractDiffMs > dataFrequencyMs;
    }

    viewHistory(configuration: SftpReaderConfiguration) {
        var vm = this;
        SftpReaderHistoryDialog.open(vm.$modal, configuration);
    }


    /*ignoreBatchSplit(content: SftpReaderBatchContents, status: SftpReaderChannelStatus) {
        var vm = this;

        var reason = prompt('Enter reason');
        if (reason == null) {
            return;
        }

        vm.sftpReaderService.ignoreBatchSplit(status.latestBatchId, content.batchSplitId, status.id, reason).subscribe(
            (result) => {
                content.error = null;
                content.result = reason;
                content.notified = true;
            },
            (error) => {
                vm.logger.error('Failed to ignore batch split', error, 'SFTP Reader Error');
            }
        )
    }*/

    /*selectAllBatchSpitErrors(status: SftpReaderChannelStatus) {
        var vm = this;

        for (var i=0; i<status.completeBatchContents.length; i++) {
            var batchSplit = status.completeBatchContents[i] as SftpReaderBatchContents;
            if (!batchSplit.notified) {
                batchSplit.selected = true;
            }
        }
    }

    ignoreBatchSplits(status: SftpReaderChannelStatus) {
        var vm = this;

        var selected = [];
        for (var i=0; i<status.completeBatchContents.length; i++) {
            var batchSplit = status.completeBatchContents[i];
            if (batchSplit.selected) {
                selected.push(batchSplit);
            }
        }

        if (selected.length == 0) {
            vm.logger.warning("No errors selected");
            return;
        }

        var reason = prompt('Enter reason');
        if (reason == null) {
            return;
        }

        for (var j=0; j<selected.length; j++) {
            var batchSplit = selected[j] as SftpReaderBatchContents;
            vm.ignoreBatchSplit(status, batchSplit, reason);
        }
    }

    ignoreBatchSplit(status: SftpReaderChannelStatus, batchSplit: SftpReaderBatchContents, reason: string) {
        var vm = this;

        vm.sftpReaderService.ignoreBatchSplit(status.latestBatchId, batchSplit.batchSplitId, status.id, reason).subscribe(
            (result) => {
                batchSplit.error = null;
                batchSplit.result = reason;
                batchSplit.notified = true;
            },
            (error) => {
                vm.logger.error('Failed to ignore batch split', error, 'SFTP Reader Error');
            }
        )
    }*/

    togglePauseAll() {
        var vm = this;
        vm.sftpReaderService.togglePauseAll().subscribe(
            (result) => {
                vm.refreshInstances(false);
            },
            (error) => {
                vm.logger.error('Failed to toggle pause', error);
            }
        );
    }

    togglePause(configuration: SftpReaderConfiguration) {
        var vm = this;
        var configurationId = configuration.configurationId;
        vm.sftpReaderService.togglePause(configurationId).subscribe(
            (result) => {
                vm.refreshInstances(false);
            },
            (error) => {
                vm.logger.error('Failed to toggle pause', error);
            }
        );
    }

    viewOrgs(status: SftpReaderChannelStatus, contents: SftpReaderBatchContents[]) {
        var vm = this;
        var configurationId = status.id;
        var batchId = status.completeBatchId;
        SftpReaderOrgsDialog.open(vm.$modal, configurationId, batchId, contents);
    }

    getInstanceNames(): string[] {
        var vm = this;

        var ret = [];

        if (vm.configurations) {
            for (var i=0; i<vm.configurations.length; i++) {
                var configuration = vm.configurations[i];
                var instanceName = configuration.instanceName;
                if (ret.indexOf(instanceName) == -1) {
                    ret.push(instanceName);
                }
            }
        }

        ret.sort();

        return ret;
    }


    /**
     * saves current list to CSV
     */
    saveToCsv() {

        var vm = this;

        //create CSV content in a String
        var lines = [];
        var line;

        line = '\"Configuration ID\",' +
            '\"Configuration Name\",' +
            '\"Last Checked for Data\",' +
            '\"Error Checking\",' +
            '\"Last Extract Received\",' +
            '\"Data From\",' +
            '\"Files Received\",' +
            '\"Extract Size\",' +
            '\"Valid and Complete\",' +
            '\"Orgs in Extract\"';
        lines.push(line);

        var configs = vm.getConfigurationsToDisplay();
        for (var i=0; i<configs.length; i++) {
            var config = configs[i] as SftpReaderConfiguration;

            var statusArr = vm.getStatusToDisplay(config);
            if (statusArr.length == 0) {
                vm.logger.error('Cannot save while loading');
                return;
            }

            var status = statusArr[0];

            var cols = [];

            cols.push(config.configurationId);
            cols.push(config.friendlyName);


            if (status.latestPollingStart) {
                var d = new Date();
                d.setTime(status.latestPollingStart);
                var s = ServiceListComponent.formatDate(d);
                cols.push(s);
            } else {
                cols.push('Never');
            }

            if (status.latestPollingException) {
                cols.push('Y');
            } else {
                cols.push('N');
            }

            if (status.latestBatchReceived) {
                var d = new Date();
                d.setTime(status.latestBatchReceived);
                var s = ServiceListComponent.formatDate(d);
                cols.push(s);
            } else {
                cols.push('Never');
            }

            if (status.latestBatchIdentifier) {
                cols.push(status.latestBatchIdentifier);
            } else {
                cols.push('');
            }

            if (status.latestBatchFileCount) {
                cols.push(status.latestBatchFileCount);
            } else {
                cols.push('');
            }

            if (status.latestBatchSizeBytes) {
                cols.push(status.latestBatchSizeBytes);
            } else {
                cols.push('');
            }

            if (status.latestBatchComplete) {
                cols.push('Y');
            } else {
                cols.push('N');
            }

            if (status.completeBatchContents) {
                cols.push(status.completeBatchContents.length);
            } else {
                cols.push('Unknown');
            }

            line = '\"' + cols.join('\",\"') + '\"';
            lines.push(line);
        }

        var csvStr = lines.join('\r\n');

        const filename = 'SFTP_status.csv';
        const blob = new Blob([csvStr], { type: 'text/plain' });

        let url = window.URL.createObjectURL(blob);
        let a = document.createElement('a');
        document.body.appendChild(a);
        a.setAttribute('style', 'display: none');
        a.href = url;
        a.download = filename;
        a.click();
        window.URL.revokeObjectURL(url);
        a.remove();
    }

    isApplicationDead(status: QueueReaderStatus): boolean {
        var vm = this;
        //console.log('getting status');
        //console.log(status);
        var statusTime = status.timestmp;
        var warningTime = vm.applicationStatusLastRefreshed.getTime() - (1000 * 60 * 2);

        return statusTime < warningTime;
    }

    getApplicationAgeDesc(status: QueueReaderStatus): string {
        var vm = this;
        return DateTimeFormatter.getDateDiffDesc(new Date(status.timestmp), vm.applicationStatusLastRefreshed, 2);
    }

    getApplicationCellColour(status: QueueReaderStatus): any {
        var vm = this;
        //var hostName = status.hostName;
        //var colour = vm.hostNameColourMap[hostName];
        var colour = '#e5e7e9';
        return {'background-color': colour};
    }


    isApplicationNeedsRestart(status: QueueReaderStatus): boolean {
        if (!status.dtJar
            || !status.dtStarted) {
            return false;
        }

        //it needs a restart if the jar date is more recent than the start date
        return status.dtJar > status.dtStarted;
    }

    getApplicationNeedsRestartDesc(status: QueueReaderStatus): string {

        var vm = this;

        if (!status.dtJar
            || !status.dtStarted) {
            return '';
        }

        var dtJar = new Date(status.dtJar);
        var dtStarted = new Date(status.dtStarted);
        return 'Jar built: ' + ServiceListComponent.formatDate(dtJar) + ', app started: ' + ServiceListComponent.formatDate(dtStarted);
    }

    getApplicationExecutionTime(status: QueueReaderStatus): string {
        var vm = this;
        if (!status.isBusySince) {
            return '';
        } else {
            return DateTimeFormatter.getDateDiffDesc(new Date(status.isBusySince), new Date(status.timestmp), 1);
        }
    }
}