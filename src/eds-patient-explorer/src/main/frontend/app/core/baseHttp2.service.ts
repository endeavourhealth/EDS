import {Http, Response, RequestOptionsArgs} from "@angular/http";
import {Observable} from "rxjs";

export class BaseHttp2Service {
	constructor(private http : Http) { }

	httpGet(url : string, options? : RequestOptionsArgs) : Observable<any> {
		return this.http.get(url, options)
			.map((res:Response) => {
				if (res.headers.get('content-type') == 'text/plain')
					return res.text() ? res.text() : null;
				else
					return res.text() ? res.json() : null;
			})
			.catch(this.handleError);
	}

	httpPost(url : string, body? : any, options? : RequestOptionsArgs) : Observable<any> {
		return this.http.post(url, body, options)
			.map((res:Response) => {
				if (res.headers.get('content-type') == 'text/plain')
					return res.text() ? res.text() : null;
				else
					return res.text() ? res.json() : null;
			})
			.catch(this.handleError);
	}

	httpDelete(url : string, options? : RequestOptionsArgs) : Observable<any> {
		return this.http.delete(url, options)
			.map((res:Response) => {
				if (res.headers.get('content-type') == 'text/plain')
					return res.text() ? res.text() : null;
				else
					return res.text() ? res.json() : null;
			})
			.catch(this.handleError);
	}

	private handleError(error: any): Promise<any> {
		console.error('An error occurred', error);
		return Promise.reject(error.message || error);
	}
}