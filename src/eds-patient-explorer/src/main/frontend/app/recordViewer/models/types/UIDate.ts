export class UIDate {
    date: Date;
    precision: string;

    constructor() {}

    public static parse(date : string) : UIDate {
        return UIDate.fromDate(Date.parse(date));
    }

    public static fromDate(date : number) : UIDate {
        let uiDate = new UIDate();
        uiDate.date = new Date();
        uiDate.date.setTime(date);
        return uiDate;
    }
}
