/// <reference path="../../typings/tsd.d.ts" />

module app.core {
	import IIdleProvider = angular.idle.IIdleProvider;
	import IKeepAliveProvider = angular.idle.IKeepAliveProvider;
	import IHttpProvider = angular.IHttpProvider;
	'use strict';

	class Config {

		static $inject = ['$httpProvider', 'IdleProvider', 'KeepaliveProvider'];

		constructor(
			$httpProvider:IHttpProvider,
			IdleProvider:IIdleProvider,
			KeepaliveProvider:IKeepAliveProvider) {
			$httpProvider.defaults.headers.post['Accept'] = 'application/json';
			$httpProvider.defaults.headers.post['Content-Type'] = 'application/json; charset=utf-8';
			$httpProvider.defaults.withCredentials = true;

			toastr.options.timeOut = 4000;
			toastr.options.positionClass = 'toast-bottom-right';
			IdleProvider.idle(300);
			IdleProvider.timeout(10);
			KeepaliveProvider.interval(10);
		}
	}

	angular
		.module('app.core')
		.config(Config);
}