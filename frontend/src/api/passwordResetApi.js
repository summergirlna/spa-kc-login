import {postJson} from './http'

export function startPasswordReset(email) {
    return postJson('/api/password-reset/email', {
        email
    })
}

export function confirmPasswordResetCode(resetId, code) {
    return postJson('/api/password-reset/confirm-code', {
        resetId,
        code
    })
}

export function completePasswordReset(resetId, password) {
    return postJson('/api/password-reset/complete', {
        resetId,
        password
    })
}