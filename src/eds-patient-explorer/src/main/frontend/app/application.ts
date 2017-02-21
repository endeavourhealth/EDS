import '../content/css/index.css';
import '../content/less/index.less';

import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import {enableProdMode, ErrorHandler} from '@angular/core';

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
import {EdsErrorHandler} from "./common/errorHandler.service";


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
				},
				{ provide: ErrorHandler, useClass: EdsErrorHandler}
			],
			bootstrap: [ UIView ]
		};
	}

	public static Run(ApplicationModule: any) {
		$('document').ready(function () {

			if (PRODUCTION) {
				console.log('Production mode');
				enableProdMode(); //Uncomment for production
			} else {
				console.log('Development mode');
			}

			var wellKnownConfig: WellKnownConfig = WellKnownConfig.factory();

			var defer = jQuery.Deferred();

			jQuery.getJSON("/public/wellknown/authconfig", (data: any, textStatus: string, jqXHR: any) => {
				var authConfig = data as AuthConfig;
				defer.resolve(authConfig);
			});

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

// Declare here but set via WebPack Define plugin
var PRODUCTION;
