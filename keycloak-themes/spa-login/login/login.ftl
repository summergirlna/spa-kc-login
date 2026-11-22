<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','password'); section>
    <#if section = "header">
        ${msg("loginAccountTitle")}
    <#elseif section = "form">
        <form id="kc-form-login" action="${url.loginAction}" method="post">
            <div class="${properties.kcFormGroupClass!}">
                <label for="username" class="${properties.kcLabelClass!}">
                    <#if !realm.loginWithEmailAllowed>
                        ${msg("username")}
                    <#elseif !realm.registrationEmailAsUsername>
                        ${msg("usernameOrEmail")}
                    <#else>
                        ${msg("email")}
                    </#if>
                </label>

                <input
                        id="username"
                        name="username"
                        type="text"
                        value="${(login.username!'')}"
                        autocomplete="username"
                        autofocus
                        class="${properties.kcInputClass!}"
                />
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <label for="password" class="${properties.kcLabelClass!}">
                    ${msg("password")}
                </label>

                <input
                        id="password"
                        name="password"
                        type="password"
                        autocomplete="current-password"
                        class="${properties.kcInputClass!}"
                />
            </div>

            <#if realm.rememberMe && !usernameHidden??>
                <div class="${properties.kcFormGroupClass!}">
                    <label>
                        <input
                                id="rememberMe"
                                name="rememberMe"
                                type="checkbox"
                                <#if login.rememberMe??>checked</#if>
                        />
                        ${msg("rememberMe")}
                    </label>
                </div>
            </#if>

            <#if credentialId??>
                <input type="hidden" name="credentialId" value="${credentialId}"/>
            </#if>

            <div class="${properties.kcFormGroupClass!}">
                <input
                        class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                        type="submit"
                        value="${msg("doLogIn")}"
                />
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <a href="http://localhost/">
                    Back to application
                </a>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>