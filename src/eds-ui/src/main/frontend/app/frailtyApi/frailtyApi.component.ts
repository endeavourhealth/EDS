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

    /**
     * if we've received no messages in a minute, that's not a big deal. But if we've a sustained outage over a period of time, then
     * something is wrong, so highligh as an error
     */
    isSustainedOutage(queryStat: FrailtyStat): boolean {
        var vm = this;

        //if this stat doesn't represent an outage at all, then return false
        if (queryStat.total > 0) {
            return false;
        }

        var warningThreshold = 5 * 60 * 1000; //warn at five minutes of sustained zero throughput
        var cumulativeMsOutage = 0;
        var matchedOnStat = false;

        for (var i=0; i<vm.recentStats.length; i++) {
            var stat = vm.recentStats[i];

            if (stat.total == 0) {

                if (stat == queryStat) {
                    matchedOnStat = true;
                }

                var msOutage = stat.dTo - stat.dFrom;
                cumulativeMsOutage += msOutage;

                //if we've had a continuous period of outage above our threshold
                //AND that continuous period contains the stat we're displaying, then return true
                if (cumulativeMsOutage >= warningThreshold
                    && matchedOnStat) {
                    return true;
                }

            } else {
                //reset
                cumulativeMsOutage = 0;
                matchedOnStat = false;
            }
        }

        return false;
    }

}