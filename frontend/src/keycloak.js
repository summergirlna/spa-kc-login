import Keycloak from "keycloak-js";

const keycloak = new Keycloak({
    url: 'http://localhost:8180',
    realm: 'spa-kc-login',
    clientId: 'spa-client'
})

let initPromise = null

export function initKeycloak() {
    if (!initPromise) {
        initPromise = keycloak.init({
            onLoad: 'check-sso',
            pkceMethod: 'S256'
        })
    }

    return initPromise
}

export default keycloak