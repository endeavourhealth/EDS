import {NgModule} from "@angular/core";
import {LoggerService} from "./logger.service";
import {Logger} from "angular2-logger/core";
import {ModuleStateService} from "./moduleState.service";

@NgModule({
	providers : [
		Logger,
		LoggerService,
		ModuleStateService
	]
})
export class CommonModule {}