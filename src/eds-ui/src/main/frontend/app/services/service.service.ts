import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {Service} from "./models/Service";
import {System} from "../system/models/System";
import {BaseHttp2Service} from "eds-common-js";
import {Http, URLSearchParams} from "@angular/http";
import {EdsLibraryItem} from "../edsLibrary/models/EdsLibraryItem";
import {OrganisationType} from "./models/OrganisationType";
import {linq} from "eds-common-js/dist/index";
import {DpaHistory} from "./models/DpaHistory";
import {SubscriberHistory} from "./models/SubscriberHistory";

@Injectable()
export class ServiceService extends BaseHttp2Service {

	//common filter options used by the Service list and Transform Errors page
	serviceNameFilter: string;
	serviceNameSearchIncludeTags: boolean;
	serviceNameSearchSpecificTag: string;
	servicePublisherConfigFilter: string;
	//serviceHasErrorsFilter: boolean;
	serviceStatusFilter: string;
	serviceCcgCodeFilterStr: string;
	serviceCcgCodeFilterRegex: string;
	serviceCcgCodeFilterIsRegex: boolean;
	serviceLastDataFilter: string;
	servicePublisherModeFilter: string;
	serviceHideClosedFilter: boolean;
	sortFilter: string;
	serviceShowDateFilter: boolean;

	tagNameCache: string[];
	refreshingTagNameCache: boolean;
	publisherConfigNameCache: string[];
	refreshingPublisherConfigNameCache: boolean;
	ccgCodeCache: string[];
	refreshingCcgCodeCache: boolean;
	ccgNameCache: {};
	refreshingCcgNameCache: boolean;


	constructor(http : Http) {
		super (http);

		var vm = this;
		vm.clearFilters();
	}


	getAll(): Observable<Service[]> {
		return this.httpGet('api/service');
	}

	get(uuid : string) : Observable<Service> {
		let params = new URLSearchParams();
		params.set('uuid',uuid);
		return this.httpGet('api/service', { search : params });
	}

	getForOdsCode(odsCode : string) : Observable<Service> {
		let params = new URLSearchParams();
		params.set('odsCode', odsCode);
		return this.httpGet('api/service', { search : params });
	}

	save(service : Service) : Observable<any> {
		return this.httpPost('api/service', service);
	}

	validateSave(service : Service) : Observable<string> {
		return this.httpPost('api/service/validateService', service);
	}


	delete(uuid : string) : Observable<string> {
		let params = new URLSearchParams();
		params.set('uuid',uuid);
		return this.httpDelete('api/service/', { search : params });
	}

	deleteData(serviceUuid : string) : Observable<any> {
		let params = new URLSearchParams();
		params.set('serviceId', serviceUuid);
		return this.httpDelete('api/service/data', { search : params });
	}

	search(searchText: string) : Observable<Service[]> {
		let params = new URLSearchParams();
		params.set('searchText', searchText);
		return this.httpGet('api/service', { search : params });
	}

	getSystemsForService(serviceId : string) : Observable<System[]> {
		let params = new URLSearchParams();
		params.set('serviceId', serviceId);
		return this.httpGet('api/service/systemsForService', { search : params });
	}

	getOpenOdsRecord(odsCode : string) : Observable<{}> {
		let params = new URLSearchParams();
		params.set('odsCode', odsCode);
		return this.httpGet('api/service/openOdsRecord', { search : params });
	}

	getDsmDetails(odsCode : string) : Observable<{}> {
		let params = new URLSearchParams();
		params.set('odsCode', odsCode);
		return this.httpGet('api/service/dsmDetails', { search : params });
	}

	/*getServiceProtocols(serviceId: string) : Observable<EdsLibraryItem[]> {
		let params = new URLSearchParams();
		params.set('serviceId', serviceId);
		return this.httpGet('api/service/protocolsForService', { search : params });
	}*/

	getOrganisationTypeList() : Observable<OrganisationType[]> {
		return this.httpGet('api/service/organisationTypeList', {});
	}

	private getTagNames() : Observable<string[]> {
		return this.httpGet('api/service/tagNames', {});
	}

	private getPublisherConfigNames() : Observable<string[]> {
		return this.httpGet('api/service/publisherConfigNames', {});
	}

