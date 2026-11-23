import {postJson} from './http'

export function startEmailChange(token, newEmail) {
    return postJson('/api/account/email-change/start', {
        newEmail
    }, {
        headers: {
            Authorization: `Bearer ${token}`
        }
    })
}

export function confirmEmailChange(token, changeId, code) {
    return postJson('/api/account/email-change/confirm', {
        changeId,
        code
    }, {
        headers: {
            Authorization: `Bearer ${token}`
        }
    })
}