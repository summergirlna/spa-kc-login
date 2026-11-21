export function RegisterCompleteStep({
                                         userId,
                                         message,
                                         onBackToLogin
                                     }) {
    return (
        <section>
            <h2>Registration completed</h2>

            {message && <p>{message}</p>}

            <p>払い出されたユーザID:</p>
            <p>
                <strong>{userId}</strong>
            </p>

            <button type="button" onClick={onBackToLogin}>
                Back to login
            </button>
        </section>
    )
}