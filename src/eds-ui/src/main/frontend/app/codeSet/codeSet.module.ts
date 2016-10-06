import {CodeSetController} from './codeSetController';
import {CodeSetRoute} from './codeSet.route';

angular.module('app.codeSet', [])
	.controller('CodeSetController', CodeSetController)
	.config(CodeSetRoute);