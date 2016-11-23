import {TechnicalInterface} from "./TechnicalInterface";
import {System} from "./System";
import {Service} from "./Service";

export class ServiceContract {
    type : string;
    service : Service;
    system : System;
    technicalInterface : TechnicalInterface;
    active : string;
}
