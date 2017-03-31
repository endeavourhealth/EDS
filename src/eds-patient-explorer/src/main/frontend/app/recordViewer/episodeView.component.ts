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
	private UNKNOWN : number = 2;
	private GP : number = 1;
	private COMMUNITY : number = -1;
	private HOSPITAL : number = -2;

	@Input()
	set person(person: any) {
		this._person = person;
		this.loadEpisodes();
	}

	@Output() episodeChange: EventEmitter<UIEpisodeOfCare[]> = new EventEmitter<UIEpisodeOfCare[]>();
	private _person: UIPerson;
	private _chart : any;
	private _hoverEpisode : UIEpisodeOfCare;
	public currentEpisodes: UIEpisodeOfCare[];
	public pastEpisodes: UIEpisodeOfCare[];

	constructor(protected logger: LoggerService, protected recordViewerService : RecordViewerService) {
	}

	loadEpisodes() {
		let vm = this;
		vm.currentEpisodes = null;
		vm.pastEpisodes = null;
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
		this.episodeChange.emit([episode]);
	}

	selectCurrentEpisodes() {
		this.episodeChange.emit(this.currentEpisodes);
	}

	selectPastEpisodes() {
		this.episodeChange.emit(this.pastEpisodes);
	}

	selectAllEpisodes() {
		this.episodeChange.emit(this.currentEpisodes.concat(this.pastEpisodes));
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
				offset:-78,
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
					"2" : "Unknown",
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
				max : 2,
				min : -2,
				title: {
					text: 'Secondary          GP     N/K'
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
					events : {
						mouseOver : () => this.hoverEpisode(episode),
						click : () => this.selectEpisode(episode)
					}
				});
				if (episode.period.end) {
					orgData.data.push({
						x : episode.period.end.date,
						y : this.getOrgTypeValue(episode.managingOrganisation.type),
						events : {
							mouseOver : () => this.hoverEpisode(episode),
							click : () => this.selectEpisode(episode)
						}
				});
					orgData.data.push({
						x : episode.period.end.date,
						y : null,
						events : {
							mouseOver : () => this.hoverEpisode(episode),
							click : () => this.selectEpisode(episode)
						}
					});
				} else {
					orgData.data.push({
						x: new Date().getTime(),
						y: this.getOrgTypeValue(episode.managingOrganisation.type),
						events: {
							mouseOver: () => this.hoverEpisode(episode),
							click : () => this.selectEpisode(episode)
						}
					});
				}
			}
		}

		return series;
	}

	getOrgTypeValue(organisationType : String) {
		switch (organisationType) {
			case 'GP Surgery':
			case 'PR':
				return this.GP;
			case 'Hospital' :
			case 'TR' :
				return this.HOSPITAL;
			case 'Community' :
				return this.COMMUNITY;
			default:
				return this.UNKNOWN;
		}
	}

	hoverEpisode(episode : UIEpisodeOfCare) {
		this._hoverEpisode = episode;
	}
}

