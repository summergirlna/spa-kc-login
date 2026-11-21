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

    const fetchHello = () => {
        setLoading(true)
        setApiError('')

        fetch('http://localhost:8080/api/hello', {
            headers: {
                Authorization: `Bearer ${keycloak.token}`
            }
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

                    fetchHello()
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

            {keycloakReady && !authenticated && (
                <section>
                    <h2>Login</h2>
                    <p>バックエンド API を利用するにはログインしてください。</p>
                    <button type="button" onClick={login}>
                        Login with Keycloak
                    </button>
                </section>
            )}

            {keycloakReady && authenticated && (
                <>
                    <section>
                        <h2>Authentication</h2>
                        <p>Logged in as: {username}</p>
                        <button type="button" onClick={logout}>
                            Logout
                        </button>
                    </section>

                    <section>
                        <h2>Backend API</h2>

                        {loading && <p>Loading...</p>}

                        {apiError && (
                            <p style={{color: 'red'}}>
                                API Error: {apiError}
                            </p>
                        )}

                        {!loading && !apiError && message && (
                            <p>Backend message: {message}</p>
                        )}

                        <button type="button" onClick={fetchHello}>
                            Reload backend message
                        </button>
                    </section>
                </>
            )}
        </main>
    )


}

export default App
