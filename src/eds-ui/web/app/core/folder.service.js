/// <reference path="../../typings/tsd.d.ts" />
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var app;
(function (app) {
    var core;
    (function (core) {
        'use strict';
        var FolderService = (function (_super) {
            __extends(FolderService, _super);
            function FolderService() {
                _super.apply(this, arguments);
            }
            FolderService.prototype.getFolders = function (moduleId, folderUuid) {
                var request = {
                    params: {
                        'folderType': moduleId,
                        'parentUuid': folderUuid
                    }
                };
                return this.httpGet('api/folder/getFolders', request);
            };
            FolderService.prototype.getFolderContents = function (folderUuid) {
                var request = {
                    params: {
                        'folderUuid': folderUuid
                    }
                };
                return this.httpGet('api/folder/getFolderContents', request);
            };
            FolderService.prototype.saveFolder = function (folder) {
                // Make clean copy of object, just in case of additions
                // Typing the request ensures any property changes are caught
                var request = {
                    uuid: folder.uuid,
                    folderName: folder.folderName,
                    folderType: folder.folderType,
                    parentFolderUuid: folder.parentFolderUuid,
                    hasChildren: folder.hasChildren,
                    contentCount: folder.contentCount
                };
                return this.httpPost('api/folder/saveFolder', request);
            };
            FolderService.prototype.deleteFolder = function (folder) {
                // Make clean copy of object, just in case of additions
                // Typing the request ensures any property changes are caught
                var request = {
                    uuid: folder.uuid,
                    folderName: folder.folderName,
                    folderType: folder.folderType,
                    parentFolderUuid: folder.parentFolderUuid,
                    hasChildren: folder.hasChildren,
                    contentCount: folder.contentCount
                };
                return this.httpPost('api/folder/deleteFolder', request);
            };
            return FolderService;
        })(core.BaseHttpService);
        core.FolderService = FolderService;
        angular
            .module('app.core')
            .service('FolderService', FolderService);
    })(core = app.core || (app.core = {}));
})(app || (app = {}));
//# sourceMappingURL=folder.service.js.map