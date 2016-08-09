/// <reference path="../../typings/index.d.ts" />

module app.core {
    'use strict';
    import IAuth = app.appstartup.IAuth;

    export class AuthInterceptor {
        static $inject = ['$q'];

        constructor(protected promise:ng.IQService) {
        }

        public request = (config : any) =>
        {
            var deferred = this.promise.defer();
            var authz = Auth.factory().getAuthz();
            if (authz != null && authz.token) {
                authz.updateToken(5).success(function () {
                    config.headers = config.headers || {};
                    config.headers.Authorization = 'Bearer ' + authz.token;

                    deferred.resolve(config);
                }).error(function () {
                    deferred.reject('Failed to refresh token');
                });
            } else {
                deferred.resolve(config);
            }
            return deferred.promise;
        }
    }

    angular
        .module('app.core')
        .service('AuthInterceptor', AuthInterceptor)
        .config(['$httpProvider', function ($httpProvider: ng.IHttpProvider) {
            $httpProvider.interceptors.push('AuthInterceptor');
        }]);
}