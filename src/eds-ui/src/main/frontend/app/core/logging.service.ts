/// <reference path="../../typings/index.d.ts" />

module app.core {
	import LoggingEvent = app.models.LoggingEvent;
	'use strict';

	export interface ILoggingService {
		getLoggingEvents(serviceId : string, level : string, page : number):ng.IPromise<LoggingEvent[]>;
		getStackTrace(eventId : string) : ng.IPromise<string>;
	}

	export class LoggingService extends BaseHttpService implements ILoggingService {

		getLoggingEvents(serviceId : string, level : string, page : number):ng.IPromise<LoggingEvent[]> {
			var request = {
				params: {
					'serviceId': serviceId,
					'level': level,
					'page': page
				}
			};

			return this.httpGet('api/logging/getLoggingEvents', request);
		}

		getStackTrace(eventId : string):ng.IPromise<string> {
			var request = {
				params: {
					'eventId': eventId
				}
			};

			return this.httpGet('api/logging/getStackTrace', request);
		}
	}

	angular
		.module('app.core')
		.service('LoggingService', LoggingService);
}