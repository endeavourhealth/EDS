import {TransformErrorSummary} from "./../exchangeAudit/TransformErrorSummary";
import {TransformErrorDetail} from "./../exchangeAudit/TransformErrorDetail";
import {linq, LoggerService} from "eds-common-js";
import {StateService} from "ui-router-ng2";
import {Component} from "@angular/core";
import {ExchangeAuditService} from "../exchangeAudit/exchangeAudit.service";
import {Subscription} from "rxjs/Subscription";
import {ServiceService} from "../services/service.service";

@Component({
	template : require('./transformErrors.html')
})
export class TransformErrorsComponent {
	transformErrorSummaries:TransformErrorSummary[];
	selectedSummary:TransformErrorSummary;
	selectedExchangeIndex:number;

	selectExchangeErrorDetail:TransformErrorDetail;
	busyPostingToExchange: Subscription;

	//filtering
	filteredErrorSummaries: TransformErrorSummary[];
	allPublisherConfigNames: string[];


	constructor(private serviceService : ServiceService,
				protected exchangeAuditService:ExchangeAuditService,
				protected logger:LoggerService,
				protected $state : StateService) {


	}

	ngOnInit() {
		this.refreshSummaries();
	}

	refreshSummaries() {
		var vm = this;
		vm.transformErrorSummaries = null;
		vm.selectedSummary = null;

		vm.exchangeAuditService.getTransformErrorSummaries()
			.subscribe(
				(result) => {
					vm.transformErrorSummaries = linq(result).OrderBy(s => s.service.name.toLowerCase()).ToArray();
					/*vm.transformErrorSummaries = linq(result).OrderBy(s => s.service.name).ToArray();*/
					vm.applyFiltering();
					vm.findAllPublisherConfigNames();

					if (result.length == 0) {
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
		var serviceId = summary.service.uuid;
		var systemId = summary.systemId;

		this.busyPostingToExchange = vm.exchangeAuditService.rerunFirstExchangeInError(serviceId, systemId).subscribe(
			(result) => {
				vm.logger.success('Successfully posted to exchange', 'Post to Exchange');
				vm.refreshSummariesKeepingSelection(summary, result);
				this.busyPostingToExchange = null;
			},
			(error) => {
				vm.logger.error('Failed to post to exchange', error, 'Post to Exchange')
				//clear down to say we're not busy
				this.busyPostingToExchange = null;
			}
		)
	}

	rerunAll(summary:TransformErrorSummary) {
		var vm = this;
		var serviceId = summary.service.uuid;
		var systemId = summary.systemId;

		this.busyPostingToExchange = vm.exchangeAuditService.rerunAllExchangesInError(serviceId, systemId).subscribe(
			(result) => {
				vm.logger.success('Successfully posted to exchange', 'Post to Exchange');
				vm.refreshSummariesKeepingSelection(summary, null)
				this.busyPostingToExchange = null;
			},
			(error) => {
				vm.logger.error('Failed to post to exchange', error, 'Post to Exchange')
				//clear down to say we're not busy
				this.busyPostingToExchange = null;
			}
		)
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

		vm.applyFiltering();

		var previouslySelectedSummary = vm.selectedSummary;
		vm.selectedSummary = null;

		//if we had a selected summary, re-select it
		if (previouslySelectedSummary) {
			var serviceId = previouslySelectedSummary.service.uuid;
			var systemId = previouslySelectedSummary.systemId;

			for (var i=0; i<vm.transformErrorSummaries.length; i++) {
				var summary = vm.transformErrorSummaries[i];
				if (summary.service.uuid == serviceId
					&& summary.systemId == systemId) {

					vm.selectedSummary = summary;
					break;
				}
			}
		}
	}

	loadExchange() {

		var vm = this;
		var serviceId = vm.selectedSummary.service.uuid;
		var systemId = vm.selectedSummary.systemId;
		var exchangeId = vm.selectedSummary.exchangeIds[vm.selectedExchangeIndex - 1];

		vm.exchangeAuditService.getInboundTransformAudits(serviceId, systemId, exchangeId, false)
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


	applyFiltering() {
		var vm = this;

		//if we've not loaded our services yet, just return out
		if (!vm.transformErrorSummaries) {
			return;
		}

		vm.filteredErrorSummaries = [];

		//the filtering function works on Service objects, so we need to get the list of services and filter that first
		var services = [];

		var arrayLength = vm.transformErrorSummaries.length;
		for (var i = 0; i < arrayLength; i++) {
			var transformErrorSummary = vm.transformErrorSummaries[i];
			var service = transformErrorSummary.service;
			if (services.indexOf(service) == -1) {
				services.push(service);
			}
		}

		var filteredServices = vm.serviceService.applyFiltering(services);

		for (var i = 0; i < arrayLength; i++) {
			var transformErrorSummary = vm.transformErrorSummaries[i];
			var service = transformErrorSummary.service;
			if (filteredServices.indexOf(service) > -1) {
				vm.filteredErrorSummaries.push(transformErrorSummary);
			}
		}
	}

	toggleFilters() {
		var vm = this;
		vm.serviceService.toggleFiltering();

		//call the filtered changed method to remove the applied filtering
		vm.applyFiltering();
	}

	private findAllPublisherConfigNames() {
		var vm = this;
		vm.allPublisherConfigNames = [];

		var arrayLength = vm.transformErrorSummaries.length;
		for (var i = 0; i < arrayLength; i++) {
			var transformErrorSummary = vm.transformErrorSummaries[i];
			var publisherConfigName = transformErrorSummary.service.publisherConfigName;
			if (publisherConfigName) {
				var index = vm.allPublisherConfigNames.indexOf(publisherConfigName);
				if (index == -1) {
					vm.allPublisherConfigNames.push(publisherConfigName);
				}
			}
		}

		vm.allPublisherConfigNames.sort();
	}

	getNotesPrefix(transformErrorSummary: TransformErrorSummary) : string {

		if (transformErrorSummary.service.notes
			&& transformErrorSummary.service.notes.length > 10) {
			return transformErrorSummary.service.notes.substr(0, 10) + '...';

		} else {
			return transformErrorSummary.service.notes;
		}
	}
}
