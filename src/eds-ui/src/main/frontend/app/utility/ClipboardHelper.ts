import {LoggerService} from "eds-common-js/dist/index";

export class ClipboardHelper {

    static copyBodyToClipboard(toCopy: string, logger : LoggerService) {

        //create a text area containing the text and insert into the document
        var txtArea = document.createElement("textarea");
        txtArea.style.position = 'fixed';
        txtArea.value = toCopy;
        document.body.appendChild(txtArea);
        txtArea.select();

        //invoke the copy action on the text area
        try {
            var successful = document.execCommand('copy');
            if (successful) {
                logger.success('Copied to clipboard');
            } else {
                logger.error('Failed to copy to clipboard');
            }

        } catch (err) {
            console.log('Failed to copy text to clipboard');
            console.log(err);

            logger.error('Failed to copy to clipboard', err, 'Copy');
        }

        //remove from the document
        document.body.removeChild(txtArea);
    }
}