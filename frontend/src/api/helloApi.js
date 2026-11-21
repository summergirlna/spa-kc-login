import {getJson} from './http'

export function fetchHello(token) {
    return getJson('/api/hello', {
        headers: {
            Authorization: `Bearer ${token}`
        }
    })
}