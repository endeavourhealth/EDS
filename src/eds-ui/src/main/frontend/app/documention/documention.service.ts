import {Injectable} from "@angular/core";
import {URLSearchParams, Http} from "@angular/http";
import {Observable} from "rxjs";
import {BaseHttp2Service} from "eds-common-js";
import {Documentation} from "./models/Documentation";

@Injectable()
export class DocumentationService extends BaseHttp2Service  {
    constructor(http : Http) { super (http); }

    getDocument(uuid : string): Observable<Documentation> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpGet('api/documentation', { search : params });
    }

    getAllAssociatedDocuments(parentUuid : string, parentType : string ) : Observable<Documentation[]> {
        let params = new URLSearchParams();
        params.set('parentUuid',parentUuid);
        params.set('parentType',parentType);
        return this.httpGet('api/documentation/associated', { search : params });
    }

    deleteDocument(uuid : string) : Observable<any> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpDelete('api/documentation', { search : params });
    }
}
