import IModalService = angular.ui.bootstrap.IModalService;
import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;
import IModalSettings = angular.ui.bootstrap.IModalSettings;

import {BaseDialogController} from "../dialogs/baseDialog.controller";
import {LoggingEvent} from "../models/LoggingEvent";
import {ILoggerService} from "../blocks/logger.service";

export class LogEntryDialogController extends BaseDialogController {

	public static open($modal : IModalService, logEntry : LoggingEvent, stackTrace : string) : IModalServiceInstance {
		var options : IModalSettings = {
			template:require('./logEntryDialog.html'),
			controller:'LogEntryDialogController',
			controllerAs:'ctrl',
			backdrop:'static',
			size:'lg',
			resolve: {
				logEntry : () => logEntry,
				stackTrace : () => stackTrace
			}
		};

		var dialog = $modal.open(options);
		return dialog;
	}

	static $inject = ['$uibModalInstance', 'LoggerService', 'logEntry', 'stackTrace'];

	logEntry : LoggingEvent;
	stackTrace : string;

	constructor(protected $uibModalInstance : IModalServiceInstance,
							private log : ILoggerService,
							private entry : LoggingEvent,
							private trace : string) {
		super($uibModalInstance);

		this.logEntry = entry;
		this.stackTrace = trace;
	}

	getLevelIcon(level : string) {
		switch (level) {
			case "TRACE" :
				return "fa fa-fw fa-search text-success";
			case "DEBUG":
				return "fa fa-fw fa-bug text-primary";
			case "INFO":
				return "fa fa-fw fa-info text-info";
			case "WARN" :
				return "fa fa-fw fa-exclamation-circle text-warning";
			case "ERROR":
				return "fa fa-fw fa-ban text-danger";
			case "FATAL":
				return "fa fa-fw fa-stop text-danger";
			default:
				return "fa fa-fw fa-space";
		}
	}
}
