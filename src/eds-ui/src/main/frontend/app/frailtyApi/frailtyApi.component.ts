import {linq, LoggerService} from "eds-common-js";
import {StateService} from "ui-router-ng2";
import {Component} from "@angular/core";
import {Subscription} from "rxjs/Subscription";
import {ServiceService} from "../services/service.service";
import {FrailtyApiService} from "./frailtyApi.service";

@Component({
    template : require('./frailtyApi.html')
})
export class FrailtyApiComponent {

    resultStr: string;

    constructor(protected frailtyService:FrailtyApiService,
                protected logger:LoggerService,
                protected $state:StateService) {


    }

    ngOnInit() {
        this.refreshStatus();
    }

    refreshStatus() {
        var vm = this;



    }
}