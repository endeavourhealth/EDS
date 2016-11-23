import {NgModule} from "@angular/core";
import {SecurityService} from "./security.service";
import {AuthHttpService} from "./authHttp.service";

@NgModule({
	providers : [
		AuthHttpService,
		SecurityService
	]
})
export class SecurityModule {}