import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {ScheduledTasksComponent} from "./scheduledTasks.component";
import {ScheduledTasksService} from "./scheduledTasks.service";
import {ScheduledTaskHistoryDialog} from "./scheduledTaskHistory.dialog";

@NgModule({
    imports : [
        BrowserModule,
        FormsModule,
        NgbModule
    ],
    declarations : [
        ScheduledTasksComponent,
        ScheduledTaskHistoryDialog
    ],
    entryComponents : [
        ScheduledTaskHistoryDialog
    ],
    providers : [
        ScheduledTasksService
    ]
})
export class ScheduledTasksModule {}