import {Http, Response, RequestOptionsArgs} from "@angular/http";
import {Observable} from "rxjs";

export class BaseHttp2Service {
	constructor(private http : Http) { }

	httpGet(url : string, options? : RequestOptionsArgs) : Observable<any> {
		return this.http.get(url, options)
			.map((res:Response) => {
				return res.json();
			})
			.catch(this.handleError);
	}

	httpPost(url : string, options? : RequestOptionsArgs) : Observable<any> {
		return this.http.post(url, options)
			.map((res:Response) => {
				return res.json();
			})
			.catch(this.handleError);
	}

	private handleError(error: any): Promise<any> {
		console.error('An error occurred', error);
		return Promise.reject(error.message || error);
	}
}