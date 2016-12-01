import {AuthConfig} from "./AuthConfig";

export class WellKnownConfig {

    private authConfig:AuthConfig;

    private static instance:WellKnownConfig;

    // singleton instance
    public static factory():WellKnownConfig {
        if(WellKnownConfig.instance == null) {
            WellKnownConfig.instance = new WellKnownConfig();
        }
        return WellKnownConfig.instance;
    }

    constructor() {

    }

    public getAuthConfig():AuthConfig {
        return this.authConfig;
    }

    public setAuthConfig(authConfig:AuthConfig) {
        this.authConfig = authConfig;
    }
}

