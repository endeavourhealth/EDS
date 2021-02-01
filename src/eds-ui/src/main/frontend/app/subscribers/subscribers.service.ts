import {BaseHttp2Service} from "eds-common-js";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";
import {SubscriberConfiguration} from "./models/SubscriberConfiguration";

@Injectable()
export class SubscribersService extends BaseHttp2Service {

    //filters for Subscribers page so they don't get reset when leaving and returning
    showDates: boolean; //show dates vs show date diff
    publisherNameFilter: string;
    systemNameFilter: string;
    sortFilter: string;
    statusFilter: string;
    showFullSubscriberConfigDetails: boolean;

    constructor(http : Http) {
        super (http);

        var vm = this;
        vm.showDates = false;
        vm.showFullSubscriberConfigDetails = false;
        vm.sortFilter = 'NameAsc';
        vm.statusFilter = 'any-issue';
    }

    getSubscribersInstances() : Observable<SubscriberConfiguration[]> {
        return this.httpGet('api/subscribers/subscribers', { });
    }

    getSubscriberDetails(subscriberName: string) : Observable<SubscriberConfiguration> {

        var params = new URLSearchParams();
        params.append('subscriberName', '' + subscriberName);

        return this.httpGet('api/subscribers/subscriberDetail', { search : params});
    }
}
