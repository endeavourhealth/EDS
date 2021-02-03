import {TransformErrorSummary} from "./../exchangeAudit/TransformErrorSummary";
import {TransformErrorDetail} from "./../exchangeAudit/TransformErrorDetail";
import {linq, LoggerService} from "eds-common-js";
import {StateService} from "ui-router-ng2";
import {Component} from "@angular/core";
import {ExchangeAuditService} from "../exchangeAudit/exchangeAudit.service";
import {Subscription} from "rxjs/Subscription";
import {ServiceService} from "../services/service.service";
import {Service} from "../services/models/Service";
import {DateTimeFormatter} from "../utility/DateTimeFormatter";

@Component({
	template : require('./transformErrors.html')
})
export class TransformErrorsComponent {

	//SD-338 - need to import the static formatting functions so they can be used by the HTML template
	formatYYYYMMDDHHMMSS = DateTimeFormatter.formatYYYYMMDDHHMMSS;

	transformErrorSummaries: TransformErrorSummary[];
	selectedSummary: TransformErrorSummary;
	selectedExchangeIndex: number;
	tagStrDisplayLimit: number;
	cachedTagStrs: {};

	selectExchangeErrorDetail:TransformErrorDetail;
	busyPostingToExchange: Subscription;

	//filtering
	filteredErrorSummaries: TransformErrorSummary[];

	constructor(private serviceService : ServiceService,
				protected exchangeAuditService:ExchangeAuditService,
				protected logger:LoggerService,
				protected $state : StateService) {


	}

	ngOnInit() {
		var vm = this;
		vm.tagStrDisplayLimit = 10;
		vm.cachedTagStrs = {}
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

	/*rerunFirst(summary:TransformErrorSummary) {
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
	}*/

	rerunAll(summary:TransformErrorSummary) {
		var vm = this;
		var serviceId = summary.service.uuid;
		var systemId = summary.systemId;

		this.busyPostingToExchange = vm.exchangeAuditService.rerunAllExchangesInError(serviceId, systemId).subscribe(
			(result) => {
				vm.logger.success('Successfully posted to exchange', 'Post to Exchange');
				//vm.refreshSummariesKeepingSelection(summary, null);
				this.busyPostingToExchange = null;
			},
			(error) => {
				vm.logger.error('Failed to post to exchange', error, 'Post to Exchange')
				//clear down to say we're not busy
				this.busyPostingToExchange = null;
			}
		)
	}

	/*private refreshSummariesKeepingSelection(original:TransformErrorSummary, replacement:TransformErrorSummary) {
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
	}*/

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
		var hmErrorsByServiceId = {};

		var arrayLength = vm.transformErrorSummaries.length;
		for (var i = 0; i < arrayLength; i++) {
			var transformErrorSummary = vm.transformErrorSummaries[i];
			var service = transformErrorSummary.service;
			if (services.indexOf(service) == -1) {
				services.push(service);
			}

			var errorsForService = hmErrorsByServiceId[transformErrorSummary.service.uuid];
			if (!errorsForService) {
				errorsForService = [];
			}
			errorsForService.push(transformErrorSummary);
			hmErrorsByServiceId[transformErrorSummary.service.uuid] = errorsForService;
		}

		var filteredServices = vm.serviceService.applyFiltering(services, true);

		for (var i=0; i<filteredServices.length; i++) {
			var service = filteredServices[i];
			var errorsForService = hmErrorsByServiceId[service.uuid];

			for (var j=0; j<errorsForService.length; j++) {
				var errorForService = errorsForService[j];
				vm.filteredErrorSummaries.push(errorForService);
			}
		}

	}

	clearFilters() {
		var vm = this;
		vm.serviceService.clearFilters();

		//call the filtered changed method to remove the applied filtering
		vm.applyFiltering();
	}



	checkAll() {
		var vm = this;

		//if we've not loaded our services yet, just return out
		if (!vm.transformErrorSummaries) {
			return;
		}

		var arrayLength = vm.transformErrorSummaries.length;
		for (var i = 0; i < arrayLength; i++) {
			var transformErrorSummary = vm.transformErrorSummaries[i];
			transformErrorSummary.checked = true;
		}
	}

	getCheckedCount(): number {
		var vm = this;

		//only count filtered ones
		if (!vm.filteredErrorSummaries) {
			return 0;
		}

		var ret = 0;

		var arrayLength = vm.filteredErrorSummaries.length;
		for (var i = 0; i < arrayLength; i++) {
			var transformErrorSummary = vm.filteredErrorSummaries[i];
			if (transformErrorSummary.checked) {
				ret ++;
			}
		}

		return ret;
	}

	requeueChecked() {
		var vm = this;

		var filteredAndChecked = [];

		var arrayLength = vm.filteredErrorSummaries.length;
		for (var i = 0; i < arrayLength; i++) {
			var transformErrorSummary = vm.filteredErrorSummaries[i];
			if (transformErrorSummary.checked) {
				filteredAndChecked.push(transformErrorSummary);
			}
		}

		if (filteredAndChecked.length == 0) {
			vm.logger.error('No services checked', null, 'Post to Exchange')
			return;
		}

		this.busyPostingToExchange = vm.exchangeAuditService.rerunAllExchangesInErrorForServices(filteredAndChecked).subscribe(
			(result) => {
				vm.logger.success('Successfully posted to exchange', 'Post to Exchange');
				this.busyPostingToExchange = null;
			},
			(error) => {
				vm.logger.error('Failed to post to exchange', error, 'Post to Exchange')
				//clear down to say we're not busy
				this.busyPostingToExchange = null;
			}
		)
	}

	edit(item : Service) {
		this.$state.go('app.serviceEdit', {itemUuid: item.uuid, itemAction: 'edit'});
	}

	getTagStrPrefix(service: Service) : string {
		var vm = this;
		var str = vm.getTagStr(service);
		if (str
			&& str.length > vm.tagStrDisplayLimit) {
			return str.substr(0, vm.tagStrDisplayLimit) + '...';

		} else {
			return str;
		}
	}

	getTagStr(service: Service) : string {
		var vm = this;

		if (!vm.cachedTagStrs[service.uuid]) {
			var str = vm.serviceService.createTagStr(service);
			vm.cachedTagStrs[service.uuid] = str;
		}
		return vm.cachedTagStrs[service.uuid]
	}
}
