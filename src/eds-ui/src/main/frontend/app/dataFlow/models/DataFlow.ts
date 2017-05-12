export class DataFlow {
    uuid : string;
    name: string;
    status : string;
    directionId : number;
    flowScheduleId : number;
    approximateVolume : number;
    dataExchangeMethodId : number;
    flowStatusId : number;
    additionalDocumentation : string;
    signOff : string;
    dataSet : string;
    cohort : string;
    subscriber : string;
    dsas : { [key:string]:string; };
    dpas : { [key:string]:string; };

    getDisplayItems() :any[] {
        return [
            {label: 'Status', property: 'status'},
            {label: 'Approximate Volume', property: 'approximateVolume'},
            {label: 'Sign Off', property: 'signOff'},
        ];
    }
}
