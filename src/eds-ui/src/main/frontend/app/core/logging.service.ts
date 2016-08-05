/// <reference path="../../typings/index.d.ts" />

module app.core {
	import LoggingEvent = app.models.LoggingEvent;
	'use strict';

	export interface ILoggingService {
		getLoggingEvents(serviceId : string):ng.IPromise<LoggingEvent[]>;
	}

	export class LoggingService extends BaseHttpService implements ILoggingService {

		getLoggingEvents(serviceId : string):ng.IPromise<LoggingEvent[]> {
			var request = {
				params: {
					'serviceId': serviceId
				}
			};

			return this.httpGet('api/logging/getLoggingEvents', request);
		}
	}

	angular
		.module('app.core')
		.service('LoggingService', LoggingService);
}