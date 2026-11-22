import {useState} from 'react'
import {
    completePasswordReset,
    confirmPasswordResetCode, resendPasswordResetCode,
    startPasswordReset
} from '../../api/passwordResetApi'
import {EmailStep} from "../../components/EmailStep.jsx";
import {CodeStep} from "../../components/CodeStep.jsx";
import {PasswordStep} from "../../components/PasswordStep.jsx";

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

    const handleResendPasswordResetCode = () => {
        setError('')
        setMessage('')
        setLoading(true)

        resendPasswordResetCode(resetId)
            .then(() => {
                setCode('')
                setMessage('パスワードリセット用の確認コードを再送しました。runtime-data/mails を確認してください。')
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
            <EmailStep
                title="Password reset"
                description="登録済みのメールアドレスを入力してください。"
                email={email}
                message={message}
                error={error}
                loading={loading}
                submitLabel="Send code"
                loadingLabel="Sending..."
                onEmailChange={setEmail}
                onBack={resetAndBackToLogin}
                onSubmit={handleStartPasswordReset}
            />
        )
    }

    if (step === 'code') {
        return (
            <CodeStep
                title="Password reset code"
                description="確認コードを入力してください。"
                code={code}
                message={message}
                error={error}
                loading={loading}
                onCodeChange={setCode}
                onCancel={resetAndBackToLogin}
                onResend={handleResendPasswordResetCode}
                onSubmit={handleConfirmCode}
            />
        )
    }

    if (step === 'password') {
        return (
            <PasswordStep
                title="New password"
                description="新しいパスワードを入力してください。"
                password={password}
                message={message}
                error={error}
                loading={loading}
                submitLabel="Change password"
                loadingLabel="Changing..."
                onPasswordChange={setPassword}
                onCancel={resetAndBackToLogin}
                onSubmit={handleCompletePasswordReset}
            />
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