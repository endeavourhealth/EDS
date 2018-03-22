import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {Service} from "./models/Service";
import {Organisation} from "../organisations/models/Organisation";
import {System} from "../system/models/System";
import {BaseHttp2Service} from "eds-common-js";
import {Http, URLSearchParams} from "@angular/http";

@Injectable()
export class ServiceService extends BaseHttp2Service {

	//common filter options used by the Service list and Transform Errors page
	showFilters: boolean;
	serviceNameFilter: string;
	servicePublisherConfigFilter: string;
	serviceHasErrorsFilter: boolean;
	allPublisherConfigNames: string[];

	constructor(http : Http) {
		super (http);
	}

	getAll(): Observable<Service[]> {
		return this.httpGet('api/service');
	}

	get(uuid : string) : Observable<Service> {
		let params = new URLSearchParams();
		params.set('uuid',uuid);
		return this.httpGet('api/service', { search : params });
	}

	save(service : Service) : Observable<any> {
		return this.httpPost('api/service', service);
	}

	delete(uuid : string) : Observable<any> {
		let params = new URLSearchParams();
		params.set('uuid',uuid);
		return this.httpDelete('api/service/', { search : params });
	}

	deleteData(serviceUuid : string, systemUuid : string) : Observable<any> {
		let params = new URLSearchParams();
		params.set('serviceId', serviceUuid);
		params.set('systemId', systemUuid);
		return this.httpDelete('api/service/data', { search : params });
	}

	search(searchData : string) : Observable<Service[]> {
		let params = new URLSearchParams();
		params.set('searchData',searchData);
		return this.httpGet('api/service', { search : params });
	}

	getServiceOrganisations(uuid : string) : Observable<Organisation[]> {
		let params = new URLSearchParams();
		params.set('uuid',uuid);
		return this.httpGet('api/service/organisations', { search : params });
	}

	getSystemsForService(serviceId : string) : Observable<System[]> {
		let params = new URLSearchParams();
		params.set('serviceId',serviceId);
		return this.httpGet('api/service/systemsForService', { search : params });
	}


	toggleFiltering() {
		var vm = this;
		vm.showFilters = !vm.showFilters;
	}

	applyFiltering(services: Service[]) : Service[] {
		var vm = this;

		//if we've not loaded our services yet, just return out
		if (!services) {
			return;
		}

		var filteredServices = [];

		//work out if the name/ID search text is valid regex and force it to lower case if so
		var validNameFilterRegex;
		if (vm.serviceNameFilter) {
			try {
				new RegExp(vm.serviceNameFilter);
				validNameFilterRegex = vm.serviceNameFilter.toLowerCase();
			} catch (e) {
				//do nothing
			}
		}

		var arrayLength = services.length;
		for (var i = 0; i < arrayLength; i++) {
			var service = services[i];

			//only apply the filters if we're showing the panel
			if (vm.showFilters) {

				if (vm.serviceHasErrorsFilter) {
					if (!service.hasInboundError) {
						continue;
					}
				}

				if (vm.servicePublisherConfigFilter) {
					var publisherConfigName = service.publisherConfigName;
					if (!publisherConfigName || publisherConfigName != vm.servicePublisherConfigFilter) {
						continue;
					}
				}

				//only apply the name filter if it's valid regex
				if (validNameFilterRegex) {
					var name = service.name;
					var id = service.localIdentifier;
					var notes = service.notes;
					if ((!name || !name.toLowerCase().match(validNameFilterRegex))
						&& (!id || !id.toLowerCase().match(validNameFilterRegex))
						&& (!notes || !notes.toLowerCase().match(validNameFilterRegex))) {
						continue;
					}
				}
			}

			filteredServices.push(service);
		}

		return filteredServices;

	}
}

