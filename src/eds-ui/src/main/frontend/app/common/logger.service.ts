import {Logger} from "angular2-logger/core";
import {Injectable} from "@angular/core";
import {ToastsManager} from "ng2-toastr";
import {Response} from '@angular/http';

@Injectable()
export class LoggerService {
	constructor(private $log : Logger, private toastr : ToastsManager) {
		$log.level = $log.Level.LOG;
	}

	// straight to console; bypass toastr
	log(...args:any[]) {
		this.$log.log(args);
	}

	error(message:string, data?:{}, title?:string) {
		this.toastr.error(this.appendResponseMessage(message, data), title, {enableHTML: true});
		//this.toastr.error(message, title);
		this.$log.error('Error: ' + message, '\nSummary:', title, '\nDetails:', data);
	}

	info(message:string, data?:{}, title?:string) {
		this.toastr.info(this.appendResponseMessage(message, data), title, {enableHTML: true});
		//this.toastr.info(message, title);
		this.$log.info('Info: ' + message, '\nSummary:', title, '\nDetails:', data);
	}

	success(message:string, data?:{}, title?:string) {
		this.toastr.success(this.appendResponseMessage(message, data), title, {enableHTML: true});
		//this.toastr.success(message, title);
		this.$log.info('Success: ' + message, '\nSummary:', title, '\nDetails:', data);
	}

	warning(message:string, data?:{}, title?:string) {
		this.toastr.warning(this.appendResponseMessage(message, data), title, {enableHTML: true});
		//this.toastr.warning(message, title);
		this.$log.warn('Warning: ' + message, '\nSummary:', title, '\nDetails:', data);
	}

	/**
	 * if logging an error in response to an HTTP error, the data object will be a Response
	 * containing a _body property, which is a JSON string containing the error message
	 * returned from the server. If we've got one of these, append it to the message to be
	 * displayed in the toastr
     */
	private appendResponseMessage(message:string, data?:{}) : string {

		if (data
			&& '_body' in data) {

			var body = data['_body'];
			if (body) {
				var bodyObj = JSON.parse(body);
				if ('message' in bodyObj) {
					message += ':<br/>';
					message += bodyObj['message'];

				}
			}
		}

		return message;
	}
}
