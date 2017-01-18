import {TransformErrorSummary} from "./../exchangeAudit/TransformErrorSummary";
import {TransformErrorDetail} from "./../exchangeAudit/TransformErrorDetail";
import {LoggerService} from "../common/logger.service";
import {StateService} from "ui-router-ng2";
import {Component} from "@angular/core";
import {ExchangeAuditService} from "../exchangeAudit/exchangeAudit.service";

@Component({
	template : require('./transformErrors.html')
})
export class TransformErrorsComponent {
	transformErrorSummaries:TransformErrorSummary[];
	selectedSummary:TransformErrorSummary;
	selectedExchangeIndex:number;

	selectExchangeErrorDetail:TransformErrorDetail;


	constructor(protected exchangeAuditService:ExchangeAuditService,
				protected logger:LoggerService,
				protected $state : StateService) {

		this.refreshSummaries();
	}
	/*constructor(protected transformErrorService:TransformErrorsService,
				protected logger:LoggerService,
				protected $state : StateService) {

		this.refreshSummaries();
	}*/

	refreshSummaries() {
		var vm = this;
		vm.transformErrorSummaries = null;
		vm.selectedSummary = null;

		vm.exchangeAuditService.getTransformErrorSummaries()
			.subscribe(
				(data) => {
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
		var serviceId = summary.serviceId;
		var systemId = summary.systemId;

		vm.exchangeAuditService.rerunFirstExchangeInError(serviceId, systemId)
			.subscribe(
				(replacement) => vm.refreshSummariesKeepingSelection(summary, replacement)
			);
	}

	rerunAll(summary:TransformErrorSummary) {
		var vm = this;
		var serviceId = summary.serviceId;
		var systemId = summary.systemId;

		vm.exchangeAuditService.rerunAllExchangesInError(serviceId, systemId)
			.subscribe(
				() => vm.refreshSummariesKeepingSelection(summary, null)
			);
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
		var exchangeId = vm.selectedSummary.exchangeIds[vm.selectedExchangeIndex - 1];

		vm.exchangeAuditService.getTransformErrorDetail(serviceId, systemId, exchangeId, true, true)
			.subscribe(
				(data) => {
					vm.selectExchangeErrorDetail = data[0];
				},
				(error) => vm.logger.error('Failed to retrieve transform audit', error, 'View Exchanges')
			);
	}

	actionItem(uuid : string, action : string) {
		this.$state.go('app.resources', {itemUuid: uuid, itemAction: action});
	}
}
