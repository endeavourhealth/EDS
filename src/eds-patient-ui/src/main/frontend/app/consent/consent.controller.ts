import {ILoggerService} from "../blocks/logger.service";
import {IConsentService} from "../core/consent.service";
import {IStateService} from "angular-ui-router";

export class ConsentController {

    static $inject = ['ConsentService', 'LoggerService', '$state'];

    constructor(private consentService:IConsentService,
                private logger:ILoggerService,
                private $state : IStateService) {
        //this.refresh();
    }
}

