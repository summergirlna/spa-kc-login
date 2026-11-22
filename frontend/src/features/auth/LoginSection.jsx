export function LoginSection({onLogin, onCreateAccount, onForgotPassword}) {
    return (
        <section>
            <h2>Login</h2>
            <p>バックエンド API を利用するにはログインしてください。</p>

            <button type="button" onClick={onLogin}>
                Login with Keycloak
            </button>

            <button type="button" onClick={onCreateAccount}>
                Create account
            </button>

            <button type="button" onClick={onForgotPassword}>
                Forgot password?
            </button>
        </section>
    )
}