/// <reference path="../../typings/index.d.ts" />
/// <reference path="../blocks/logger.service.ts" />

module app.audit {
    import IAuditService = app.core.IAuditService;
    import ILoggerService = app.blocks.ILoggerService;

    'use strict';

    class AuditController {

        static $inject = ['AuditService', 'LoggerService', '$state'];

        constructor(private auditService:IAuditService,
                    private logger:ILoggerService,
                    private $state : IStateService) {
            //this.refresh();
        }
    }

    angular
        .module('app.audit')
        .controller('AuditController', AuditController);
}
