import {linq, LoggerService} from "eds-common-js";
import {StateService} from "ui-router-ng2";
import {Component} from "@angular/core";
import {Subscription} from "rxjs/Subscription";
import {ServiceService} from "../services/service.service";
import {ConfigRecord} from "./ConfigRecord";
import {ConfigManagerService} from "./configManager.service";
import {MessageBoxDialog} from "eds-common-js/dist/index";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ConfigHistory} from "./ConfigHistory";

@Component({
    template : require('./configManager.html')
})
export class ConfigManagerComponent {

    refreshingStatus: boolean;
    records: ConfigRecord[];
    selectedRecord: ConfigRecord;
    newRecords: ConfigRecord[];
    changedRecords: ConfigRecord[];
    validJsonMessage: string;
    invalidJsonMessage: string;
    filterAppId: string;
    filterAppIdIncludeGlobal: boolean;
    filterSearchText: string;
    filterSearchOnAppId: boolean;
    filterSearchOnConfigId: boolean;
    filterSearchOnData: boolean;
    filteredRecords: ConfigRecord[];
    historyMap: {};
    refreshingHistory: boolean;
    selectedHistory: ConfigHistory;

    constructor(private $modal: NgbModal,
                private configManagerService: ConfigManagerService,
                private logger: LoggerService,
                private $state: StateService) {

        var vm = this;
        vm.filterAppIdIncludeGlobal = false;
        vm.filterSearchOnConfigId = true;
    }

    ngOnInit() {
        this.refreshStatus();
    }

    refreshStatusWithValidation() {
        var vm = this;
        if (vm.newRecords.length > 0
            || vm.changedRecords.length > 0) {

            MessageBoxDialog.open(vm.$modal, 'Refresh', 'Refreshing will lose unsaved changes. Continue?', 'Yes', 'No')
                .result.then(
                () => vm.refreshStatus(),
                () => {}
            );

        } else {
            vm.refreshStatus();
        }
    }

    refreshStatus() {
        var vm = this;
        vm.refreshingStatus = true;
        vm.newRecords = [];
        vm.changedRecords = [];
        vm.selectedRecord = null;
        vm.validJsonMessage = null;
        vm.invalidJsonMessage = null;
        vm.filteredRecords = null;
        vm.historyMap = {};
        vm.selectedHistory = null;

        vm.configManagerService.getRecords().subscribe(
            (result) => {
                vm.logger.success('Successfully got config records');

                vm.records = linq(result)
                        .OrderBy(s => s.appId.toLowerCase())
                        .ThenBy(s => s.configId.toLowerCase())
                        .ToArray();
                vm.applyFiltering();
                vm.refreshingStatus = false;
            },
            (error) => {
                vm.logger.error('Failed get config records');
                vm.refreshingStatus = false;
            }
        )
    }

    selectRecord(record: ConfigRecord) {
        var vm = this;
        vm.selectedRecord = record;
        vm.validJsonMessage = null;
        vm.invalidJsonMessage = null;
        vm.selectedHistory = null;
    }

    saveRecord() {
        var vm = this;
        var record = vm.selectedRecord;
        if (!record.appId
            || record.appId == '<app_id>') {
            vm.logger.error('App ID not set');
            return;
        }
        if (!record.configId
            || record.configId == '<config_id>') {
            vm.logger.error('Config ID not set');
            return;
        }
        if (!record.configData) {
            vm.logger.error('Config data not set');
            return;
        }

        //validate not a duplicate that will overwrite something else
        for (var i=0; i<vm.records.length; i++) {
            var otherRecord = vm.records[i];
            if (otherRecord != record) {
                if (otherRecord.appId == record.appId
                    && otherRecord.configId == record.configId) {
                    vm.logger.error('Duplicate record');
                    return;
                }
            }
        }

        vm.configManagerService.saveRecord(record).subscribe(
            (result) => {
                vm.logger.success('Saved config record');

                //remove from new records if present
                var index = vm.newRecords.indexOf(record);
                if (index > -1) {
                    vm.newRecords.splice(index, 1);
                }
                index = vm.changedRecords.indexOf(record);
                if (index > -1) {
                    vm.changedRecords.splice(index, 1);
                }

            },
            (error) => {
                vm.logger.error('Failed to save config record');
            }
        );

    }

    deleteRecord() {
        var vm = this;
        var record = vm.selectedRecord;
        if (!record) {
            return;
        }

        MessageBoxDialog.open(vm.$modal, 'Delete Record', 'Are you sure you want to delete this config record?', 'Yes', 'No')
            .result.then(
            () => vm.doDeleteRecord(record),
            () => {}
        );
    }

    doDeleteRecord(record: ConfigRecord) {
        var vm = this;
        vm.configManagerService.deleteRecord(record).subscribe(
            (result) => {
                vm.logger.success('Delete config record');
                vm.refreshStatus();
            },
            (error) => {
                vm.logger.error('Failed to delete config record');
            }
        );
    }

    copyRecord() {
        var vm = this;
        var record = vm.selectedRecord;
        if (!record) {
            return;
        }

        vm.newRecordImpl(record);
    }

    newRecord() {
        var vm = this;
        vm.newRecordImpl(null);
    }

    private newRecordImpl(copyFrom: ConfigRecord) {

        var vm = this;
        vm.selectedRecord = {} as ConfigRecord;
        vm.selectedRecord.appId = '<app_id>';
        vm.selectedRecord.configId = '<config_id>';

        if (copyFrom) {
            vm.selectedRecord.appId = copyFrom.appId;
            vm.selectedRecord.configId = copyFrom.configId + ' (copy)';
            vm.selectedRecord.configData = copyFrom.configData;
        }

        vm.records.push(vm.selectedRecord);
        vm.newRecords.push(vm.selectedRecord);
        vm.filteredRecords.push(vm.selectedRecord);
    }

