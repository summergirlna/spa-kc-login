import {ErrorMessage} from '../../components/ErrorMessage'

export function AuthenticatedSection({
                                         username,
                                         loading,
                                         apiError,
                                         message,
                                         onLogout,
                                         onChangePassword,
                                         onChangeEmail,
                                         onReload
                                     }) {
    return (
        <>
            <section>
                <h2>Authentication</h2>
                <p>Logged in as: {username}</p>
                <button type="button" onClick={onChangePassword}>
                    Change password
                </button>
                <button type="button" onClick={onChangeEmail}>
                    Change email
                </button>
                <button type="button" onClick={onLogout}>
                    Logout
                </button>
            </section>

            <section>
                <h2>Backend API</h2>

                {loading && <p>Loading...</p>}

                <ErrorMessage label="API Error" message={apiError}/>

                {!loading && !apiError && message && (
                    <p>Backend message: {message}</p>
                )}

                <button type="button" onClick={onReload}>
                    Reload backend message
                </button>
            </section>
        </>
    )
}