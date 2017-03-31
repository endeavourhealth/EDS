import {Pipe, PipeTransform} from "@angular/core";

@Pipe({name : 'locationClass'})
export class LocationClass implements PipeTransform {
	transform(locationType: string): string {
		switch (locationType) {
			case 'GP Surgery':
			case 'PR':
				return 'panel-success';
			case 'Hospital' :
			case 'TR' :
				return 'panel-danger';
			case 'Community' :
				return 'panel-warning';
			default:
				return 'panel-default';
		}
	}
}

@Pipe({name : 'locationIcon'})
export class LocationIcon implements PipeTransform {
	public static get(locationType: string): string {
		switch (locationType) {
			case '':
				return 'fa-fw';
			case 'GP Surgery':
			case 'PR':
				return 'fa-user-md';
			case 'Hospital' :
			case 'TR' :
				return 'fa-hospital-o';
			case 'Community' :
				return 'fa-medkit';
			case 'Multiple':
				return 'fa-share-alt';
			default:
				return 'fa-question';
		}
	}

	transform(locationType: string): string {
		return LocationIcon.get(locationType);
	}
}
