import {useEffect, useState} from 'react'
import './App.css'
import keycloak, {initKeycloak} from './keycloak'

const API_BASE_URL = ''

function App() {
    const [screen, setScreen] = useState('login')
    const [message, setMessage] = useState('')
    const [loading, setLoading] = useState(true)
    const [apiError, setApiError] = useState('')
    const [authError, setAuthError] = useState('')
    const [keycloakReady, setKeycloakReady] = useState(false)
    const [authenticated, setAuthenticated] = useState(false)
    const [username, setUsername] = useState('')

    const [registrationId, setRegistrationId] = useState('')
    const [registrationEmail, setRegistrationEmail] = useState('')
    const [confirmationCode, setConfirmationCode] = useState('')
    const [password, setPassword] = useState('')
    const [registeredUserId, setRegisteredUserId] = useState('')
    const [registrationMessage, setRegistrationMessage] = useState('')
    const [registrationError, setRegistrationError] = useState('')
    const [registrationLoading, setRegistrationLoading] = useState(false)

    const fetchHello = () => {
        setLoading(true)
        setApiError('')

        fetch(`${API_BASE_URL}/api/hello`, {
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
                    setScreen('authenticated')
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
            redirectUri: 'http://localhost'
        })
    }

    const startRegistration = () => {
        setRegistrationError('')
        setRegistrationMessage('')
        setRegistrationLoading(true)

        fetch(`${API_BASE_URL}/api/registration/email`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                email: registrationEmail
            })
        })
            .then(handleJsonResponse)
            .then((data) => {
                setRegistrationId(data.registrationId)
                setRegistrationMessage("確認コードを出力しました。runtime-data/mails を確認してください。")
                setScreen('register-code')
            })
            .catch((error) => {
                setRegistrationError(error.message)
            })
            .finally(() => {
                setRegistrationLoading(false)
            })
    }

    const confirmRegistration = () => {
        setRegistrationError('')
        setRegistrationMessage('')
        setRegistrationLoading(true)

        fetch(`${API_BASE_URL}/api/registration/confirm-code`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                registrationId,
                code: confirmationCode
            })
        })
            .then(handleJsonResponse)
            .then(() => {
                setRegistrationMessage('確認コードを検証しました。パスワードを設定してください。')
                setScreen('register-password')
            })
            .catch((error) => {
                setRegistrationError(error.message)
            })
            .finally(() => {
                setRegistrationLoading(false)
            })
    }

    const completeRegistration = () => {
        setRegistrationError('')
        setRegistrationMessage('')
        setRegistrationLoading(true)

        fetch(`${API_BASE_URL}/api/registration/complete`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                registrationId,
                password
            })
        })
            .then(handleJsonResponse)
            .then((data) => {
                setRegisteredUserId(data.userId)
                setRegistrationMessage('ユーザ登録が完了しました。')
                setScreen('register-complete')
            })
            .catch((error) => {
                setRegistrationError(error.message)
            })
            .finally(() => {
                setRegistrationLoading(false)
            })
    }

    const backToLogin = () => {
        setScreen('login')
        setRegistrationId('')
        setRegistrationEmail('')
        setConfirmationCode('')
        setPassword('')
        setRegisteredUserId('')
        setRegistrationMessage('')
        setRegistrationError('')
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
                <section>
                    <h2>Login</h2>
                    <p>バックエンド API を利用するにはログインしてください。</p>

                    <button type="button" onClick={login}>
                        Login with Keycloak
                    </button>

                    <button type="button" onClick={() => setScreen('register-email')}>
                        Create account
                    </button>
                </section>
            )}

            {keycloakReady && !authenticated && screen === 'register-email' && (
                <section>
                    <h2>Create account</h2>
                    <p>メールアドレスを入力してください。</p>

                    <label>
                        Email
                        <input
                            type="email"
                            value={registrationEmail}
                            onChange={(event) => setRegistrationEmail(event.target.value)}
                            placeholder="user@example.com"
                        />
                    </label>

                    {registrationError && <p style={{color: 'red'}}>Error: {registrationError}</p>}

                    <div className="button-row">
                        <button type="button" onClick={backToLogin}>
                            Back
                        </button>
                        <button
                            type="button"
                            onClick={startRegistration}
                            disabled={registrationLoading || !registrationEmail}
                        >
                            {registrationLoading ? 'Sending...' : 'Next'}
                        </button>
                    </div>
                </section>
            )}

            {keycloakReady && !authenticated && screen === 'register-code' && (
                <section>
                    <h2>Confirmation code</h2>
                    <p>確認コードを入力してください。</p>
                    <p>確認コードは <code>runtime-data/mails</code> 配下に出力されています。</p>

                    {registrationMessage && <p>{registrationMessage}</p>}

                    <label>
                        Confirmation code
                        <input
                            type="text"
                            value={confirmationCode}
                            onChange={(event) => setConfirmationCode(event.target.value)}
                            placeholder="123456"
                            maxLength={6}
                        />
                    </label>

                    {registrationError && <p style={{color: 'red'}}>Error: {registrationError}</p>}

                    <div className="button-row">
                        <button type="button" onClick={backToLogin}>
                            Cancel
                        </button>
                        <button
                            type="button"
                            onClick={confirmRegistration}
                            disabled={registrationLoading || confirmationCode.length !== 6}
                        >
                            {registrationLoading ? 'Checking...' : 'Next'}
                        </button>
                    </div>
                </section>
            )}

            {keycloakReady && !authenticated && screen === 'register-password' && (
                <section>
                    <h2>Set password</h2>
                    <p>パスワードを設定してください。</p>
                    <p>8文字以上、英字と数字を含めてください。</p>

                    {registrationMessage && <p>{registrationMessage}</p>}

                    <label>
                        Password
                        <input
                            type="password"
                            value={password}
                            onChange={(event) => setPassword(event.target.value)}
                            placeholder="Password123"
                        />
                    </label>

                    {registrationError && <p style={{color: 'red'}}>Error: {registrationError}</p>}

                    <div className="button-row">
                        <button type="button" onClick={backToLogin}>
                            Cancel
                        </button>
                        <button
                            type="button"
                            onClick={completeRegistration}
                            disabled={registrationLoading || password.length < 8}
                        >
                            {registrationLoading ? 'Registering...' : 'Register'}
                        </button>
                    </div>
                </section>
            )}

            {keycloakReady && !authenticated && screen === 'register-complete' && (
                <section>
                    <h2>Registration completed</h2>

                    {registrationMessage && <p>{registrationMessage}</p>}

                    <p>払い出されたユーザID:</p>
                    <p>
                        <strong>{registeredUserId}</strong>
                    </p>

                    <button type="button" onClick={backToLogin}>
                        Back to login
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

function handleJsonResponse(response) {
    return response.json()
        .then((data) => {
            if (!response.ok) {
                throw new Error(data.message ?? `API request failed: ${response.status}`)
            }

            return data
        })
}

export default App
