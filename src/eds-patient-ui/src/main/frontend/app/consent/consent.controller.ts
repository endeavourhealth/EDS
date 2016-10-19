import {ILoggerService} from "../blocks/logger.service";
import {IConsentService} from "../core/consent.service";

export class ConsentController {

    static $inject = ['ConsentService', 'LoggerService', '$state'];

    constructor(private consentService:IConsentService,
                private logger:ILoggerService) {
        //this.refresh();
    }
}

