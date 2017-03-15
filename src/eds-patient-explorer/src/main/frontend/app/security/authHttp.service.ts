import {Injectable} from "@angular/core";
import {Http, ConnectionBackend, RequestOptions, Request, RequestOptionsArgs, Response, Headers} from "@angular/http";
import {Observable} from "rxjs";
import {Auth} from "./security.auth";
export const JWT_RESPONSE_HEADER = 'X-Auth-Token';

@Injectable()
export class AuthHttpService extends Http {
	constructor(backend: ConnectionBackend, defaultOptions: RequestOptions) {
		super(backend, defaultOptions);
	}

	request(url: string | Request, options?: RequestOptionsArgs): Observable<Response> {
		const request = super.request(url, this.appendAuthHeader(options));
		request.map(this.saveToken);
		return request;
	}

	get(url: string, options?: RequestOptionsArgs): Observable<Response> {
		const request = super.get(url, this.appendAuthHeader(options));
		request.map(this.saveToken);
		return request;
	}

	post(url: string, body: any, options?: RequestOptionsArgs): Observable<Response> {
		const request = super.post(url, body, this.appendAuthHeader(options));
		request.map(this.saveToken);
		return request;
	}

	put(url: string, body: any, options?: RequestOptionsArgs): Observable<Response> {
		const request = super.put(url, body, this.appendAuthHeader(options));
		request.subscribe(this.saveToken);
		return request;
	}

	delete(url: string, options?: RequestOptionsArgs): Observable<Response> {
		const request = super.delete(url, this.appendAuthHeader(options));
		request.map(this.saveToken);
		return request;
	}

	private appendAuthHeader(options?: RequestOptionsArgs): RequestOptionsArgs {
		let mergedOptions: RequestOptionsArgs;
		if (!options) {
			mergedOptions = { };
		} else {
			mergedOptions = options;
		}

		if (!mergedOptions.headers)
			mergedOptions.headers = new Headers();

		if (mergedOptions.withCredentials == null)
			mergedOptions.withCredentials = true;

		if (mergedOptions.withCredentials) {
			var authz = Auth.factory().getAuthz();
			if (authz != null && authz.token) {
				const token = authz.token;
				if (token) mergedOptions.headers.append('Authorization', `Bearer ${token}`);
			}
		}
		return mergedOptions;
	}

	private saveToken(res: Response): void {
		const token = res.headers.get(JWT_RESPONSE_HEADER);
		if (token) localStorage.setItem(JWT_RESPONSE_HEADER, token);
	}
}