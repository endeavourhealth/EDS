import {TechnicalInterface} from "../../systems/models/TechnicalInterface";
import {System} from "../../systems/models/System";
import {Service} from "../../services/models/Service";

export class ServiceContract {
    type : string;
    service : Service;
    system : System;
    technicalInterface : TechnicalInterface;
    active : string;
}
