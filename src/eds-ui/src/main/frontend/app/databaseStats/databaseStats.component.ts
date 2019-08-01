import {linq, LoggerService} from "eds-common-js";
import {StateService} from "ui-router-ng2";
import {Component} from "@angular/core";
import {Subscription} from "rxjs/Subscription";
import {ServiceService} from "../services/service.service";
import {DatabaseStatsService} from "./databaseStats.service";
import {DatabaseHost} from "./DatabaseHost";
import {Database} from "./Database";

@Component({
    template : require('./databaseStats.html')
})
export class DatabaseStatsComponent {

    refreshingStatus: boolean;
    hosts: DatabaseHost[];
    databaseSortField: string;
    databaseSortAsc: boolean;

    constructor(protected databaseStatsService:DatabaseStatsService,
                protected logger:LoggerService,
                protected $state:StateService) {


    }

    ngOnInit() {
        var vm = this;
        vm.databaseSortField = 'name';
        vm.databaseSortAsc = true;
        vm.refreshHosts();
    }

    refreshHosts() {
        var vm = this;
        vm.refreshingStatus = true;

        vm.databaseStatsService.getDatabaseServers().subscribe(
            (result) => {
                //sort by host name
                //vm.hosts = result;
                vm.hosts = linq(result).OrderBy(s => s.host.toLowerCase()).ToArray();

                vm.refreshingStatus = false;
            },
            (error) => {
                vm.logger.error('Failed to get database server list', error, 'Database Stats');
                vm.refreshingStatus = false;
            }
        )
    }

    toggleHostExpanded(host: DatabaseHost) {

        if (host.expanded
            || host.databases
            || host.error) {

            host.expanded = !host.expanded;
            return;
        }

        host.refreshingDatabases = true;

        var vm = this;
        vm.databaseStatsService.getDatabaseSizes(host).subscribe(
            (result) => {

                //see if the result contained an error
                if (result.length == 1 && result[0].error) {
                    host.error = result[0].error;

                } else {
                    host.databases = result;
                }

                host.refreshingDatabases = false;
                host.expanded = true;
            },
            (error) => {
                vm.logger.error('Failed to get database sizes on ' + host, error, 'Database Stats');
                host.refreshingDatabases = false;
            }
        )
    }

    getPanelClass(host: DatabaseHost): string {
        if (host.error) {
            return 'panel panel-danger';
        } else if (host.databases) {
            return 'panel panel-success';
        } else {
            return 'panel panel-info';
        }
    }

    expandAllHosts(expand: boolean) {
        var vm = this;
        var i;
        for (i=0; i<vm.hosts.length; i++) {
            var host = vm.hosts[i];

            if (expand != host.expanded) {
                vm.toggleHostExpanded(host);
            }
        }
    }

    getSortedDatabases(host: DatabaseHost): Database[] {

        var ret;

        var vm = this;
        if (vm.databaseSortField == 'name') {
            ret = linq(host.databases).OrderBy(s => s.name.toLowerCase()).ToArray();

        } else if (vm.databaseSortField == 'size') {
            ret = linq(host.databases).OrderBy(s => s.sizeBytes).ToArray();

        } else {
            console.log('Unknown sort mode [' + vm.databaseSortField + "]");
        }

        if (!vm.databaseSortAsc) {
            ret = ret.reverse();
        }

        return ret;
    }

    sortDatabases(sortBy: string) {
        var vm = this;
        if (vm.databaseSortField == sortBy) {
            vm.databaseSortAsc = !vm.databaseSortAsc;
        } else {
            vm.databaseSortField = sortBy;
            vm.databaseSortAsc = true;
        }
    }
}