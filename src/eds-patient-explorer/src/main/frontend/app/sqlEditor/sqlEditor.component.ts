import {Component, OnInit, ViewChild, ElementRef} from "@angular/core";
import {LoggerService} from "../common/logger.service";
import {SqlEditorService} from "./sqlEditor.service";
import {TableMeta} from "./models/TableMeta";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {UploadCsvDialog} from "./uploadCsv.dialog";

declare let CodeMirror : any;

@Component({
	template : require('./sqlEditor.html')
})
export class SqlEditorComponent implements OnInit {

	@ViewChild('sql') el:ElementRef;
	private editor : any;
	private resultData : string[][];
	private running : boolean;

	constructor(
		protected $modal : NgbModal,
		protected logger : LoggerService,
		protected sqlEditorService : SqlEditorService) {
	}

	ngOnInit(): void {
		let vm = this;
		vm.editor = CodeMirror.fromTextArea(vm.el.nativeElement, {
			mode: 'text/x-mysql',
			indentWithTabs: true,
			smartIndent: true,
			lineNumbers: true,
			matchBrackets : true,
			autofocus: true,
			extraKeys: {"Ctrl-Space": "autocomplete"}
		});
		vm.sqlEditorService.getTableData().subscribe(
			(tableData) => {
				vm.updateOptions(tableData);
				vm.editor.setValue("");
			}
		);
	}

	private refreshMetaData() {
		let vm = this;
		vm.sqlEditorService.getTableData().subscribe(
			(tableData) => vm.updateOptions(tableData)
		);
	}

	private updateOptions(tableData : TableMeta[]) {
		let hintOptions = { tables : {} };

		for (let t = 0; t < tableData.length; t++) {
			let table: TableMeta = tableData[t];
			hintOptions.tables[table.name] = table.fields.map(f => f.name);
		}

		this.editor.setOption("hintOptions", hintOptions);
	}

	private getHeaders() {
		if (this.resultData)
			return this.resultData[0];
		else
			return [];
	}

	private getRows() {
		if (this.resultData)
			return this.resultData.slice(1);
		else
			return [];
	}

	private executeQuery() {
		let vm = this;
		let sql = vm.editor.getValue();
		vm.running = false;
		vm.resultData = null;
		vm.sqlEditorService.runQuery(sql).subscribe(
			(result) => {
				vm.logger.success('SQL executed');
				vm.resultData = result;
				vm.running = false;
				vm.refreshMetaData();
			},
			(error) => vm.logger.error(error._body, error, error.statusText)
		);
	}

	private importData() {
		let vm = this;
		UploadCsvDialog.open(vm.$modal).result.then(
			(result) => { if (result) vm.editor.setValue(result); },
			(error) => vm.logger.error("CSV upload failed", error)
		);
	}

	private exportResults() {
			let data = this.resultData.map(
				row => row.map(field => {
					if (field)
						return '"' + field.replace('"','""') + '"';
					else
						return 'NULL';
				})
					.join(',')
			).join('\n');
			let blob = new Blob([data], { type: 'text/plain' });
			window['saveAs'](blob, 'results.csv');
	}
}
