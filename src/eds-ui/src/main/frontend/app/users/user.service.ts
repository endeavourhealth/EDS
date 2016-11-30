import {Injectable} from "@angular/core";
import {User} from "./models/User";
import {Observable} from "rxjs";

@Injectable()
export class UserService {
	saveUser(editedUser: User) : Observable<any> {
		return Observable.create();
	}

	getUserList()  : Observable<any> {
		return Observable.create();
	}
}