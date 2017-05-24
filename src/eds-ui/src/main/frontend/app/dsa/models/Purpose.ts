export class Purpose {
    uuid: string;
    title: string;
    detail: string;

    getDisplayItems() :any[] {
        return [
            {label: 'Detail', property: 'detail'},
        ];
    }
}