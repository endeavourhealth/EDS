import {LogEntryDialogController} from "./logEntryDialog.controller";
import {LoggingController} from "./logging.controller";
import {LoggingRoute} from "./logging.route";

angular.module('app.logging', [])
	.controller('LogEntryDialogController', LogEntryDialogController)
	.controller('LoggingController', LoggingController)
	.config(LoggingRoute);