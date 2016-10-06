import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;

export class BaseDialogController {
	resultData : any;

	constructor(protected $uibModalInstance : IModalServiceInstance) {
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