    isNewRecord(record: ConfigRecord): boolean {
        var vm = this;
        return vm.newRecords.indexOf(record) > -1;
    }

    isChangedRecord(record: ConfigRecord): boolean {
        var vm = this;
        return vm.changedRecords.indexOf(record) > -1;
    }

    validateJSON() {
        var vm = this;
        vm.validJsonMessage = null;
        vm.invalidJsonMessage = null;

        var str = vm.selectedRecord.configData;

        try {
            JSON.parse(str);
            vm.validJsonMessage = 'Valid JSON';
            //vm.logger.success('Valid JSON');

        } catch (e) {
            vm.invalidJsonMessage = 'Invalid JSON: ' + e.message;
            //console.log('error: '+ e.message);
            //vm.logger.error('Invalid JSON');
         }
    }

    validateXML() {
        var vm = this;
        vm.validJsonMessage = null;
        vm.invalidJsonMessage = null;

        var str = vm.selectedRecord.configData;

        var oParser = new DOMParser();
        var oDOM = oParser.parseFromString(str, "text/xml");

        if (oDOM.getElementsByTagName('parsererror').length > 0) {
            vm.invalidJsonMessage = oDOM.getElementsByTagName('parsererror')[0].getElementsByTagName('div')[0].innerHTML;
        } else {
            vm.validJsonMessage = 'Valid XML';
        }

        /*var error = oDOM.getElementsByTagName('parsererror');
        console.log('error = [' + error + ']');*/

    /*.length ?
            (new XMLSerializer()).serializeToString(oDOM) : "all good"
        );

        var root = (oDOM.documentElement.nodeName == "parsererror" ? "error while parsing" : oDOM.documentElement.nodeName);
        console.log('parsed = [' + root + ']');
        console.log(oDOM);*/

        /*
        try {
            JSON.parse(str);
            vm.validJsonMessage = 'Valid JSON';
            //vm.logger.success('Valid JSON');

        } catch (e) {
            vm.invalidJsonMessage = 'Invalid JSON: ' + e.message;
            //console.log('error: '+ e.message);
            //vm.logger.error('Invalid JSON');
        }*/
    }

    configJsonChanged(record: ConfigRecord) {
        var vm = this;
        vm.validJsonMessage = null;
        vm.invalidJsonMessage = null;

        var index = vm.changedRecords.indexOf(record);
        if (index == -1) {
            vm.changedRecords.push(record);
        }
    }

    getAppIds(): string[] {
        var vm = this;
        var ret = [];

        if (vm.records) {
            for (var i=0; i<vm.records.length; i++) {
                var record = vm.records[i];
                var appId = record.appId;
                if (ret.indexOf(appId) == -1) {
                    ret.push(appId);
                }
            }
        }

        return ret;
    }

    applyFiltering() {
        var vm = this;
        vm.filteredRecords = [];

        //work out if the name/ID search text is valid regex and force it to lower case if so
        var validConfigDataSearchRegex;
        if (vm.filterSearchText) {
            try {
                new RegExp(vm.filterSearchText);
                validConfigDataSearchRegex = vm.filterSearchText.toLowerCase();
            } catch (e) {
                //do nothing and it'll ignore it in the search
            }
        }


        if (vm.records) {
            for (var i=0; i<vm.records.length; i++) {
                var record = vm.records[i];

                if (vm.filterAppId) {
                    var appId = record.appId;
                    if (vm.filterAppId != appId
                        && (appId != 'global' || !vm.filterAppIdIncludeGlobal)) {
                        continue;
                    }
                }

                if (validConfigDataSearchRegex) {

                    var matches = false;

                    var appId = record.appId;
                    var configId = record.configId;
                    var data = record.configData;
                    if (
                        (vm.filterSearchOnAppId && appId && appId.toLowerCase().match(validConfigDataSearchRegex))
                        || (vm.filterSearchOnConfigId && configId && configId.toLowerCase().match(validConfigDataSearchRegex))
                        || (vm.filterSearchOnData && data && data.toLowerCase().match(validConfigDataSearchRegex))) {
                        matches = true;
                    }

                    if (!matches) {
                        continue;
                    }
                }

                vm.filteredRecords.push(record);
            }

            //if selected record isn't in the filtered list, de-select it
            if (vm.selectedRecord) {
                if (vm.filteredRecords.indexOf(vm.selectedRecord) == -1) {
                    vm.selectedRecord = null;
                }
            }
        }

    }

    hasHistory(record: ConfigRecord): boolean {
        var vm = this;
        var key = record.appId + '_' + record.configId;
        return vm.historyMap.hasOwnProperty(key);
    }

    retrieveHistory(record: ConfigRecord) {
        var vm = this;
        var key = record.appId + '_' + record.configId;

        vm.refreshingHistory = true;

        vm.configManagerService.getHistory(record.appId, record.configId).subscribe(
            (result) => {
                vm.logger.success('Successfully config history');
                vm.historyMap[key] = result.reverse(); //reverse so most-recent-first
                vm.refreshingHistory = false;
            },
            (error) => {
                vm.logger.error('Failed get config history');
                vm.refreshingHistory = false;
            }
        )
    }

    getHistory(record: ConfigRecord) : ConfigHistory[] {
        var vm = this;
        var key = record.appId + '_' + record.configId;
        return vm.historyMap[key];
    }

    selectHistory(history: ConfigHistory) {
        var vm = this;
        vm.selectedHistory = history;
    }
}