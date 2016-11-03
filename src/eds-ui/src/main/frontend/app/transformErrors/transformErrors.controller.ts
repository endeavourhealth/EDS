import IStateService = angular.ui.IStateService;

import {TransformErrorSummary} from "../models/TransformErrorSummary";
import {TransformErrorDetail} from "../models/TransformErrorDetail";
import {ITransformErrorsService} from "../core/transformErrors.service";
import {ILoggerService} from "../blocks/logger.service";

export class TransformErrorsController {
	transformErrorSummaries:TransformErrorSummary[];
	selectedSummary:TransformErrorSummary;
	selectedExchangeIndex:number;

	selectExchangeErrorDetail:TransformErrorDetail;


	static $inject = ['TransformErrorsService', 'LoggerService', '$state'];

	constructor(protected transformErrorService:ITransformErrorsService,
				protected logger:ILoggerService,
				protected $state : IStateService) {

		this.refreshSummaries();
	}

	refreshSummaries() {
		var vm = this;
		vm.transformErrorSummaries = null;
		vm.selectedSummary = null;

		vm.transformErrorService.getTransformErrorSummaries()
			.then(function (data:TransformErrorSummary[]) {
				vm.transformErrorSummaries = data;
				if (data.length == 0) {
					vm.logger.success('No transform errors found');
				}
			});
	}

	selectSummary(summary:TransformErrorSummary) {

		var vm = this;
		vm.selectedSummary = summary;
		vm.selectedExchangeIndex = 1;

		vm.loadExchange();
	}

	rerunFirst(summary:TransformErrorSummary) {
		var vm = this;
		var serviceId = vm.selectedSummary.serviceId;
		var systemId = vm.selectedSummary.systemId;

		vm.transformErrorService.rerunFirst(serviceId, systemId)
			.then(function (replacement:TransformErrorSummary) {

				//after re-running, refresh all our data
				vm.refreshSummariesKeepingSelection(summary, replacement);
			});
	}

	rerunAll(summary:TransformErrorSummary) {
		var vm = this;
		var serviceId = vm.selectedSummary.serviceId;
		var systemId = vm.selectedSummary.systemId;

		vm.transformErrorService.rerunAll(serviceId, systemId)
			.then(function () {

				//after re-running, refresh all our data
				vm.refreshSummariesKeepingSelection(summary, null);
			});
	}

	private refreshSummariesKeepingSelection(original:TransformErrorSummary, replacement:TransformErrorSummary) {
		var vm = this;

		//if we have a replacement, swap it into the array, otherwise remove from the array
		var index = vm.transformErrorSummaries.indexOf(original);
		if (replacement) {
			vm.transformErrorSummaries[index] = replacement;

		} else {
			vm.transformErrorSummaries.splice(index, 1);

		}

		var previouslySelectedSummary = vm.selectedSummary;
		vm.selectedSummary = null;

		//if we had a selected summary, re-select it
		if (previouslySelectedSummary) {
			var serviceId = previouslySelectedSummary.serviceId;
			var systemId = previouslySelectedSummary.systemId;

			for (var i=0; i<vm.transformErrorSummaries.length; i++) {
				var summary = vm.transformErrorSummaries[i];
				if (summary.serviceId == serviceId
					&& summary.systemId == systemId) {

					vm.selectedSummary = summary;
					break;
				}
			}
		}
	}

	loadExchange() {

		var vm = this;
		var serviceId = vm.selectedSummary.serviceId;
		var systemId = vm.selectedSummary.systemId;
		var exchangeId = vm.selectedSummary.exchangeIds[vm.selectedExchangeIndex-1];

		vm.transformErrorService.getTransformErrorDetail(serviceId, systemId, exchangeId)
			.then(function (data:TransformErrorDetail) {
				vm.selectExchangeErrorDetail = data;
			});
	}

	actionItem(uuid : string, action : string) {
		this.$state.go('app.resources', {itemUuid: uuid, itemAction: action});
	}
}
