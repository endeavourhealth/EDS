/// <reference path="../../typings/index.d.ts" />

module app.core {
    'use strict';

    export interface IAuditService {
    }

    export class AuditService extends BaseHttpService implements IAuditService {

    }

    angular
        .module('app.core')
        .service('AuditService', AuditService);
}