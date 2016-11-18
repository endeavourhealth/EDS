import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {Input} from "@angular/core";

export class DialogBase {
	@Input()resultData : any;

	constructor(protected $uibModalInstance : NgbActiveModal) {
	}

	ok() {
		this.$uibModalInstance.close(this.resultData);
		console.log('OK Pressed');
	}

	cancel() {
		this.$uibModalInstance.dismiss('cancel');
		console.log('Cancel Pressed');
	}
}
