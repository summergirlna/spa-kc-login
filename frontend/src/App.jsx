import {useEffect, useState} from 'react'
import './App.css'
import keycloak, {initKeycloak} from './keycloak'

function App() {
    const [message, setMessage] = useState('')
    const [loading, setLoading] = useState(true)
    const [apiError, setApiError] = useState('')
    const [authError, setAuthError] = useState('')
    const [keycloakReady, setKeycloakReady] = useState(false)
    const [authenticated, setAuthenticated] = useState(false)
    const [username, setUsername] = useState('')

    const fetchHello = (authenticated) => {
        setLoading(true)
        setApiError('')

        const headers = authenticated && keycloak.token
            ? {
                Authorization: `Bearer ${keycloak.token}`
            }
            : {}

        fetch('http://localhost:8080/api/hello', {
            headers
        })
            .then((response) => {
                if (!response.ok) {
                    throw new Error(`API request failed: ${response.status}`)
                }

                return response.json()
            })
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
                    setUsername(
                        keycloak.tokenParsed?.preferred_username
                        ?? keycloak.tokenParsed?.sub
                        ?? ''
                    )
                }

                fetchHello(authenticated)
            })
            .catch((error) => {
                console.error('Failed to initialize Keycloak', error)
                setAuthError('Failed to initialize Keycloak')
                fetchHello(false)
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
            redirectUri: 'http://localhost:5173'
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

            {keycloakReady && (
                <section>
                    <h2>Authentication</h2>

                    {authenticated ? (
                        <>
                            <p>Logged in as: {username}</p>
                            <button type="button" onClick={logout}>
                                Logout
                            </button>
                        </>
                    ) : (
                        <>
                            <p>Not logged in</p>
                            <button type="button" onClick={login}>
                                Login
                            </button>
                        </>
                    )}
                </section>
            )}

            <section>
                <h2>Backend API</h2>

                {loading && <p>Loading...</p>}

                {apiError && (
                    <p style={{color: 'red'}}>
                        API Error: {apiError}
                    </p>
                )}

                {!loading && !apiError && (
                    <p>Backend message: {message}</p>
                )}
            </section>
        </main>
    )

}

export default App
