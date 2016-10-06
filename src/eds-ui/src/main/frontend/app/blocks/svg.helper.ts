export interface SvgElement {
    getScreenCTM();
    createSVGPoint();
}

export class SvgHelper {
    public static removeClassSVG(obj, remove) {
        var classes = obj.attr('class');
        if (!classes) {
            return false;
        }

        var index = classes.search(remove);

        // if the class already doesn't exist, return false now
        if (index == -1) {
            return false;
        }
        else {
            // string manipulation to remove the class
            classes = classes.substring(0, index) + classes.substring((index + remove.length), classes.length);

            // set the new string as the object's class
            obj.attr('class', classes);

            return true;
        }
    };

    public static hasClassSVG(obj, has) {
        var classes = obj.attr('class');
        if (!classes) {
            return false;
        }

        var index = classes.search(has);

        if (index == -1) {
            return false;
        }
        else {
            return true;
        }
    };
}
