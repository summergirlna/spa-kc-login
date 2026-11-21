import Keycloak from "keycloak-js";

const keycloak = new Keycloak({
    url: `${window.location.origin}/auth`,
    realm: 'spa-kc-login',
    clientId: 'spa-client'
})

let initPromise = null

export function initKeycloak() {
    if (!initPromise) {
        initPromise = keycloak.init({
            pkceMethod: 'S256',
            checkLoginIframe: false
        })
    }

    return initPromise
}

export default keycloak