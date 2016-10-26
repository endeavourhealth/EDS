require('zone.js');
import 'reflect-metadata';

import {NgModule, OnInit, AfterViewInit} from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import {HttpModule, RequestOptions, XHRBackend, Http} from '@angular/http';
import { Logger } from "angular2-logger/core";
import {ToastModule} from "ng2-toastr";

import './app.module';
import {upgradeAdapter} from "./upgradeAdapter";

import {Auth} from "./appstartup/appstartup.auth";
import {AuthHttp} from "./core/httpInterceptor";
import {WellKnownConfig} from "./appstartup/appstartup.module";
import {AuthConfig} from "./models/wellknown/AuthConfig";

import {AuditComponent} from "./audit/audit.component";
import {ConsentComponent} from "./consent/consent.component";
import {MedicalRecordComponent} from "./medicalRecord/medicalRecord.component";
import {TopnavComponent} from "./layout/topnav.component";

import {MedicalRecordService} from "./medicalRecord/medicalRecord.service";
import {ConfigService} from "./config/config.service";
import {ConsentService} from "./consent/consent.service";
import {AuditService} from "./audit/audit.service";
import {AdminService} from "./core/admin.service";
import {SecurityService} from "./core/security.service";
import {EdsLoggerService} from "./blocks/logger.service";

@NgModule({
	imports: [
		BrowserModule,
		FormsModule,
		HttpModule,
		ToastModule,
	],
	declarations: [
		AuditComponent,
		ConsentComponent,
		MedicalRecordComponent,
		TopnavComponent,
	],
	providers: [
		Logger,

		EdsLoggerService,
		MedicalRecordService,
		AuditService,
		ConsentService,
		ConfigService,
		AdminService,
		SecurityService,
		{
			provide: Http,
			useFactory: (backend: XHRBackend, defaultOptions: RequestOptions) => new AuthHttp(backend, defaultOptions),
			deps: [XHRBackend, RequestOptions]
		}
	]
})

export class AppModule implements OnInit, AfterViewInit {
	constructor(private $http:Http, private log : EdsLoggerService) {}

	ngOnInit(): void {
		this.log.info("Initialized");
	}

	ngAfterViewInit() {

		var $injector = angular.injector(['ng']);
		var promise:ng.IQService = $injector.get('$q') as ng.IQService;
		var wellKnownConfig:WellKnownConfig = WellKnownConfig.factory();

		var defer = promise.defer();

		// try to read the auth configuration from local storage, if not found, get it from the public API and store it
		var path: string = 'eds.config.auth';
		var text: string = localStorage.getItem(path);
		if (text === null || typeof text === "undefined" || text === "undefined") {
			// use jQuery to avoid angular http interceptors
			jQuery.getJSON("/public/wellknown/authconfig", (data:any, textStatus:string, jqXHR:any) => {
				var authConfig = data as AuthConfig;
				localStorage.setItem(path, JSON.stringify(authConfig));
				defer.resolve(authConfig);
			});
		}
		else {
			defer.resolve(<AuthConfig>JSON.parse(text));
		}

		defer.promise.then((authConfig:AuthConfig) => {
			// set the config
			wellKnownConfig.setAuthConfig(authConfig);

			Auth.factory().setOnAuthSuccess(()=> {
				// manually bootstrap angular
				upgradeAdapter.bootstrap(document.body, ['app'], {strictDi: true});
			});

			Auth.factory().setOnAuthError(()=> {
				console.log('Failed to start app as not authenticated, check the well known auth configuration.')
			});

			Auth.factory().init();
		});
	}

}

