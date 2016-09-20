/// <reference path="../../typings/index.d.ts" />
/// <reference path="../blocks/logger.service.ts" />

module app.consent {
    import IConsentService = app.core.IConsentService;
    import ILoggerService = app.blocks.ILoggerService;

    'use strict';

    class ConsentController {

        static $inject = ['ConsentService', 'LoggerService', '$state'];

        constructor(private consentService:IConsentService,
                    private logger:ILoggerService,
                    private $state : IStateService) {
            //this.refresh();
        }
    }

    angular
        .module('app.consent')
        .controller('ConsentController', ConsentController);
}
