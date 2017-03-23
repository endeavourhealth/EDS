import {Component, Input, Output, EventEmitter} from "@angular/core";
import {LoggerService} from "../common/logger.service";
import {RecordViewerService} from "./recordViewer.service";
import {UIPerson} from "./models/resources/admin/UIPerson";
import {linq} from "../common/linq";
import {UIEpisodeOfCare} from "./models/resources/clinical/UIEpisodeOfCare";

const Highcharts = require('highcharts/highcharts.src');
import 'highcharts/adapters/standalone-framework.src';
import {LocationIcon} from "./pipes/location";
import {UIOrganisation} from "./models/resources/admin/UIOrganisation";

@Component({
	selector : 'episodeView',
	template : require('./episodeView.html')
})
export class EpisodeViewComponent {
	private GP : number = 1;
	private COMMUNITY : number = -1;
	private HOSPITAL : number = -2;

	@Input()
	set person(person: any) {
		this._person = person;
		this.loadEpisodes();
	}

	@Output() episodeChange: EventEmitter<UIEpisodeOfCare> = new EventEmitter<UIEpisodeOfCare>();
	private _person: UIPerson;
	private _chart : any;
	private _hoverEpisode : UIEpisodeOfCare;
	public currentEpisodes: UIEpisodeOfCare[];
	public pastEpisodes: UIEpisodeOfCare[];

	constructor(protected logger: LoggerService, protected recordViewerService : RecordViewerService) {
	}

	loadEpisodes() {
		let vm = this;
		// let episodes = this.getDummyEpisodeData();
		vm.recordViewerService.getEpisodes(vm._person).subscribe(
			(episodes : UIEpisodeOfCare[]) => {
				vm.currentEpisodes = linq(episodes)
					.Where(t => t.period.end == null)
					.OrderByDescending(t => t.period.start.date)
					.ToArray();
				vm.pastEpisodes = linq(episodes)
					.Where(t => t.period.end != null)
					.OrderByDescending(t => t.period.start.date)
					.ToArray();
				vm.showTimeLine();
		 	},
		 	(error) => vm.logger.error("Error loading episodes",error)
		 );
	}

	selectEpisode(episode: UIEpisodeOfCare) {
		this.episodeChange.emit(episode);
	}

	showTimeLine() {
		let config = this.chartConfig();
		config.series = this.chartSeries();
		this._chart = Highcharts.chart('timeline', config);
		this.zoomYears(1);
	}

	zoomYears(years) {
		// Default the view to the last 12 months
		let today = new Date();
		let lastYear = new Date();
		lastYear.setFullYear(lastYear.getFullYear() - years);
		this._chart.xAxis[0].setExtremes(lastYear.getTime(), today.getTime());
	}

	zoomAll() {
		this._chart.xAxis[0].setExtremes(null, null);
	}

	chartConfig() {
		return {
			chart: {
				resetZoomButton: {
					theme: {
						display: 'none'
					}
				},
				type: 'area',
				zoomType: 'x'
			},
			title:{
				text:null
			},
			xAxis: {

				maxPadding : 0,
				offset:-95,
				lineWidth:2,
				lineColor:'#000000',
				max : new Date().getTime(),
				reversed : true,
				type: 'datetime',
				title: {
					text: null
				},
			},
			yAxis: {
				categories: {
					"1": "GP Surgery",
					"0" : "",
					"-1": 'Community',
					"-2": 'Hospital'
				},
				labels : {
					useHTML:true,
					formatter : function() {
						return '<i class="fa ' + LocationIcon.get(this.value) + '"></i>';
					}
				},
				lineWidth:2,
				lineColor:'#000000',
				max : 1,
				min : -2,
				title: {
					text: '  Secondary         Primary'
				}
			},
			tooltip: {
				headerFormat: '<b>{series.name}</b><br>',
				pointFormat: '{point.x:%d-%b-%Y}'
			},
			legend: {
				enabled : false
			},
			plotOptions: {
				area: {
					fillOpacity: 0.5,
					marker: {
						enabled: false
					},
				},
			},
			series : []
		};
	}

	chartSeries() {
		let allEpisodes : UIEpisodeOfCare[] = this.currentEpisodes
			.concat(this.pastEpisodes);
		allEpisodes = linq(allEpisodes)
			.OrderBy(e => e.managingOrganisation.name)
			.ThenBy(e => e.period.start.date)
			.ToArray();

		let managingOrganisation : UIOrganisation = null;
		let series : any[] = [];
		let orgData : any = null;

		for(let episode of allEpisodes) {
			if (!managingOrganisation || managingOrganisation.name != episode.managingOrganisation.name) {
				managingOrganisation = episode.managingOrganisation;
				orgData = {
					name : managingOrganisation.name,
					data : []
				};
				series.push(orgData);
			}

			if (episode.period.start) {
				orgData.data.push({
					x : episode.period.start.date,
					y : this.getOrgTypeValue(episode.managingOrganisation.type),
					events : { mouseOver : () => this.hoverEpisode(episode) }
				});
				if (episode.period.end) {
					orgData.data.push({
						x : episode.period.end.date,
						y : this.getOrgTypeValue(episode.managingOrganisation.type),
						events : { mouseOver : () => this.hoverEpisode(episode) }
				});
					orgData.data.push({
						x : episode.period.end.date,
						y : null,
						events : { mouseOver : () => this.hoverEpisode(episode) }
					});
				} else {
					orgData.data.push({
						x: new Date().getTime(),
						y: this.getOrgTypeValue(episode.managingOrganisation.type),
						events: {mouseOver: () => this.hoverEpisode(episode)}
					});
				}
			}
		}

		return series;
	}

