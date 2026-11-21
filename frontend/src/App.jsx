import {useEffect, useState} from 'react'
import './App.css'
import keycloak, {initKeycloak} from './keycloak'
import {fetchHello} from "./api/helloApi.js";
import {LoginSection} from "./features/auth/LoginSection.jsx";
import {RegistrationFlow} from "./features/registration/RegistrationFlow.jsx";
import {AuthenticatedSection} from "./features/auth/AuthenticatedSection.jsx";

function App() {
    const [screen, setScreen] = useState('login')
    const [message, setMessage] = useState('')
    const [loading, setLoading] = useState(true)
    const [apiError, setApiError] = useState('')
    const [authError, setAuthError] = useState('')
    const [keycloakReady, setKeycloakReady] = useState(false)
    const [authenticated, setAuthenticated] = useState(false)
    const [username, setUsername] = useState('')

    const loadHello = () => {
        setLoading(true)
        setApiError('')

        fetchHello(keycloak.token)
            .then((data) => {
                setMessage(data.message)
            })
            .catch((error) => {
                setApiError(error.message)
            })
            .finally(() => {
                setLoading(false)
            })
    }

    useEffect(() => {
        initKeycloak()
            .then((authenticated) => {
                setAuthenticated(authenticated)

                if (authenticated) {
                    setScreen('authenticated')
                    setUsername(
                        keycloak.tokenParsed?.preferred_username
                        ?? keycloak.tokenParsed?.sub
                        ?? ''
                    )

                    loadHello()
                }
            })
            .catch((error) => {
                console.error('Failed to initialize Keycloak', error)
                setAuthError('Failed to initialize Keycloak')
            })
            .finally(() => {
                setKeycloakReady(true)
            })
    }, [])

    const login = () => {
        keycloak.login()
    }

    const logout = () => {
        keycloak.logout({
            redirectUri: 'http://localhost'
        })
    }

    return (
        <main>
            <h1>SPA + Spring Boot + Keycloak Training</h1>

            {!keycloakReady && <p>Initializing Keycloak...</p>}

            {authError && (
                <p style={{color: 'red'}}>
                    Auth Error: {authError}
                </p>
            )}

            {keycloakReady && !authenticated && screen === 'login' && (
                <LoginSection
                    onLogin={login}
                    onCreateAccount={() => setScreen('registration')}
                />
            )}

            {keycloakReady && !authenticated && screen === 'registration' && (
                <RegistrationFlow onBackToLogin={() => setScreen('login')}/>
            )}

            {keycloakReady && authenticated && (
                <AuthenticatedSection
                    username={username}
                    loading={loading}
                    apiError={apiError}
                    message={message}
                    onLogout={logout}
                    onReload={loadHello}
                />
            )}
        </main>
    )
}

export default App
