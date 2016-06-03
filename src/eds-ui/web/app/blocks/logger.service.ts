/// <reference path="../../typings/tsd.d.ts" />

module app.blocks {
	'use strict';

	export interface ILoggerService {
		info: (message:string, data?:{}, title?:string) => void;
		error: (message:string, data?:{}, title?:string) => void;
		success: (message:string, data?:{}, title?:string) => void;
		warning: (message:string, data?:{}, title?:string) => void;
		log: (...args:any[]) => void;
	}

	export class LoggerService implements ILoggerService {
		static $inject:Array<string> = ['$log'];

		constructor(private $log:ng.ILogService) {
			toastr.options.timeOut = 4000;
			toastr.options.positionClass = 'toast-bottom-right';
		}

		// straight to console; bypass toastr
		log(...args:any[]) {
			this.$log.log(args);
		}

		error(message:string, data?:{}, title?:string) {
			toastr.error(message, title);
			this.$log.error('Error: ' + message, '\nSummary:', title, '\nDetails:', data);
		}

		info(message:string, data?:{}, title?:string) {
			toastr.info(message, title);
			this.$log.info('Info: ' + message, '\nSummary:', title, '\nDetails:', data);
		}

		success(message:string, data?:{}, title?:string) {
			toastr.success(message, title);
			this.$log.info('Success: ' + message, '\nSummary:', title, '\nDetails:', data);
		}

		warning(message:string, data?:{}, title?:string) {
			toastr.warning(message, title);
			this.$log.warn('Warning: ' + message, '\nSummary:', title, '\nDetails:', data);
		}
	}

	angular
		.module('app.blocks')
		.service('LoggerService', LoggerService);
}