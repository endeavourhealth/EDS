import {Logger} from "angular2-logger/core";
import {Injectable} from "@angular/core";
import {ToastsManager} from "ng2-toastr";

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
		this.toastr.error(message, title);
		this.$log.error('Error: ' + message, '\nSummary:', title, '\nDetails:', data);
	}

	info(message:string, data?:{}, title?:string) {
		this.toastr.info(message, title);
		this.$log.info('Info: ' + message, '\nSummary:', title, '\nDetails:', data);
	}

	success(message:string, data?:{}, title?:string) {
		this.toastr.success(message, title);
		this.$log.info('Success: ' + message, '\nSummary:', title, '\nDetails:', data);
	}

	warning(message:string, data?:{}, title?:string) {
		this.toastr.warning(message, title);
		this.$log.warn('Warning: ' + message, '\nSummary:', title, '\nDetails:', data);
	}
}
