import {Injectable} from "@angular/core";

@Injectable()
export class ModuleStateService {
	states : any = {};

	setState(moduleName : string, state : any) {
		this.states[moduleName] = state;
	}

	getState(moduleName : string) : any {
		return this.states[moduleName];
	}

}
