require('zone.js');
import 'reflect-metadata';

import {NgModule, OnInit} from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import {HttpModule, RequestOptions, XHRBackend, Http} from '@angular/http';
import { Logger } from "angular2-logger/core";
import {ToastModule} from "ng2-toastr";

import {AuthHttp} from "./core/httpInterceptor";

import {AuditComponent} from "./audit/audit.component";
import {ConsentComponent} from "./consent/consent.component";
import {MedicalRecordComponent} from "./medicalRecord/medicalRecord.component";

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
		MedicalRecordComponent
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

export class AppModule implements OnInit {
	constructor(private log : EdsLoggerService) {}

	ngOnInit(): void {
		this.log.info("Initialized");
	}

}

import './app.module';