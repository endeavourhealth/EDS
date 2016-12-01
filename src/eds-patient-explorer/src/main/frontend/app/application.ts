import '../content/css/index.css';
import '../content/less/index.less';

import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { enableProdMode } from '@angular/core';

import {WellKnownConfig} from "./security/models/WellKnownConfig";
import {AuthConfig} from "./security/models/AuthConfig";
import {Auth} from "./security/security.auth";

import {BrowserModule} from '@angular/platform-browser';
import {FormsModule} from '@angular/forms';
import {HttpModule, RequestOptions, XHRBackend, Http} from '@angular/http';
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {ToastModule, ToastOptions} from "ng2-toastr";
import {UIRouterModule, RootModule, UIView} from 'ui-router-ng2';
import {TreeModule} from "angular2-tree-component";

import {AuthHttpService} from "./security/authHttp.service";

// Modules
import {CommonModule} from "./common/common.module";
import {DialogsModule} from "./dialogs/dialogs.module";
import {LayoutModule} from "./layout/layout.module";

// Top level component
import {ShellComponent} from "./layout/shell.component";

//enableProdMode(); //Uncomment for production

// *** USE JQUERY TO BOOTSTRAP APPLICATION ONCE KEYCLOAK IS AUTHORIZED ***
export class Application {

	public static Define({modules, states, defaultState}) {
		return {
			imports: [
				BrowserModule,
				FormsModule,
				HttpModule,
				TreeModule,
				NgbModule.forRoot(),
				ToastModule.forRoot(<ToastOptions>{animate: 'flyRight', positionClass: 'toast-bottom-right'}),
				UIRouterModule.forRoot(<RootModule>{ states: states.concat({name: 'app', url: '/app', component: ShellComponent}), useHash: true, otherwise: defaultState }),

				CommonModule,
				DialogsModule,

				LayoutModule,
			].concat(modules),
			providers: [
				{
					provide: Http,
					useFactory: (backend: XHRBackend, defaultOptions: RequestOptions) => new AuthHttpService(backend, defaultOptions),
					deps: [XHRBackend, RequestOptions]
				}
			],
			bootstrap: [ UIView ]
		};
	}

	public static Run(ApplicationModule: any) {
		$('document').ready(function () {

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

					Auth.factory().setOnAuthSuccess(() => {
						// manually bootstrap angular
						platformBrowserDynamic().bootstrapModule(ApplicationModule)
							.then((success: any) => console.log('App bootstrapped'))
							.catch((err: any) => console.error(err));
					});

					Auth.factory().setOnAuthError(() => {
						console.log('Failed to start app as not authenticated, check the well known auth configuration.')
					});

					Auth.factory().init();
				});
		});
	}
}