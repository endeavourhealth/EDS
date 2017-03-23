import {Component} from "@angular/core";
import {NgbModal, NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {Headers, RequestOptions, URLSearchParams} from "@angular/http";
import {SqlEditorService} from "./sqlEditor.service";

@Component({
    selector: 'ngbd-modal-content',
    template: require('./uploadCsv.html')
})
export class UploadCsvDialog {
    public static open(modalService: NgbModal) {
        return modalService.open(UploadCsvDialog, {backdrop: "static"});
    }

    private tableName : string;
    private file : File;

    constructor(protected activeModal: NgbActiveModal,
                protected sqlEditorService : SqlEditorService) {
    }

    fileChange(event) {
        let fileList: FileList = event.target.files;
        if(fileList.length > 0)
            this.file = fileList[0];
        else
            this.file = null;
    }

    private uploadFile() {
        let vm = this;
        let myReader:FileReader = new FileReader();

        myReader.onloadend = function(e){
            vm.generateCreateTableStatement(myReader.result);
        }

        myReader.readAsText(vm.file);
    }

    private generateCreateTableStatement(csvData) {
        let rows = csvData.split('\n');
        let fieldList = rows[0].replace(/"/g,'');
        let fields = fieldList.split(',');

        let sql = '-- DROP TABLE IF EXISTS workspace.' + this.tableName + ';\n\n';
        sql += 'CREATE TABLE workspace.' + this.tableName + '(\n';

        for (let i = 0; i < fields.length; i++) {
            sql += '\t' + fields[i] + ' CHARACTER VARYING(255),\n';
        }
        sql += '\tCONSTRAINT pk_'+this.tableName+'_'+fields[0]+' PRIMARY KEY ('+fields[0]+')\n';
        sql += ');\n\n';

        sql += 'INSERT INTO workspace.' + this.tableName + '\n';
        sql += '(' + fieldList + ')\n';
        sql += 'VALUES';

        for (let i = 1; i < rows.length; i++) {
            if (i > 1)
                sql += ',';
            sql += '\n(' + rows[i].replace(/"/g,"'") + ')';
        }
        sql += ';';

        this.activeModal.close(sql);
    }

    ok() {
        this.uploadFile();
    }

    cancel() {
        this.activeModal.close(null);
    }
}
