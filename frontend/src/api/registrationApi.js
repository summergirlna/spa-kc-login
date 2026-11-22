import {postJson} from './http'

export function startRegistration(email) {
    return postJson('/api/registration/email', {
        email
    })
}

export function resendRegistrationCode(registrationId) {
    return postJson('/api/registration/resend-code', {
        registrationId
    })
}

export function confirmRegistrationCode(registrationId, code) {
    return postJson('/api/registration/confirm-code', {
        registrationId,
        code
    })
}

export function completeRegistration(registrationId, password, mfaType) {
    return postJson('/api/registration/complete', {
        registrationId,
        password,
        mfaType
    })
}