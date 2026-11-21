import {ButtonRow} from '../../components/ButtonRow'
import {ErrorMessage} from '../../components/ErrorMessage'

export function RegisterEmailStep({
                                      email,
                                      error,
                                      loading,
                                      onEmailChange,
                                      onBack,
                                      onSubmit
                                  }) {
    return (
        <section>
            <h2>Create account</h2>
            <p>メールアドレスを入力してください。</p>

            <label>
                Email
                <input
                    type="email"
                    value={email}
                    onChange={(event) => onEmailChange(event.target.value)}
                    placeholder="user@example.com"
                />
            </label>

            <ErrorMessage message={error}/>

            <ButtonRow>
                <button type="button" onClick={onBack}>
                    Back
                </button>
                <button
                    type="button"
                    onClick={onSubmit}
                    disabled={loading || !email}
                >
                    {loading ? 'Sending...' : 'Next'}
                </button>
            </ButtonRow>
        </section>
    )
}