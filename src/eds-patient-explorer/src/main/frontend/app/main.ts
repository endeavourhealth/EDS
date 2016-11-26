import '../content/css/index.css';
import '../content/less/index.less';

import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { enableProdMode } from '@angular/core';

import { AppModule } from './app.module';
import {AuthConfig} from "./security/models/AuthConfig";
import {Auth} from "./security/security.auth";
import {WellKnownConfig} from "./security/models/WellKnownConfig";

//enableProdMode(); //Uncomment for production

// *** USE JQUERY TO BOOTSTRAP APPLICATION ONCE KEYCLOAK IS AUTHORIZED ***
$('document').ready( function() {

	var wellKnownConfig: WellKnownConfig = WellKnownConfig.factory();

	var defer = jQuery.Deferred();

// try to read the auth configuration from local storage, if not found, get it from the public API and store it
	var path: string = 'eds.config.auth';
	var text: string = localStorage.getItem(path);
	if (text === null || typeof text === "undefined" || text === "undefined") {
		// use jQuery to avoid angular http interceptors
		jQuery.getJSON("/public/wellknown/authconfig", (data: any, textStatus: string, jqXHR: any) => {
			var authConfig = data as AuthConfig;
			localStorage.setItem(path, JSON.stringify(authConfig));
			defer.resolve(authConfig);
		});
	}
	else {
		defer.resolve(<AuthConfig>JSON.parse(text));
	}

	jQuery.when(defer.promise()).then(
		function (authConfig: AuthConfig) {
			// set the config
			wellKnownConfig.setAuthConfig(authConfig);

			Auth.factory().setOnAuthSuccess(()=> {
				// manually bootstrap angular
				platformBrowserDynamic().bootstrapModule(AppModule)
					.then((success: any) => console.log('App bootstrapped'))
					.catch((err: any) => console.error(err));
			});

			Auth.factory().setOnAuthError(()=> {
				console.log('Failed to start app as not authenticated, check the well known auth configuration.')
			});

			Auth.factory().init();
		});
});