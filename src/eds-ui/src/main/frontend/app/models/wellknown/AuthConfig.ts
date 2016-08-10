module app.models.wellknown {
	'use strict';

	export class AuthConfig {

        constructor() {
		}

		realm : string;
		authServerUrl : string;
		authClientId : string;
		appUrl : string;
	}
}