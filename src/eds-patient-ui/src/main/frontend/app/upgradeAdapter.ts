import {UpgradeAdapter} from '@angular/upgrade';
import {AppModule} from "./app.module";
import {forwardRef} from "@angular/core";

export const upgradeAdapter = new UpgradeAdapter(forwardRef(() => AppModule));