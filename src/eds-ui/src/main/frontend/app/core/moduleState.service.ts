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
