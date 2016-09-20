/// <reference path="../../typings/index.d.ts" />

module app.core {
    'use strict';

    export interface IConsentService {
    }

    export class ConsentService extends BaseHttpService implements IConsentService {

    }

    angular
        .module('app.core')
        .service('ConsentService', ConsentService);
}