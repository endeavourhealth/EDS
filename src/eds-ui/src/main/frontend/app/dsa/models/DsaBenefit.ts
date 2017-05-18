export class DsaBenefit {
    uuid: string;
    name: string;
    title: string;
    detail: string;

    getDisplayItems() :any[] {
        return [
            {label: 'Detail', property: 'detail'},
        ];
    }
}