/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../models/MenuOption.ts" />
/// <reference path="../models/Role.ts" />
/// <reference path="../models/User.ts" />
/// <reference path="../models/UserInRole.ts" />

module app.core {
	'use strict';

	export interface IModuleStateService {
		setState(moduleName : string, state : any) : void;
		getState(moduleName : string) : any;
	}

	export class ModuleStateService implements IModuleStateService {
		states : any = {};

		setState(moduleName : string, state : any) {
			this.states[moduleName] = state;
		}

		getState(moduleName : string) : any {
			return this.states[moduleName];
		}

	}

	angular
		.module('app.core')
		.service('ModuleStateService', ModuleStateService);
}