import {Component} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {StateService, Transition} from "ui-router-ng2";
import {Service} from "../services/models/Service";
import {Organisation} from "../organisations/models/Organisation";
import {System} from "../system/models/System";
import {TechnicalInterface} from "../system/models/TechnicalInterface";
/*import {Endpoint} from "./models/Endpoint";*/
import {AdminService} from "../administration/admin.service";
/*import {ServiceService} from "./service.service";*/
import {OrganisationPickerDialog} from "../organisations/organisationPicker.dialog";
import {MessageBoxDialog} from "../dialogs/messageBox/messageBox.dialog";
import {LoggerService} from "../common/logger.service";
import {SystemService} from "../system/system.service";

import {ExchangeAuditTransformErrorSummary} from "./ExchangeAuditTransformErrorSummary";
import {ExchangeAuditTransformErrorDetail} from "./ExchangeAuditTransformErrorDetail";
import {ExchangeAuditService} from "./exchangeAudit.service";
import {Exchange} from "./Exchange";
import {ServiceService} from "../services/service.service";


@Component({
	template : require('./exchangeAudit.html')
})
export class ExchangeAuditComponent {


	service: Service;
	exchangesToShow: number;

	exchanges: Exchange[];
	selectedExchange: Exchange;




	transformErrorSummaries:ExchangeAuditTransformErrorSummary[];
	selectedSummary:ExchangeAuditTransformErrorSummary;
	selectedExchangeIndex:number;
	selectExchangeErrorDetail:ExchangeAuditTransformErrorDetail;


	constructor(private $modal : NgbModal,
				private $window : StateService,
				private log : LoggerService,
				private serviceService : ServiceService,
				private exchangeAuditService : ExchangeAuditService,
				private transition : Transition) {

		this.service = new Service();
		this.exchangesToShow = 100;

		var uuid = transition.params()['serviceUuid'];

		serviceService.get(uuid)
			.subscribe(
				(result) => {
					this.service = result;
					this.refreshExchanges();
				},
				(error) => log.error('Failed to retrieve service', error, 'Refresh Service')
			)

	}


	close() {
		this.$window.go(this.transition.from());
	}

	refreshExchanges() {
		var vm = this;
		var serviceId = vm.service.uuid;

		vm.exchangeAuditService.getExchangeList(serviceId, vm.exchangesToShow).subscribe(
			(result) => {
				vm.exchanges = result;
				if (result.length == 0) {
					vm.log.success('No exchanges found');
				}
			},
			(error) => vm.log.error('Failed to retrieve exchanges', error, 'View Exchanges')
		)
	}



	createMissingData() {
		var vm = this;
		vm.exchangeAuditService.createMissingData().subscribe(
			(result) => vm.log.success('Data created', null, 'Create Missing Data'),
			(error) => vm.log.error('Failed to create data', error, 'Create Missing Data')
		)
	}

	selectExchange(exchange : Exchange) {

		var vm = this;
		vm.selectedExchange = exchange;
		vm.loadExchangeEventsIfRequired(exchange);
	}

	private loadExchangeEventsIfRequired(exchange: Exchange) {

		//if they're already loaded, just return out
		if (exchange.events) {
			return;
		}

		var vm = this;
		vm.exchangeAuditService.getExchangeEvents(exchange.exchangeId).subscribe(
			(result) => {
				exchange.events = result;
			},
			(error) => vm.log.error('Failed to retrieve exchange events', error, 'View Exchanges')
		)
	}

	getSelectedExchangeHeaderKeys(): string[] {

		var vm = this;
		if (!vm.selectedExchange) {
			return null;
		}

		return Object.keys(vm.selectedExchange.headers);
	}

	postToExchange(exchangeName: string) {
		var vm = this;
		var exchangeId = vm.selectedExchange.exchangeId;

		vm.exchangeAuditService.postToExchange(exchangeId, exchangeName).subscribe(
			(result) => vm.log.success('Successfully posted to ' + exchangeName + ' exchange', 'Pust to Exchange'),
			(error) => vm.log.error('Failed to post to ' + exchangeName + ' exchange', error, 'Pust to Exchange')
		)
	}

	/*refreshSummaries() {
		var vm = this;
		vm.transformErrorSummaries = null;
		vm.selectedSummary = null;

		vm.transformErrorService.getTransformErrorSummaries()
			.subscribe(
				(data) => {
					vm.transformErrorSummaries = data;
					if (data.length == 0) {
						vm.logger.success('No transform errors found');
					}
				});
	}

	selectSummary(summary:ExchangeAuditTransformErrorSummary) {

		var vm = this;
		vm.selectedSummary = summary;
		vm.selectedExchangeIndex = 1;

		vm.loadExchange();
	}

	rerunFirst(summary:ExchangeAuditTransformErrorSummary) {
		var vm = this;
		var serviceId = vm.selectedSummary.serviceId;
		var systemId = vm.selectedSummary.systemId;

		vm.transformErrorService.rerunFirst(serviceId, systemId)
			.subscribe(
				(replacement) => vm.refreshSummariesKeepingSelection(summary, replacement)
			);
	}

	rerunAll(summary:ExchangeAuditTransformErrorSummary) {
		var vm = this;
		var serviceId = vm.selectedSummary.serviceId;
		var systemId = vm.selectedSummary.systemId;

		vm.transformErrorService.rerunAll(serviceId, systemId)
			.subscribe(
				() => vm.refreshSummariesKeepingSelection(summary, null)
			);
	}

	private refreshSummariesKeepingSelection(original:ExchangeAuditTransformErrorSummary, replacement:ExchangeAuditTransformErrorSummary) {
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

		vm.transformErrorService.getTransformErrorDetail(serviceId, systemId, exchangeId)
			.subscribe(
				(data) => vm.selectExchangeErrorDetail = data
			);
	}

	actionItem(uuid : string, action : string) {
		this.$state.go('app.resources', {itemUuid: uuid, itemAction: action});
	}*/
}
