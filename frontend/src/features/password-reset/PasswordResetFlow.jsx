import {useState} from 'react'
import {
    completePasswordReset,
    confirmPasswordResetCode,
    startPasswordReset
} from '../../api/passwordResetApi'
import {ErrorMessage} from '../../components/ErrorMessage'

export function PasswordResetFlow({onBackToLogin}) {
    const [step, setStep] = useState('email')
    const [email, setEmail] = useState('')
    const [resetId, setResetId] = useState('')
    const [code, setCode] = useState('')
    const [password, setPassword] = useState('')
    const [message, setMessage] = useState('')
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)

    const resetAndBackToLogin = () => {
        setStep('email')
        setEmail('')
        setResetId('')
        setCode('')
        setPassword('')
        setMessage('')
        setError('')
        onBackToLogin()
    }

    const handleStartPasswordReset = () => {
        setError('')
        setMessage('')
        setLoading(true)

        startPasswordReset(email)
            .then((data) => {
                setResetId(data.resetId)
                setMessage('パスワードリセット用の確認コードを出力しました。runtime-data/mails を確認してください。')
                setStep('code')
            })
            .catch((error) => {
                setError(error.message)
            })
            .finally(() => {
                setLoading(false)
            })
    }

    const handleConfirmCode = () => {
        setError('')
        setMessage('')
        setLoading(true)

        confirmPasswordResetCode(resetId, code)
            .then(() => {
                setMessage('確認コードを検証しました。新しいパスワードを設定してください。')
                setStep('password')
            })
            .catch((error) => {
                setError(error.message)
            })
            .finally(() => {
                setLoading(false)
            })
    }

    const handleCompletePasswordReset = () => {
        setError('')
        setMessage('')
        setLoading(true)

        completePasswordReset(resetId, password)
            .then(() => {
                setPassword('')
                setMessage('パスワードを変更しました。新しいパスワードでログインしてください。')
                setStep('complete')
            })
            .catch((error) => {
                setError(error.message)
            })
            .finally(() => {
                setLoading(false)
            })
    }

    if (step === 'email') {
        return (
            <section>
                <h2>Password reset</h2>
                <p>登録済みのメールアドレスを入力してください。</p>

                {message && <p>{message}</p>}

                <label>
                    Email
                    <input
                        type="email"
                        value={email}
                        onChange={(event) => setEmail(event.target.value)}
                        placeholder="user@example.com"
                    />
                </label>

                <ErrorMessage message={error}/>

                <button type="button" onClick={resetAndBackToLogin}>
                    Back to login
                </button>
                <button
                    type="button"
                    onClick={handleStartPasswordReset}
                    disabled={loading || email.length === 0}
                >
                    {loading ? 'Sending...' : 'Send code'}
                </button>
            </section>
        )
    }

    if (step === 'code') {
        return (
            <section>
                <h2>Password reset code</h2>
                <p>確認コードを入力してください。</p>
                <p>確認コードは <code>runtime-data/mails</code> 配下に出力されています。</p>

                {message && <p>{message}</p>}

                <label>
                    Confirmation code
                    <input
                        type="text"
                        value={code}
                        onChange={(event) => setCode(event.target.value)}
                        placeholder="123456"
                        maxLength={6}
                    />
                </label>

                <ErrorMessage message={error}/>

                <button type="button" onClick={resetAndBackToLogin}>
                    Cancel
                </button>
                <button
                    type="button"
                    onClick={handleConfirmCode}
                    disabled={loading || code.length !== 6}
                >
                    {loading ? 'Checking...' : 'Next'}
                </button>
            </section>
        )
    }

    if (step === 'password') {
        return (
            <section>
                <h2>New password</h2>
                <p>新しいパスワードを入力してください。</p>

                {message && <p>{message}</p>}

                <label>
                    New password
                    <input
                        type="password"
                        value={password}
                        onChange={(event) => setPassword(event.target.value)}
                        placeholder="New password"
                    />
                </label>

                <ErrorMessage message={error}/>

                <button type="button" onClick={resetAndBackToLogin}>
                    Cancel
                </button>
                <button
                    type="button"
                    onClick={handleCompletePasswordReset}
                    disabled={loading || password.length === 0}
                >
                    {loading ? 'Changing...' : 'Change password'}
                </button>
            </section>
        )
    }

    return (
        <section>
            <h2>Password reset completed</h2>

            {message && <p>{message}</p>}

            <button type="button" onClick={resetAndBackToLogin}>
                Back to login
            </button>
        </section>
    )
}