	private getCcgCodes() : Observable<string[]> {
		return this.httpGet('api/service/ccgCodes', {});
	}

	private getCcgNames(): Observable<{}> {
		return this.httpGet('api/service/ccgNames', {});
	}

	/*private addDays(date: Date, days: number) {
		var result = new Date(date);
		result.setDate(result.getDate() + days);
		return result;
	}*/

	applyFiltering(services: Service[], transformErrorsView: boolean) : Service[] {
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
				validNameFilterRegex = vm.serviceNameFilter.toLowerCase().trim();
			} catch (e) {
				//do nothing
			}
		}

		//validate parent/CCG search text is valid regex
		var validParentFilterRegex;
		if (vm.serviceCcgCodeFilterIsRegex
			&& vm.serviceCcgCodeFilterRegex) {
			try {
				new RegExp(vm.serviceCcgCodeFilterRegex);
				validParentFilterRegex = vm.serviceCcgCodeFilterRegex.toLowerCase();
			} catch (e) {
				//do nothing
			}
		}

		//work out the bounding dates if searching by last data
		var latestCutoffLimit;
		var now = new Date().getTime();
		var dayDuration = 1000 * 60 * 60 * 24;

		if (vm.serviceLastDataFilter) {
			if (vm.serviceLastDataFilter == '1d') {
				latestCutoffLimit = now - dayDuration;

			} else if (vm.serviceLastDataFilter == '2d') {
				latestCutoffLimit = now - (dayDuration * 2);

			} else if (vm.serviceLastDataFilter == '1wk') {
				latestCutoffLimit = now - (dayDuration * 7);

			} else if (vm.serviceLastDataFilter == '2wk') {
				latestCutoffLimit = now - (dayDuration * 14);

			} else if (vm.serviceLastDataFilter == 'older') {
				latestCutoffLimit = now - (dayDuration * 14); //specifically the same as the 2wk filter above

			} else {
				console.log('unknown last data filer ' + vm.serviceLastDataFilter);
			}

			//console.log('filter from ' + minLastData + ' to ' + maxLastData);
		}


		var arrayLength = services.length;
		for (var i = 0; i < arrayLength; i++) {
			var service = services[i];

			if (vm.servicePublisherConfigFilter) {
				var publisherConfigName = service.publisherConfigName;
				if (vm.servicePublisherConfigFilter == 'NoPublisher') {
					if (publisherConfigName) {
						continue;
					}

				} else {
					if (!publisherConfigName || publisherConfigName != vm.servicePublisherConfigFilter) {
						continue;
					}
				}
			}

			//if it's not regex, then just compare strings
			if (!vm.serviceCcgCodeFilterIsRegex
				&& vm.serviceCcgCodeFilterStr) {

				var ccgCode = service.ccgCode;
				if (!ccgCode || ccgCode != vm.serviceCcgCodeFilterStr) {
					continue;
				}
			}

			//if it's not regex, then just compare strings
			if (vm.serviceCcgCodeFilterIsRegex
				&& validParentFilterRegex) {

				var ccgCode = service.ccgCode;
				if (!ccgCode || !ccgCode.toLowerCase().match(validParentFilterRegex)) {
					continue;
				}
			}

			if (vm.serviceStatusFilter
				&& !transformErrorsView) { //only applies to full service list

				var desiredVal;
				if (vm.serviceStatusFilter == 'NoStatus') {
					desiredVal = 0;
				} else if (vm.serviceStatusFilter == 'NoData') {
					desiredVal = 1;
				} else if (vm.serviceStatusFilter == 'OK') {
					desiredVal = 2;
				} else if (vm.serviceStatusFilter == 'Behind') {
					desiredVal = 3;
				} else if (vm.serviceStatusFilter == 'Error') {
					desiredVal = 4;
				} else {
					console.log('Unknown sort mode ' + vm.serviceStatusFilter);
				}
				//console.log('sort mode = ' + vm.serviceStatusFilter + ' val = ' + desiredVal);

				var statusVal = vm.getSortingStatusValue(service);
				//console.log('service ' + service.localIdentifier + ' has status val ' + statusVal + ' looking for ' + vm.serviceStatusFilter);
				if (statusVal != desiredVal) {
					//console.log('skipped');
					continue;
				} else {
					//console.log('include');
				}
			}

			if (vm.servicePublisherModeFilter) {
				//&& !transformErrorsView) { //only applies to main services view
				var include = false;

				if (service.systemStatuses) {
					for (var j=0; j<service.systemStatuses.length; j++) {
						var systemStatus = service.systemStatuses[j];
						if (systemStatus.publisherMode == vm.servicePublisherModeFilter) {
							include = true;
							break;
						}
					}
				}

				if (!include) {
					continue;
				}
			}

			if (latestCutoffLimit) {
				var include = false;

				if (service.systemStatuses) {
					for (var j=0; j<service.systemStatuses.length; j++) {
						var systemStatus = service.systemStatuses[j];
						var cutoff = systemStatus.lastReceivedExtractCutoff;

						if (vm.serviceLastDataFilter == 'older') {
							//if wanting things OLDER than the limit, then we want a smaller number
							if (cutoff < latestCutoffLimit) {
								include = true;
							}

						} else {
							//if wanting things NEWER than the limit, then we want a larger number
							if (cutoff > latestCutoffLimit) {
								include = true;
							}
						}
					}
				}

				if (!include) {
					continue;
				}
			}

			//hide closed filter
			if (vm.serviceHideClosedFilter) {
				if (service.tags
				&& service.tags.hasOwnProperty('Closed')) {
					continue;
				}
			}

			//if filtering by a specific tag, this rules out any service WITHOUT that tag
			if (vm.serviceNameSearchIncludeTags
				&& vm.serviceNameSearchSpecificTag) {

				if (!service.tags
					|| !service.tags.hasOwnProperty(vm.serviceNameSearchSpecificTag)) {
					continue;
				}
			}

			//only apply the name filter if it's valid regex
			if (validNameFilterRegex) {
				var name = service.name;
				var alias = service.alias;
				var id = service.localIdentifier;
				var uuid = service.uuid;

				var include = false;
				if (name && name.toLowerCase().match(validNameFilterRegex)) {
					include = true;

				} if (alias && alias.toLowerCase().match(validNameFilterRegex)) {
					include = true;

				} else if (id && id.toLowerCase().match(validNameFilterRegex)) {
					include = true;

				} else if (uuid && uuid.toLowerCase() == validNameFilterRegex) { //don't compare this as regex
					include = true;

				} else if (service.tags && vm.serviceNameSearchIncludeTags ) {

					var tagNames = Object.keys(service.tags);
					for (var j=0; j<tagNames.length; j++) {
						var tagName = tagNames[j];
						var tagValue = service.tags[tagName];

						//if restricting to a specific tag, skip any others
						if (!vm.serviceNameSearchSpecificTag
								|| tagName == vm.serviceNameSearchSpecificTag) {

							if (tagName.toLowerCase().match(validNameFilterRegex)
								|| (tagValue && tagValue.toLowerCase().match(validNameFilterRegex))) {
								include = true;
								break;
							}
						}
					}
				}

				if (!include) {
					continue;
				}
			}

			filteredServices.push(service);
		}

		//always sort by name first
		filteredServices = linq(filteredServices).OrderBy(s => s.name.toLowerCase()).ToArray();

		if (vm.sortFilter) {

			//console.log('sorting by ' + vm.sortFilter);

			if (vm.sortFilter == 'NameAsc'
				|| vm.sortFilter == 'NameDesc') {
				//already sorted by name

				if (vm.sortFilter == 'NameDesc') {
					filteredServices = filteredServices.reverse();
				}

			} else if (vm.sortFilter == 'IDAsc'
				|| vm.sortFilter == 'IDDesc') {
				filteredServices = linq(filteredServices).OrderBy(s => this.getSortingLocalId(s)).ToArray();

				if (vm.sortFilter == 'IDDesc') {
					filteredServices = filteredServices.reverse();
				}

			} else if (vm.sortFilter == 'ParentAsc'
				|| vm.sortFilter == 'ParentDesc') {
				filteredServices = linq(filteredServices).OrderBy(s => this.getSortingCcgCode(s)).ToArray();

				if (vm.sortFilter == 'ParentDesc') {
					filteredServices = filteredServices.reverse();
				}

			} else if (vm.sortFilter == 'PublisherConfigAsc'
				|| vm.sortFilter == 'PublisherConfigDesc') {
				filteredServices = linq(filteredServices).OrderBy(s => this.getSortingPublisherConfigName(s)).ToArray();

				if (vm.sortFilter == 'PublisherConfigDesc') {
					filteredServices = filteredServices.reverse();
				}

			} else if (vm.sortFilter == 'LastDataAsc'
				|| vm.sortFilter == 'LastDataDesc') {

				//always use the most recent date if multiple publisher feeds are present
				filteredServices = linq(filteredServices).OrderBy(s => this.getSortingDate(s)).ToArray();

				if (vm.sortFilter == 'LastDataDesc') {
					filteredServices = filteredServices.reverse();
				}

			} else if (vm.sortFilter == 'StatusAsc'
				|| vm.sortFilter == 'StatusDesc') {

				//use a special function that works out a sorting value for the status
				filteredServices = linq(filteredServices).OrderBy(s => this.getSortingStatusValue(s)).ToArray();

				if (vm.sortFilter == 'StatusDesc') {
					filteredServices = filteredServices.reverse();
				}

			} else {
				console.log('unknown sort mode ' + vm.sortFilter);
			}
		}

		return filteredServices;

	}

	private getSortingLocalId(s:Service):string {
		if (s.localIdentifier) {
			return s.localIdentifier;

		} else {
			return '';
		}
	}

	private getSortingDate(s: Service): number {
		var ret;

		if (s.systemStatuses) {

			for (var i = 0; i < s.systemStatuses.length; i++) {
				var status = s.systemStatuses[i];
				if (status.lastReceivedExtractCutoff
					&& (!ret
					|| status.lastReceivedExtractCutoff > ret)) {

					ret = status.lastReceivedExtractCutoff;
				}
			}
		}

		//if no date, then use the current time
		if (!ret) {
			ret = new Date().getTime();
			//ret = 0;
		}

		//console.log('service ' + s.name + ' ret ' + ret + ' -> ' + new Date().setTime(ret));
		return ret * -1; //multiple by -1 so we get everything ordered more intuitively
	}

	private getSortingStatusValue(s: Service): number {

		var ret = 0; //no status

		//console.log('service ' + s.name + ' has statuses ' + s.systemStatuses);
		if (s.systemStatuses) {

			for (var i = 0; i < s.systemStatuses.length; i++) {
				var status = s.systemStatuses[i];

				var thisVal;

				if (!status.lastReceivedExtract) { //no data
					thisVal = 1;

				} else if (!status.processingInError //OK
					&& status.processingUpToDate) {
					thisVal = 2;

				} else if (!status.processingInError //behind
					&& !status.processingUpToDate) {
					thisVal = 3;

				} else { //error
					thisVal = 4
				}

				if (thisVal > ret) {
					ret = thisVal;
				}
			}
		}

		//console.log('service ' + s.name + ' ret ' + ret);
		return ret;
	}


	private getSortingCcgCode(s:Service):string {
		if (s.ccgCode) {
			return s.ccgCode;
		} else {
			return '';
		}
	}

	private getSortingPublisherConfigName(s:Service):string {
		if (s.publisherConfigName) {
			return s.publisherConfigName;
		} else {
			return '';
		}
	}

	getCcgName(ccgCode: string) : string {

		if (!ccgCode || ccgCode.length == 0) {
			return '';
		}

		//if we've already cached it, then just look up and return
		var vm = this;
		if (vm.ccgNameCache) {
			var ret = vm.ccgNameCache[ccgCode];
			if (!ret) {
				ret = '???';
			}
			return ret;
		}

		//if not pre-cached it, then we
		if (!vm.refreshingCcgNameCache) {
			vm.refreshingCcgNameCache = true;

			vm.getCcgNames()
				.subscribe(
					(result) => {
						vm.ccgNameCache = result;
						vm.refreshingCcgNameCache = false;
					},
					(error) => {
						vm.refreshingCcgNameCache = false;
					}
				);
		}

		//return an empty string until the above has come back
		return 'loading...';
	}
	/*getCcgName(ccgCode: string) : string {

		if (!ccgCode || ccgCode.length == 0) {
			return '';
		}

		var vm = this;
		if (!vm.ccgNameCache || Object.keys(vm.ccgNameCache).length === 0) {

			var map = {};

			map['02N'] = 'NHS Airedale, Wharfedale and Craven CCG';
			map['09C'] = 'NHS Ashford CCG';
			map['07L'] = 'NHS Barking and Dagenham CCG';
			map['07M'] = 'NHS Barnet CCG';
			map['02P'] = 'NHS Barnsley CCG';
			map['99E'] = 'NHS Basildon and Brentwood CCG';
			map['02Q'] = 'NHS Bassetlaw CCG';
			map['11E'] = 'NHS Bath and North East Somerset CCG';
			map['06F'] = 'NHS Bedfordshire CCG';
			map['07N'] = 'NHS Bexley CCG';
			map['00Q'] = 'NHS Blackburn with Darwen CCG';
			map['00R'] = 'NHS Blackpool CCG';
			map['00T'] = 'NHS Bolton CCG';
			map['02W'] = 'NHS Bradford City CCG';
			map['02R'] = 'NHS Bradford Districts CCG';
			map['07P'] = 'NHS Brent CCG';
			map['09D'] = 'NHS Brighton and Hove CCG';
			map['07Q'] = 'NHS Bromley CCG';
			map['00V'] = 'NHS Bury CCG';
			map['02T'] = 'NHS Calderdale CCG';
			map['06H'] = 'NHS Cambridgeshire and Peterborough CCG';
			map['07R'] = 'NHS Camden CCG';
			map['04Y'] = 'NHS Cannock Chase CCG';
			map['09E'] = 'NHS Canterbury and Coastal CCG';
			map['99F'] = 'NHS Castle Point and Rochford CCG';
			map['09A'] = 'NHS Central London (Westminster) CCG';
			map['00X'] = 'NHS Chorley and South Ribble CCG';
			map['07T'] = 'NHS City and Hackney CCG';
			map['03V'] = 'NHS Corby CCG';
			map['05A'] = 'NHS Coventry and Rugby CCG';
			map['09H'] = 'NHS Crawley CCG';
			map['07V'] = 'NHS Croydon CCG';
			map['00C'] = 'NHS Darlington CCG';
			map['09J'] = 'NHS Dartford, Gravesham and Swanley CCG';
			map['02X'] = 'NHS Doncaster CCG';
			map['11J'] = 'NHS Dorset CCG';
			map['05C'] = 'NHS Dudley CCG';
			map['00D'] = 'NHS Durham Dales, Easington and Sedgefield CCG';
			map['07W'] = 'NHS Ealing CCG';
			map['06K'] = 'NHS East and North Hertfordshire CCG';
			map['01A'] = 'NHS East Lancashire CCG';
			map['03W'] = 'NHS East Leicestershire and Rutland CCG';
			map['02Y'] = 'NHS East Riding of Yorkshire CCG';
			map['05D'] = 'NHS East Staffordshire CCG';
			map['09L'] = 'NHS East Surrey CCG';
			map['09F'] = 'NHS Eastbourne, Hailsham and Seaford CCG';
			map['01C'] = 'NHS Eastern Cheshire CCG';
			map['07X'] = 'NHS Enfield CCG';
			map['03X'] = 'NHS Erewash CCG';
			map['10K'] = 'NHS Fareham and Gosport CCG';
			map['11M'] = 'NHS Gloucestershire CCG';
			map['06M'] = 'NHS Great Yarmouth and Waveney CCG';
			map['03A'] = 'NHS Greater Huddersfield CCG';
			map['08A'] = 'NHS Greenwich CCG';
			map['01F'] = 'NHS Halton CCG';
			map['03D'] = 'NHS Hambleton, Richmondshire and Whitby CCG';
			map['08C'] = 'NHS Hammersmith and Fulham CCG';
			map['03Y'] = 'NHS Hardwick CCG';
			map['08D'] = 'NHS Haringey CCG';
			map['03E'] = 'NHS Harrogate and Rural District CCG';
			map['08E'] = 'NHS Harrow CCG';
			map['00K'] = 'NHS Hartlepool and Stockton-on-Tees CCG';
			map['09P'] = 'NHS Hastings and Rother CCG';
			map['08F'] = 'NHS Havering CCG';
			map['05F'] = 'NHS Herefordshire CCG';
			map['06N'] = 'NHS Herts Valleys CCG';
			map['01D'] = 'NHS Heywood, Middleton and Rochdale CCG';
			map['99K'] = 'NHS High Weald Lewes Havens CCG';
			map['08G'] = 'NHS Hillingdon CCG';
			map['09X'] = 'NHS Horsham and Mid Sussex CCG';
			map['07Y'] = 'NHS Hounslow CCG';
			map['03F'] = 'NHS Hull CCG';
			map['06L'] = 'NHS Ipswich and East Suffolk CCG';
			map['10L'] = 'NHS Isle of Wight CCG';
			map['08H'] = 'NHS Islington CCG';
			map['11N'] = 'NHS Kernow CCG';
			map['08J'] = 'NHS Kingston CCG';
			map['01J'] = 'NHS Knowsley CCG';
			map['08K'] = 'NHS Lambeth CCG';
			map['04C'] = 'NHS Leicester City CCG';
			map['08L'] = 'NHS Lewisham CCG';
			map['03T'] = 'NHS Lincolnshire East CCG';
			map['04D'] = 'NHS Lincolnshire West CCG';
			map['99A'] = 'NHS Liverpool CCG';
			map['06P'] = 'NHS Luton CCG';
			map['04E'] = 'NHS Mansfield and Ashfield CCG';
			map['09W'] = 'NHS Medway CCG';
			map['08R'] = 'NHS Merton CCG';
			map['06Q'] = 'NHS Mid Essex CCG';
			map['04F'] = 'NHS Milton Keynes CCG';
			map['04G'] = 'NHS Nene CCG';
			map['04H'] = 'NHS Newark and Sherwood CCG';
			map['08M'] = 'NHS Newham CCG';
			map['04J'] = 'NHS North Derbyshire CCG';
			map['00J'] = 'NHS North Durham CCG';
			map['06T'] = 'NHS North East Essex CCG';
			map['99M'] = 'NHS North East Hampshire and Farnham CCG';
			map['03H'] = 'NHS North East Lincolnshire CCG';
			map['10J'] = 'NHS North Hampshire CCG';
			map['03J'] = 'NHS North Kirklees CCG';
			map['03K'] = 'NHS North Lincolnshire CCG';
			map['06V'] = 'NHS North Norfolk CCG';
			map['05G'] = 'NHS North Staffordshire CCG';
			map['99C'] = 'NHS North Tyneside CCG';
			map['09Y'] = 'NHS North West Surrey CCG';
			map['99P'] = 'NHS Northern, Eastern and Western Devon CCG';
			map['00L'] = 'NHS Northumberland CCG';
			map['04K'] = 'NHS Nottingham City CCG';
			map['04L'] = 'NHS Nottingham North and East CCG';
			map['04M'] = 'NHS Nottingham West CCG';
			map['00Y'] = 'NHS Oldham CCG';
			map['10Q'] = 'NHS Oxfordshire CCG';
			map['10R'] = 'NHS Portsmouth CCG';
			map['08N'] = 'NHS Redbridge CCG';
			map['05J'] = 'NHS Redditch and Bromsgrove CCG';
			map['08P'] = 'NHS Richmond CCG';
			map['03L'] = 'NHS Rotherham CCG';
			map['04N'] = 'NHS Rushcliffe CCG';
			map['01G'] = 'NHS Salford CCG';
			map['05L'] = 'NHS Sandwell and West Birmingham CCG';
			map['03M'] = 'NHS Scarborough and Ryedale CCG';
			map['03N'] = 'NHS Sheffield CCG';
			map['05N'] = 'NHS Shropshire CCG';
			map['11X'] = 'NHS Somerset CCG';
			map['01R'] = 'NHS South Cheshire CCG';
			map['99Q'] = 'NHS South Devon and Torbay CCG';
			map['05Q'] = 'NHS South East Staffordshire and Seisdon Peninsula CCG';
			map['10V'] = 'NHS South Eastern Hampshire CCG';
			map['10A'] = 'NHS South Kent Coast CCG';
			map['99D'] = 'NHS South Lincolnshire CCG';
			map['01T'] = 'NHS South Sefton CCG';
			map['00M'] = 'NHS South Tees CCG';
			map['00N'] = 'NHS South Tyneside CCG';
			map['05R'] = 'NHS South Warwickshire CCG';
			map['04Q'] = 'NHS South West Lincolnshire CCG';
			map['05T'] = 'NHS South Worcestershire CCG';
			map['10X'] = 'NHS Southampton CCG';
			map['99G'] = 'NHS Southend CCG';
			map['04R'] = 'NHS Southern Derbyshire CCG';
			map['01V'] = 'NHS Southport and Formby CCG';
			map['08Q'] = 'NHS Southwark CCG';
			map['01X'] = 'NHS St Helens CCG';
			map['05V'] = 'NHS Stafford and Surrounds CCG';
			map['01W'] = 'NHS Stockport CCG';
			map['05W'] = 'NHS Stoke on Trent CCG';
			map['00P'] = 'NHS Sunderland CCG';
			map['99H'] = 'NHS Surrey Downs CCG';
			map['10C'] = 'NHS Surrey Heath CCG';
			map['08T'] = 'NHS Sutton CCG';
			map['10D'] = 'NHS Swale CCG';
			map['12D'] = 'NHS Swindon CCG';
			map['01Y'] = 'NHS Tameside and Glossop CCG';
			map['05X'] = 'NHS Telford and Wrekin CCG';
			map['10E'] = 'NHS Thanet CCG';
			map['07G'] = 'NHS Thurrock CCG';
			map['08V'] = 'NHS Tower Hamlets CCG';
			map['02A'] = 'NHS Trafford CCG';
			map['03Q'] = 'NHS Vale of York CCG';
			map['02D'] = 'NHS Vale Royal CCG';
			map['03R'] = 'NHS Wakefield CCG';
			map['05Y'] = 'NHS Walsall CCG';
			map['08W'] = 'NHS Waltham Forest CCG';
			map['08X'] = 'NHS Wandsworth CCG';
			map['02E'] = 'NHS Warrington CCG';
			map['05H'] = 'NHS Warwickshire North CCG';
			map['02F'] = 'NHS West Cheshire CCG';
			map['07H'] = 'NHS West Essex CCG';
			map['11A'] = 'NHS West Hampshire CCG';
			map['99J'] = 'NHS West Kent CCG';
			map['02G'] = 'NHS West Lancashire CCG';
			map['04V'] = 'NHS West Leicestershire CCG';
			map['08Y'] = 'NHS West London CCG';
			map['07J'] = 'NHS West Norfolk CCG';
			map['07K'] = 'NHS West Suffolk CCG';
			map['02H'] = 'NHS Wigan Borough CCG';
			map['99N'] = 'NHS Wiltshire CCG';
			map['12F'] = 'NHS Wirral CCG';
			map['06A'] = 'NHS Wolverhampton CCG';
			map['06D'] = 'NHS Wyre Forest CCG';
			map['13T'] = 'NHS Newcastle Gateshead CCG';
			map['09G'] = 'NHS Coastal West Sussex CCG';
			map['09N'] = 'NHS Guildford and Waverley CCG';
			map['01H'] = 'NHS North Cumbria CCG';
			map['14L'] = 'NHS Manchester CCG';
			map['06W'] = 'NHS Norwich CCG';
			map['06Y'] = 'NHS South Norfolk CCG';
			map['15E'] = 'NHS Birmingham and Solihull CCG';
			map['15A'] = 'NHS Berkshire West CCG';
			map['15C'] = 'NHS Bristol, North Somerset and South Gloucestershire CCG';
			map['14Y'] = 'NHS Buckinghamshire CCG';
			map['15D'] = 'NHS East Berkshire CCG';
			map['15F'] = 'NHS Leeds CCG';
			map['02M'] = 'NHS Fylde and Wyre CCG';
			map['01E'] = 'NHS Greater Preston CCG';
			map['01K'] = 'NHS Morecambe Bay CCG';

			//add hospital trusts too
			map['R1H'] = 'BARTS HEALTH NHS TRUST';
			map['RWK'] = 'EAST LONDON NHS FOUNDATION TRUST';

			//add parents of hospital trusts
			map['Q71'] = 'NHS ENGLAND LONDON';
			map['Q74'] = 'NHS ENGLAND NORTH (CUMBRIA AND NORTH EAST)';
			map['Q82'] = 'NHS ENGLAND SOUTH (SOUTH CENTRAL)';


			vm.ccgNameCache = map;
		}

		var ret = vm.ccgNameCache[ccgCode];
		if (!ret) {
			ret = "???";
		}
		return ret;
		//return 'CCG name for ' + ccgCode + ' here!';
	}*/

	public getTagNamesFromCache(): string[] {
		var vm = this;

		//if we've pre-cached this, then just return it
		if (vm.tagNameCache) {
			return vm.tagNameCache;
		}

		//if not pre-cached it, then hit the server
		if (!vm.refreshingTagNameCache) {
			vm.refreshingTagNameCache = true;

			vm.getTagNames()
				.subscribe(
					(result) => {
						vm.tagNameCache = result;
						vm.refreshingTagNameCache = false;
					},
					(error) => {
						vm.refreshingTagNameCache = false;
					}
				);
		}

		//return an empty string until the above has come back
		return [];
	}

	public getPublisherConfigNamesFromCache(): string[] {
		var vm = this;

		//if we've pre-cached this, then just return it
		if (vm.publisherConfigNameCache) {
			return vm.publisherConfigNameCache;
		}

		//if not pre-cached it, then we hit the server
		if (!vm.refreshingPublisherConfigNameCache) {
			vm.refreshingPublisherConfigNameCache = true;

			vm.getPublisherConfigNames()
				.subscribe(
					(result) => {
						vm.publisherConfigNameCache = result;
						vm.refreshingPublisherConfigNameCache = false;
					},
					(error) => {
						vm.refreshingPublisherConfigNameCache = false;
					}
				);
		}

		//return an empty string until the above has come back
		return [];
	}

	public getCcgCodesFromCache(): string[] {
		var vm = this;

		//if we've pre-cached this, then just return it
		if (vm.ccgCodeCache) {
			return vm.ccgCodeCache;
		}

		//if not pre-cached it, then we
		if (!vm.refreshingCcgCodeCache) {
			vm.refreshingCcgCodeCache = true;

			vm.getCcgCodes()
				.subscribe(
					(result) => {
						vm.ccgCodeCache = result;
						vm.refreshingCcgCodeCache = false;
					},
					(error) => {
						vm.refreshingCcgCodeCache = false;
					}
				);
		}

		//return an empty string until the above has come back
		return [];
	}


	public createTagStr(service: Service): string {
		var vm = this;

		var list = vm.getTagNamesFromCache();
		var ret = '' as string;

		if (service.tags
			&& list) {

			for (var i=0; i<list.length; i++) {
				var tagName = list[i];

				if (service.tags.hasOwnProperty(tagName)) {
					var tagValue = service.tags[tagName];

					if (ret.length > 0) {
						ret += ', ';
					}
					ret += tagName;
					if (tagValue) {
						ret += ' ';
						ret += tagValue;
					}
				}
			}
		}

		return ret;
	}

	clearFilters() {
		var vm = this;

		vm.serviceNameSearchIncludeTags = false;
		vm.serviceNameSearchSpecificTag = '';
		vm.serviceCcgCodeFilterIsRegex = false;
		vm.sortFilter = 'NameAsc';
		vm.serviceNameFilter = null;
		vm.servicePublisherConfigFilter = null;
		vm.serviceStatusFilter = null;
		vm.serviceCcgCodeFilterStr = null;
		vm.serviceCcgCodeFilterRegex = null;
		vm.serviceLastDataFilter = null;
		vm.servicePublisherModeFilter = null;
		vm.serviceHideClosedFilter = true;
		vm.serviceShowDateFilter = false;
	}


	getDpaHistory(serviceId : string) : Observable<DpaHistory[]> {
		let params = new URLSearchParams();
		params.set('uuid', serviceId);
		return this.httpGet('api/service/dpaHistory', { search : params });
	}

	getSubscriberHistory(serviceId : string) : Observable<SubscriberHistory[]> {
		let params = new URLSearchParams();
		params.set('uuid', serviceId);
		return this.httpGet('api/service/subscriberHistory', { search : params });
	}
}

