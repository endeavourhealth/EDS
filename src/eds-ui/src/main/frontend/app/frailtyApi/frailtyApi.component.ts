import {linq, LoggerService} from "eds-common-js";
import {StateService} from "ui-router-ng2";
import {Component} from "@angular/core";
import {Subscription} from "rxjs/Subscription";
import {ServiceService} from "../services/service.service";
import {FrailtyApiService} from "./frailtyApi.service";
import {FrailtyStat} from "./FrailtyStat";

@Component({
    template : require('./frailtyApi.html')
})
export class FrailtyApiComponent {

    recentStatsGroupBy: string;
    recentStatsMinutesBack: number;
    recentStats: FrailtyStat[];
    recentStatResultsGroupBy: string;
    refreshingRecentStats: boolean;

    downloadingMonthlyStats: boolean;

    constructor(protected frailtyService:FrailtyApiService,
                protected logger:LoggerService,
                protected $state:StateService) {


    }

    ngOnInit() {
        this.recentStatsMinutesBack = 20;
        this.recentStatsGroupBy = 'minute';
        this.refreshStatus();
    }

    refreshStatus() {
        var vm = this;
        vm.refreshingRecentStats = true;

        vm.frailtyService.getRecentStats(vm.recentStatsMinutesBack, vm.recentStatsGroupBy).subscribe(
            (result) => {
                vm.recentStats = result;
                vm.recentStatResultsGroupBy = vm.recentStatsGroupBy;
                vm.refreshingRecentStats = false;

            },
            (error) => {
                vm.logger.error('Failed get Frailty API stats', error, 'Frailty API');
                vm.refreshingRecentStats = false;
            }
        );
    }

    downloadMonthlyStats() {
        var vm = this;
        vm.downloadingMonthlyStats = true;

        vm.frailtyService.downloadMonthlyStats().subscribe(
            (result) => {
                const filename = 'FrailtyStats.csv';
                const blob = new Blob([result], { type: 'text/plain' });

                //window['saveAs'](blob, filename);

                let url = window.URL.createObjectURL(blob);
                let a = document.createElement('a');
                document.body.appendChild(a);
                a.setAttribute('style', 'display: none');
                a.href = url;
                a.download = filename;
                a.click();
                window.URL.revokeObjectURL(url);
                a.remove();

                vm.downloadingMonthlyStats = false;
            },
            (error) => {
                vm.logger.error('Failed get monthly Frailty API stats', error, 'Frailty API');
                vm.downloadingMonthlyStats = false;
            }
        );

    }

}