/// <reference path="../typings/index.d.ts"/>
require('zone.js');
import 'reflect-metadata';

import {NgModule} from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import {HttpModule, RequestOptions, XHRBackend, Http} from '@angular/http';
import { Logger } from "angular2-logger/core";
import {ToastModule, ToastOptions} from "ng2-toastr";
import {UIRouterModule, RootModule} from 'ui-router-ng2';

import {states} from './app.states';
import {AuthHttp} from "./core/httpInterceptor";

import {TopnavComponent} from "./layout/topnav.component";
import {ShellComponent} from "./layout/shell.component";
import {SidebarComponent} from "./layout/sidebar.component";
import {ConsentComponent} from "./consent/consent.component";
import {AuditComponent} from "./audit/audit.component";
import {MedicalRecordComponent} from "./medicalRecord/medicalRecord.component";

import {EdsLoggerService} from "./blocks/logger.service";
import {AdminService} from "./core/admin.service";
import {SecurityService} from "./core/security.service";
import {MedicalRecordService} from "./medicalRecord/medicalRecord.service";

@NgModule({
	imports: [
		BrowserModule,
		FormsModule,
		HttpModule,
		ToastModule.forRoot(<ToastOptions>{animate: 'flyRight', positionClass: 'toast-bottom-right'}),
		UIRouterModule.forRoot(<RootModule>{ states: states, useHash: true, otherwise:'/medicalRecord' })
	],
	declarations: [
		ShellComponent,
		SidebarComponent,
		TopnavComponent,
		MedicalRecordComponent,
		AuditComponent,
		ConsentComponent
	],
	providers: [
		Logger,
		EdsLoggerService,
		SecurityService,
		AdminService,
		MedicalRecordService,
		{
			provide: Http,
			useFactory: (backend: XHRBackend, defaultOptions: RequestOptions) => new AuthHttp(backend, defaultOptions),
			deps: [XHRBackend, RequestOptions]
		}
	],
	bootstrap:    [ ShellComponent ]
})

export class AppModule {}
