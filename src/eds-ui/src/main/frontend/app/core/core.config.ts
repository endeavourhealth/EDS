import IKeepAliveProvider = angular.idle.IKeepAliveProvider;
import IIdleProvider = angular.idle.IIdleProvider;
import IHttpProvider = angular.IHttpProvider;
import IToastrConfig = angular.toastr.IToastrConfig;

export class Config {

	static $inject = ['$httpProvider', 'IdleProvider', 'KeepaliveProvider', 'toastrConfig'];

	constructor(
		$httpProvider:IHttpProvider,
		IdleProvider:IIdleProvider,
		KeepaliveProvider:IKeepAliveProvider,
		toastrConfig : IToastrConfig) {
		$httpProvider.defaults.headers.post['Accept'] = 'application/json';
		$httpProvider.defaults.headers.post['Content-Type'] = 'application/json; charset=utf-8';

		toastrConfig.timeOut = 4000;
		toastrConfig.positionClass = 'toast-bottom-right';
		IdleProvider.idle(300);
		IdleProvider.timeout(10);
		KeepaliveProvider.interval(10);
	}
}
