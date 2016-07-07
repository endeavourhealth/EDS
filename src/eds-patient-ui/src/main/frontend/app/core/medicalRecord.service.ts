/// <reference path="../../typings/index.d.ts" />

module app.core {
    import FolderItem = app.models.FolderItem;
    'use strict';

    export interface IMedicalRecordService {
        getRecentDocumentsData() : ng.IPromise<FolderItem[]>;
    }

    export class MedicalRecordService extends BaseHttpService implements IMedicalRecordService {

        getRecentDocumentsData():ng.IPromise<FolderItem[]> {
            var request = {
                params: {
                    'count': 5
                }
            };

            return this.httpGet('api/dashboard/getRecentDocuments', request);
        }

    }

    angular
        .module('app.core')
        .service('MedicalRecordService', MedicalRecordService);
}