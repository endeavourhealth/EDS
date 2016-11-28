import {Component, Input, ViewChild, OnChanges} from "@angular/core";
import {UIEncounter} from "../models/resources/clinical/UIEncounter";
import {TreeNode} from "../../common/models/TreeNode";
import moment = require("moment");
import Moment = moment.Moment;
import {TreeComponent} from "angular2-tree-component";
import {linq} from "../../common/linq";

@Component({
	selector : 'consultations',
	template : require('./consultations.html')
})
export class ConsultationsComponent implements OnChanges {
	@Input() consultations : UIEncounter[] = [];
	@ViewChild(TreeComponent) tree: TreeComponent;
	dateTreeData : TreeNode[] = [];
	filteredConsultations : UIEncounter[] = [];

	ngOnChanges(): void {
		// Rebuild date tree
		if (this.consultations) {
			this.consultations = linq(this.consultations)
				.OrderByDescending(c => c.period.start.date)
				.ToArray();

			this.dateTreeData = [
				{
					id : 0,
					title : 'All',
					data : this.consultations,
					children : []
				}
			]

			for (let encounter of this.consultations) {
				let encounterDate: Moment = moment(encounter.period.start.date);
				this.getDateNode(encounterDate).data.push(encounter);
			}

			this.tree.treeModel.update();
		}
	}

	onInitialized() {
		// Check for and restore saved filter state
		this.onUpdateData();
	}

	onUpdateData() {
		let node = this.tree.treeModel.getFirstRoot();
		if (node)
			node.setActiveAndVisible();
	}

	selectDate(selection) {
		this.filteredConsultations = [];
		this.addNodeData(selection);
	}

	addNodeData(node : TreeNode) {
		if (node.data && node.data.length > 0)
			this.filteredConsultations = this.filteredConsultations.concat(node.data);

		if (node.children){
			for(let childNode of node.children)
				this.addNodeData(childNode);
		}
	}

	getDateNode(moment : Moment) : TreeNode {
		let monthNode : TreeNode = this.getMonthNode(moment);

		let dateNode : TreeNode = monthNode.children.find((e) => { return e.title === moment.format("Do"); });
		if (!dateNode) {
			dateNode = {
				id : moment.milliseconds(),
				title : moment.format("Do"),
				data : [],
				children : []
			};
			monthNode.children.push(dateNode);
		}

		return dateNode;
	}

	getMonthNode(moment : Moment) : TreeNode {
		let yearNode : TreeNode = this.getYearNode(moment);

		let monthNode : TreeNode = yearNode.children.find( (e) => { return e.title === moment.format("MMM"); });
		if (!monthNode) {
			monthNode = {
				id : moment.milliseconds(),
				title : moment.format("MMM"),
				data : [],
				children : []
			};
			yearNode.children.push(monthNode);
		}

		return monthNode;
	}

	getYearNode(moment : Moment) : TreeNode {
		let yearNode : TreeNode = this.dateTreeData.find( (e) => { return e.title === moment.format("YYYY"); });

		if (!yearNode) {
			yearNode = {
				id : moment.milliseconds(),
				title : moment.format("YYYY"),
				data : [],
				children : []
			};
			this.dateTreeData.push(yearNode);
		}

		return yearNode;
	}

}