	getOrgTypeValue(organisationType : String) {
		switch (organisationType) {
			case 'GP Surgery':
				return this.GP;
			case 'Hospital' :
				return this.HOSPITAL;
			case 'Community' :
				return this.COMMUNITY;
			default:
				return 0;
		}
	}

	hoverEpisode(episode : UIEpisodeOfCare) {
		this._hoverEpisode = episode;
	}

	getDummyEpisodeData() : any[] {
		let patientId = {
			resourceId: "b9da1825-c411-4852-bcdf-b4091d5542a7",
			serviceId: "db7eba14-4a89-4090-abf8-af6c60742cb1",
			systemId: "db8fa60e-08ff-4b61-ba4c-6170e6cb8df7"
		};

		return [
			{
				managingOrganisation : {name : "LGI", type : "Hospital"},
				period : {start : {date : Date.UTC(1973,9,26)}, end : {date : Date.UTC(1973,9, 30)}},
				patient : { patientId : patientId }
			},
			{
				managingOrganisation : {name : "LGI", type : "Hospital"},
				period : {start : {date : Date.UTC(1978,4, 23,11,0,0)}, end : {date : Date.UTC(1978,4, 23,14,30,0)}},
				patient : { patientId : patientId }
			},
			{
				managingOrganisation : {name : "LGI", type : "Hospital"},
				period : {start : {date : Date.UTC(1983,1,2)}, end : {date : Date.UTC(1983,1, 20)}},
				patient : { patientId : patientId }
			},
			{
				managingOrganisation : {name : "Huddersfield Royal Infirmary", type : "Hospital"},
				period : {start : {date : Date.UTC(1993,3,6)}, end : {date : Date.UTC(1993,3, 8)}},
				patient : { patientId : patientId }
			},
			{
				managingOrganisation : {name : "Huddersfield Royal Infirmary", type : "Hospital"},
				period : {start : {date : Date.UTC(1993,3,10)}, end : {date : Date.UTC(1993,3, 12)}},
				patient : { patientId : patientId }
			},
			{
				managingOrganisation : {name : "Huddersfield Royal Infirmary", type : "Hospital"},
				period : {start : {date : Date.UTC(1993,3,14)}, end : {date : Date.UTC(1993,6, 30)}},
				patient : { patientId : patientId }
			},
			{
				managingOrganisation : {name : "LGI", type : "Hospital"},
				period : {start : {date : Date.UTC(2017,1,2)}, end : null},
				patient : { patientId : patientId }
			},


			{
				managingOrganisation : {name : "Diabetes Clinic", type : "Community"},
				period : {start : {date : Date.UTC(2011,5,12)}, end : {date : Date.UTC(2011, 11, 12)}},
				patient : { patientId : patientId }
			},
			{
				managingOrganisation : {name : "Diabetes Clinic", type : "Community"},
				period : {start : {date : Date.UTC(2013,7,18)}, end : {date : Date.UTC(2013, 9, 25)}},
				patient : { patientId : patientId }
			},
			{
				managingOrganisation : {name : "Diabetes Clinic", type : "Community"},
				period : {start : {date : Date.UTC(2016,11,12)}, end : null},
				patient : { patientId : patientId }
			},

			{
				managingOrganisation : {name : "Golcar Surgery", type : "GP Surgery"},
				period : {start : {date : Date.UTC(1973,9,30)}, end : {date : Date.UTC(1980, 2, 17)}},
				patient : { patientId : patientId }
			},
			{
				managingOrganisation : {name : "Croft House", type : "GP Surgery"},
				period : {start : {date : Date.UTC(1980,2,18)}, end : {date : Date.UTC(1992, 8, 5)}},
				patient : { patientId : patientId }
			},
			{
				managingOrganisation : {name : "Leeds University", type : "GP Surgery"},
				period : {start : {date : Date.UTC(1992,8,5)}, end : {date : Date.UTC(1996, 6, 15)}},
				patient : { patientId : patientId }
			},
			{
				managingOrganisation : {name : "Croft House", type : "GP Surgery"},
				period : {start : {date : Date.UTC(1996,6,30)}, end : null},
				patient : { patientId : patientId }
			},

		];
	}
}

