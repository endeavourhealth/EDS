export class DateTimeFormatter {




    static formatYYYYMMDD(val: number | string): string {
        if (!val) {
            return '';
        }

        var d = null;
        if (typeof val === "number") {
            d = new Date(val as number);

        } else {
            d = new Date(val as string);
        }

        return d.getFullYear() + '-' + ('0'+(d.getMonth()+1)).slice(-2) + '-' + ('0' + d.getDate()).slice(-2);
    }

    /**
     * SD-338 - Angular2 "date pipe" issue formats 00:00 as 24:00 so use a function to format times instead
     */
    static formatYYYYMMDDHHMM(val: number | string): string {
        if (!val) {
            return '';
        }

        var d = null;
        if (typeof val === "number") {
            d = new Date(val as number);

        } else {
            d = new Date(val as string);
        }

        return d.getFullYear() + '-' + ('0'+(d.getMonth()+1)).slice(-2) + '-' + ('0' + d.getDate()).slice(-2)
            + ' ' + ('0' + d.getHours()).slice(-2) + ':' + ('0' + d.getMinutes()).slice(-2);
    }

    /**
     * SD-338 - Angular2 "date pipe" issue formats 00:00 as 24:00 so use a function to format times instead
     */
    static formatYYYYMMDDHHMMSS(val: number | string): string {
        if (!val) {
            return '';
        }

        var d = null;
        if (typeof val === "number") {
            d = new Date(val as number);

        } else {
            d = new Date(val as string);
        }

        return d.getFullYear() + '-' + ('0'+(d.getMonth()+1)).slice(-2) + '-' + ('0' + d.getDate()).slice(-2)
            + ' ' + ('0' + d.getHours()).slice(-2) + ':' + ('0' + d.getMinutes()).slice(-2) + ':' + ('0' + d.getSeconds()).slice(-2);
    }


    /**
     * SD-338 - Angular2 "date pipe" issue formats 00:00 as 24:00 so use a function to format times instead
     */
    static formatHHMM(val: number | string): string {
        if (!val) {
            return '';
        }

        var d = null;
        if (typeof val === "number") {
            d = new Date(val as number);

        } else {
            d = new Date(val as string);
        }

        return ('0' + d.getHours()).slice(-2) + ':' + ('0' + d.getMinutes()).slice(-2);
    }

    /**
     * SD-338 - Angular2 "date pipe" issue formats 00:00 as 24:00 so use a function to format times instead
     */
    static formatHHMMSS(val: number | string): string {
        if (!val) {
            return '';
        }

        var d = null;
        if (typeof val === "number") {
            d = new Date(val as number);

        } else {
            d = new Date(val as string);
        }

        return ('0' + d.getHours()).slice(-2) + ':' + ('0' + d.getMinutes()).slice(-2) + ':' + ('0' + d.getSeconds()).slice(-2);
    }



    static getDateDiffDescMs(earlier: number, later: number, numToks: number): string {
        var from = new Date();
        from.setTime(earlier);
        var to = new Date();
        to.setTime(later);
        return DateTimeFormatter.getDateDiffDesc(from, to, numToks);
    }

    static getDateDiffDesc(earlier: Date, later: Date, numToks: number): string {

        //optionalArg = (typeof optionalArg === 'undefined') ? 'default' : optionalArg;

        var diffMs = later.getTime() - earlier.getTime();

        var durSec = 1000;
        var durMin = durSec * 60;
        var durHour = durMin * 60;
        var durDay = durHour * 25;
        var durWeek = durDay * 7;
        var durYear = durDay * 365.25;

        var toks = [];

        if (toks.length < numToks) {
            var years = Math.floor(diffMs / durYear);
            if (years > 0) {
                toks.push('' + years + 'y');
                diffMs -= years * durYear;
            }
        }

        if (toks.length < numToks) {
            var weeks = Math.floor(diffMs / durWeek);
            if (weeks > 0) {
                toks.push('' + weeks + 'w');
                diffMs -= weeks * durWeek;
            }
        }

        if (toks.length < numToks) {
            var days = Math.floor(diffMs / durDay);
            if (days > 0) {
                toks.push('' + days + 'd');
                diffMs -= days * durDay;
            }
        }

        if (toks.length < numToks) {
            var hours = Math.floor(diffMs / durHour);
            if (hours > 0) {
                toks.push('' + hours + 'h');
                diffMs -= hours * durHour;
            }
        }

        if (toks.length < numToks) {
            var mins = Math.floor(diffMs / durMin);
            if (mins > 0 ) {
                toks.push('' + mins + 'm');
                diffMs -= mins * durMin;
            }
        }

        if (toks.length < numToks) {
            var secs = Math.floor(diffMs / durSec);
            if (secs > 0 ) {
                toks.push('' + secs + 's');
                diffMs -= secs * durSec;
            }
        }

        //we get errors when expressions seem to change rapidly, and if we're factoring in millis, then this happens a lot
        /*if (toks.length < numToks) {
            if (diffMs > 0) {
                toks.push('' + diffMs + 'ms');
            }
        }*/

        if (toks.length == 0) {
            toks.push('0s');
        }

        return toks.join(' ');
    }

    static getDateDiffSimpleFromNow(earlier: number | Date): string {

        if (!earlier) {
            return '';
        }

        var dEarlier = null;
        if (typeof earlier === "number") {
            dEarlier = new Date(earlier as number);
        } else {
            dEarlier = earlier;
        }

        var dLater = new Date();

        var diffMs = dLater.getTime() - dEarlier.getTime();
        var durSec = 1000;
        var durMin = durSec * 60;
        if (diffMs < durMin) {
            return '(just now)';
        } else {
            return '(' + DateTimeFormatter.getDateDiffDesc(dEarlier, dLater, 1) + ' ago)';
        }
    }

    static getDateDiffSimple(earlier: number | Date, later: number | Date): string {

        if (!earlier || !later) {
            return '';
        }

        var dEarlier = null;
        if (typeof earlier === "number") {
            dEarlier = new Date(earlier as number);
        } else {
            dEarlier = earlier;
        }

        var dLater = null;
        if (typeof later === "number") {
            dLater = new Date(later as number);
        } else {
            dLater = later;
        }

        return DateTimeFormatter.getDateDiffDesc(dEarlier, dLater, 2);
    }

}