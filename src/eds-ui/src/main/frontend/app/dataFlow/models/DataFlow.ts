export class DataFlow {
    uuid : string;
    name: string;
    directionId : number;
    flowScheduleId : number;
    approximateVolume : number;
    dataExchangeMethodId : number;
    storageProtocolId : number;
    securityInfrastructureId : number;
    securityArchitectureId : number;
    flowStatusId : number;
    additionalDocumentation : string;
    signOff : string;
    dsas : { [key:string]:string; };
    dpas : { [key:string]:string; };

    getDisplayItems() :any[] {
        return [
            {label: 'Status', property: 'status'},
            {label: 'Approximate Volume', property: 'approximateVolume'},
            {label: 'Sign Off', property: 'signOff'}
        ];
    }
}
