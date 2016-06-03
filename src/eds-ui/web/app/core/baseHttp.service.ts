/// <reference path="../../typings/tsd.d.ts" />

module app.core {
	'use strict';

	export class BaseHttpService {
		static $inject = ['$http', '$q'];

		constructor(protected http:ng.IHttpService, protected promise:ng.IQService) {
		}

		httpGet(url : string, request? : any) : ng.IPromise<any> {
			var defer = this.promise.defer();
			this.http.get(url, request)
				.then(function (response) {
					defer.resolve(response.data);
				})
				.catch(function (exception) {
					defer.reject(exception);
				});

			return defer.promise;
		}

		httpPost(url : string, request? : any) : ng.IPromise<any> {
			var defer = this.promise.defer();
			this.http.post(url, request)
				.then(function (response) {
					defer.resolve(response.data);
				})
				.catch(function (exception) {
					defer.reject(exception);
				});

			return defer.promise;
		}
	}
}