import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {ScheduledTasksComponent} from "./scheduledTasks.component";
import {ScheduledTasksService} from "./scheduledTasks.service";

@NgModule({
    imports : [
        BrowserModule,
        FormsModule,
        NgbModule
    ],
    declarations : [
        ScheduledTasksComponent
    ],
    providers : [
        ScheduledTasksService
    ]
})
export class ScheduledTasksModule {}