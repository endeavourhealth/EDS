import {EdsLoggerService} from "./logger.service";
import {upgradeAdapter} from "../upgradeAdapter";

angular.module('app.blocks', [])
	.service('EdsLoggerService', EdsLoggerService);
upgradeAdapter.upgradeNg1Provider('EdsLoggerService');
