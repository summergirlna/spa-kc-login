<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('otp'); section>
    <#if section = "header">
        Email verification
    <#elseif section = "form">
        <form id="kc-email-otp-form" action="${url.loginAction}" method="post">
            <div class="${properties.kcFormGroupClass!}">
                <label for="otp" class="${properties.kcLabelClass!}">
                    Verification code
                </label>

                <input
                        id="otp"
                        name="otp"
                        type="text"
                        autocomplete="one-time-code"
                        autofocus
                        class="${properties.kcInputClass!}"
                />
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <input
                        class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                        type="submit"
                        value="Verify"
                />
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <a href="${url.loginRestartFlowUrl}">
                    Back to login
                </a>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>