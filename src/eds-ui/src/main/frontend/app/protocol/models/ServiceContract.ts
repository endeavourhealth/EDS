import {TechnicalInterface} from "../../system/models/TechnicalInterface";
import {System} from "../../system/models/System";
import {Service} from "../../services/models/Service";

export class ServiceContract {
    type : string;
    service : Service;
    system : System;
    technicalInterface : TechnicalInterface;
    active : string;
    definesCohort: boolean;
}
