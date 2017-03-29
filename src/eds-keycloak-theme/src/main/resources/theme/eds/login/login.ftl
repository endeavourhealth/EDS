<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=social.displayInfo; section>
    <#if section = "title">
        ${msg("loginTitle",(realm.displayName!''))}
    <#elseif section = "header">
        ${msg("loginTitleHtml",(realm.displayNameHtml!''))}
    <#elseif section = "form">
        <#if realm.password>

        <form id="kc-form-login" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">

                    <div class="${properties.kcFormGroupClass!}">
                        <div class="input-group">
                            <span class="input-group-addon">
                                <i class="fa fa-user" id="username" aria-hidden="true"></i>
                            </span>
                            <#if usernameEditDisabled??>
                                <input id="username" class="${properties.kcInputClass!}" name="username" value="${(login.username!'')?html}" type="text" placeholder="${msg("username")}" disabled />
                            <#else>
                                <input id="username" class="${properties.kcInputClass!}" name="username" value="${(login.username!'')?html}" type="text" placeholder="${msg("username")}" autofocus />
                            </#if>
                        </div>
                    </div>

                    <div class="${properties.kcFormGroupClass!}">

                        <div class="input-group">
                            <span class="input-group-addon">
                                <i class="fa fa-lock" id="password" aria-hidden="true"></i>
                            </span>
                            <input id="password" class="${properties.kcInputClass!}" name="password" type="password" autocomplete="off" placeholder="${msg("password")}" />
                        </div>
                    </div>

                    <div class="${properties.kcFormGroupClass!}">

                        <div id="kc-form-buttons">
                            <div class="${properties.kcFormButtonsWrapperClass!}">
                                <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}" name="login" id="kc-login" type="submit" value="${msg("doLogIn")}"/>
                            </div>
                        </div>

                        <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                            <#if realm.rememberMe && !usernameEditDisabled??>
                                <div class="checkbox">
                                    <label>
                                        <#if login.rememberMe??>
                                            <input id="rememberMe" name="rememberMe" type="checkbox" tabindex="3" checked> ${msg("rememberMe")}
                                        <#else>
                                            <input id="rememberMe" name="rememberMe" type="checkbox" tabindex="3"> ${msg("rememberMe")}
                                        </#if>
                                    </label>
                                </div>
                            </#if>
                            <div class="${properties.kcFormOptionsWrapperClass!}">
                                <#if realm.resetPasswordAllowed>
                                    <span><a class="kc-forgot-password" href="${url.loginResetCredentialsUrl}">${msg("doForgotPassword")}</a></span>
                                </#if>
                            </div>
                        </div>

                    </div>
                </div>
            </div>
        </form>
        </#if>
    <#elseif section = "social" >
        <#if realm.password && social.providers??>
            <div class="container">
                <div id="kc-social-providers" class="col-md-4 col-md-offset-4">
                    <div id="kc-social-providers-prompt">
                    or login using one of the following:
                    </div>
                    <ul>
                        <#list social.providers as p>
                            <li><a href="${p.loginUrl}" id="zocial-${p.alias}" class="zocial ${p.providerId}"> <span class="text">${p.alias}</span></a></li>
                        </#list>
                    </ul>
                </div>
            </div>
        </#if>

    <#elseif section = "registration" >
        <#if realm.password && realm.registrationAllowed && !usernameEditDisabled??>
            <div class="container">
                <div id="kc-registration">
                    <span>${msg("noAccount")} <a href="${url.registrationUrl}">${msg("doRegister")}</a></span>
                </div>
            </div>
        </#if>        
    </#if>
</@layout.registrationLayout>
