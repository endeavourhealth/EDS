import {Component, Input} from "@angular/core";
import {RabbitQueue} from "../queueing/models/RabbitQueue";

@Component({
	selector: 'rabbit-queue',
	template:
		`
	<div class="progress">
		<div class="small">{{getName(queue?.name)}} ({{queue?.messages_ready}} @ {{queue?.message_stats?.publish_details?.rate}}/s)</div>
		<ngb-progressbar 
			min="0" 
			max="{{queueRateMax}}" 
			value="{{queue?.message_stats?.publish_details?.rate}}" 
			[ngClass]="getQueueRateAsWidthStylePercent(queue)"></ngb-progressbar>
	</div>
`
})
export class QueueComponent {
	@Input() queue : RabbitQueue;

	queueRateMax : number = 50;

	getQueueRateAsWidthStylePercent(queue : RabbitQueue){
		if (!queue || !queue.message_stats || !queue.message_stats.publish_details || !queue.message_stats.publish_details.rate)
			return {width : '0%'};

		var pcnt = (queue.message_stats.publish_details.rate * 100) / this.queueRateMax;
		return {width : pcnt + '%'};
	}

	getName(name : String) {
		var hyphen = name.indexOf('-');
		if (hyphen === -1)
			return name;
		return name.substr(hyphen+1);
	}
}