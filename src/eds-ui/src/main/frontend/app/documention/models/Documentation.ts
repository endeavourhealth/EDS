export class Documentation {
    uuid : string;
    title : string;
    filename : string;
    fileData : string;

    getDisplayItems() :any[] {
        return [
            {label: 'Title', property: 'title'},
            {label: 'Filename', property: 'filename'},
            {label: 'fileData', property: 'fileData', document : true},
        ];
    }
}
