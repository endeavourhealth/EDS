import IHttpProvider = angular.IHttpProvider;
import IIdleProvider = angular.idle.IIdleProvider;
import IKeepAliveProvider = angular.idle.IKeepAliveProvider;

export class Config {

	static $inject = ['$httpProvider', 'IdleProvider', 'KeepaliveProvider'];

	constructor(
		$httpProvider:IHttpProvider,
		IdleProvider:IIdleProvider,
		KeepaliveProvider:IKeepAliveProvider) {
		$httpProvider.defaults.headers.post['Accept'] = 'application/json';
		$httpProvider.defaults.headers.post['Content-Type'] = 'application/json; charset=utf-8';
		$httpProvider.defaults.withCredentials = true;

		IdleProvider.idle(300);
		IdleProvider.timeout(10);
		KeepaliveProvider.interval(10);
	}
}
