module app.models {
    'use strict';

    export class ServiceContract {
        type : string;
        service : Service;
        system : System;
        technicalInterface : TechnicalInterface;
        active : string;
    }
}