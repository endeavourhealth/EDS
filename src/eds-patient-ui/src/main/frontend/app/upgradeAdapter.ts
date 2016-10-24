import {UpgradeAdapter} from '@angular/upgrade';
import {forwardRef} from "@angular/core";
import {AppModule} from "./main";

export const upgradeAdapter = new UpgradeAdapter(forwardRef(() => AppModule));