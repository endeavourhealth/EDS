/// <reference path="../../typings/tsd.d.ts" />

module app.core {
	import Report = app.models.Report;
	import RequestParameters = app.models.RequestParameters;
	import UuidNameKVP = app.models.UuidNameKVP;
	import ReportSchedule = app.models.ReportSchedule;
	import QueryResult = app.models.QueryResult;
	import ReportResult = app.models.ReportResult;
	'use strict';

	export interface IReportService {
		saveReport(report : Report):ng.IPromise<Report>;
		getReport(uuid : string):ng.IPromise<Report>;
		deleteReport(uuid : string):ng.IPromise<any>;
		scheduleReport(requestParameters : RequestParameters):ng.IPromise<any>;
		getContentNamesForReportLibraryItem(uuid : string):ng.IPromise<{contents : UuidNameKVP[]}>;
		getReportSchedules(uuid : string, count : number):ng.IPromise<ReportSchedule[]>;
		getScheduleResults(uuid : string):ng.IPromise<ReportResult>;
	}

	export class ReportService extends BaseHttpService implements IReportService {
		saveReport(report: Report):ng.IPromise<Report> {
			return this.httpPost('api/report/saveReport', report);
		}

		deleteReport(uuid: string):ng.IPromise<any> {
			var request = {
				'uuid': uuid
			};
			return this.httpPost('api/report/deleteReport', request);
		}

		getReport(uuid : string):ng.IPromise<Report> {
			var request = {
				params: {
					'uuid': uuid
				}
			};
			return this.httpGet('api/report/getReport', request);
		}

		getContentNamesForReportLibraryItem(uuid : string):ng.IPromise<{contents : UuidNameKVP[]}> {
			var request = {
				params: {
					'uuid': uuid
				}
			};
			return this.httpGet('api/library/getContentNamesForReportLibraryItem', request);
		}

		scheduleReport(requestParameters : RequestParameters):ng.IPromise<any> {
			return this.httpPost('api/report/scheduleReport', requestParameters);
		}

		getReportSchedules(uuid : string, count : number):ng.IPromise<ReportSchedule[]> {
			var request = {
				params: {
					'uuid': uuid,
					'count': count
				}
			};
			return this.httpGet('api/report/getReportSchedules', request);
		}

		getScheduleResults(uuid : string):ng.IPromise<ReportResult> {
			var request = {
				params: {
					'uuid': uuid
				}
			};
			return this.httpGet('api/report/getScheduleResults', request);
		}
	}

	angular
		.module('app.core')
		.service('ReportService', ReportService);
}