import {Component, Input} from "@angular/core";
import {RabbitExchange} from "../queueing/models/RabbitExchange";

@Component({
	selector: 'rabbit-exchange',
	template:
		`
	<div class="progress">
		<div class="small">{{exchange?.name}} ({{exchange?.message_stats?.publish_in_details?.rate}}/s)</div>
		<ngb-progressbar
			min="0" 
			max="{{exchangeRateMax}}" 
			value="{{exchange?.message_stats?.publish_in_details?.rate}}" 
			[ngClass]="getExchangeRateAsWidthStylePercent(exchange)"></ngb-progressbar>
	</div>
`
})
export class ExchangeComponent {
	@Input() exchange : any;

	exchangeRateMax : number = 50;

	getExchangeRateAsWidthStylePercent(exchange : RabbitExchange){
		if (!exchange || !exchange.message_stats || !exchange.message_stats.publish_in_details || !exchange.message_stats.publish_in_details.rate)
			return {width : '0%'};

		var pcnt = (exchange.message_stats.publish_in_details.rate * 100) / this.exchangeRateMax;
		return {width : pcnt + '%'};
	}
}