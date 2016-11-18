import {Routing} from "./Routing";
import {ConfigService} from "../config/config.service";
import {ConfigurationResource} from "../config/ConfigurationResource";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs";

@Injectable()
export class QueueingService {

	private configurationId = "b9b14e26-5a52-4f36-ad89-f01e465c1361";
	private configurationName = "RouteGroup";

	constructor(protected configService : ConfigService) {
	}

	getRoutings(): Observable<Routing[]> {
		var observable : Observable<Routing[]> = Observable.create(observer => {
			this.configService.getConfig(this.configurationId)
				.subscribe(
					(configResource) => observer.next(<Routing[]>JSON.parse(configResource.configurationData)),
					(error) => observer.error(error)
				);
			}
		);

		return observable;
	}

	saveRoutings(routeGroups : Routing[]) : Observable<any> {
		var configurationResource: ConfigurationResource = {
			configurationId: this.configurationId,
			configurationName: this.configurationName,
			configurationData: JSON.stringify(routeGroups)
		};
		var observable = Observable.create(observer => {
			this.configService.saveConfig(configurationResource)
				.subscribe(
					() => observer.next(),
					(error) => observer.error(error)
				)
		});

		return observable;
	}
}
