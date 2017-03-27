import {ErrorHandler, Injectable, Injector} from "@angular/core";
import {ToastsManager} from "ng2-toastr";

@Injectable()
export class EdsErrorHandler extends ErrorHandler {
	constructor(private injector: Injector) {
		super();
	}

	handleError(err) {
		let toastr = <ToastsManager>this.injector.get(ToastsManager);

		setTimeout(() => {
			if (err.status && err.status == 403)
				toastr.error('Access to this resource is forbidden for the current user', 'Insuffucient permission');
			else
				toastr.error(err, 'Error');
		});

		super.handleError(err);

	}
}