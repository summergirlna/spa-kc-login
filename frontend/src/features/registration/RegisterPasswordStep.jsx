import {ButtonRow} from '../../components/ButtonRow'
import {ErrorMessage} from '../../components/ErrorMessage'

export function RegisterPasswordStep({
                                         password,
                                         message,
                                         error,
                                         loading,
                                         onPasswordChange,
                                         onCancel,
                                         onSubmit
                                     }) {
    return (
        <section>
            <h2>Set password</h2>
            <p>パスワードを設定してください。</p>
            <p>8文字以上、英字と数字を含めてください。</p>

            {message && <p>{message}</p>}

            <label>
                Password
                <input
                    type="password"
                    value={password}
                    onChange={(event) => onPasswordChange(event.target.value)}
                    placeholder="Password123"
                />
            </label>

            <ErrorMessage message={error}/>

            <ButtonRow>
                <button type="button" onClick={onCancel}>
                    Cancel
                </button>
                <button
                    type="button"
                    onClick={onSubmit}
                    disabled={loading || password.length < 8}
                >
                    {loading ? 'Registering...' : 'Register'}
                </button>
            </ButtonRow>
        </section>
    )
}