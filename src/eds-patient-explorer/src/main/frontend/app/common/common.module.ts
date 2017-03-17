import {NgModule} from "@angular/core";
import {LoggerService} from "./logger.service";
import {Logger} from "angular2-logger/core";
import {ModuleStateService} from "./moduleState.service";
import {EdsErrorHandler} from "./errorHandler.service";
import {LoadingIndicatorComponent} from "./loadingIndicator.component";

@NgModule({
	providers : [
		Logger,
		LoggerService,
		ModuleStateService,
		EdsErrorHandler,
	],
	declarations : [
		LoadingIndicatorComponent
	],
	exports : [
		LoadingIndicatorComponent
	]
})
export class CommonModule {}