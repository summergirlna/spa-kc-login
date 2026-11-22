import {useState} from 'react'
import {
    completeRegistration,
    confirmRegistrationCode, resendRegistrationCode,
    startRegistration
} from '../../api/registrationApi'
import {RegisterCompleteStep} from './RegisterCompleteStep'
import {RegisterMfaStep} from './RegisterMfaStep.jsx';
import {EmailStep} from "../../components/EmailStep.jsx";
import {CodeStep} from "../../components/CodeStep.jsx";
import {PasswordStep} from "../../components/PasswordStep.jsx";

export function RegistrationFlow({onBackToLogin}) {
    const [step, setStep] = useState('email')
    const [registrationId, setRegistrationId] = useState('')
    const [email, setEmail] = useState('')
    const [code, setCode] = useState('')
    const [password, setPassword] = useState('')
    const [registeredUserId, setRegisteredUserId] = useState('')
    const [mfaType, setMfaType] = useState('')
    const [message, setMessage] = useState('')
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)

    const resetAndBackToLogin = () => {
        setStep('email')
        setRegistrationId('')
        setEmail('')
        setCode('')
        setPassword('')
        setMfaType('')
        setRegisteredUserId('')
        setMessage('')
        setError('')
        onBackToLogin()
    }

    const handleStartRegistration = () => {
        setError('')
        setMessage('')
        setLoading(true)

        startRegistration(email)
            .then((data) => {
                setRegistrationId(data.registrationId)
                setMessage('確認コードを出力しました。runtime-data/mails を確認してください。')
                setStep('code')
            })
            .catch((error) => {
                setError(error.message)
            })
            .finally(() => {
                setLoading(false)
            })
    }

    const handleConfirmRegistration = () => {
        setError('')
        setMessage('')
        setLoading(true)

        confirmRegistrationCode(registrationId, code)
            .then(() => {
                setMessage('確認コードを検証しました。パスワードを設定してください。')
                setStep('password')
            })
            .catch((error) => {
                setError(error.message)
            })
            .finally(() => {
                setLoading(false)
            })
    }

    const handleResendRegistrationCode = () => {
        setError('')
        setMessage('')
        setLoading(true)

        resendRegistrationCode(registrationId)
            .then(() => {
                setCode('')
                setMessage('確認コードを再送しました。runtime-data/mails を確認してください。')
            })
            .catch((error) => {
                setError(error.message)
            })
            .finally(() => {
                setLoading(false)
            })
    }

    const handlePasswordSubmit = () => {
        setError('')
        setMessage('')
        setStep('mfa')
    }

    const handleCompleteRegistration = () => {
        setError('')
        setMessage('')
        setLoading(true)

        completeRegistration(registrationId, password, mfaType)
            .then((data) => {
                setRegisteredUserId(data.userId)
                setPassword('')
                setMessage('ユーザ登録が完了しました。')
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
            <EmailStep
                title="Create account"
                description="メールアドレスを入力してください。"
                email={email}
                message={message}
                error={error}
                loading={loading}
                submitLabel="Send confirmation code"
                loadingLabel="Sending..."
                onEmailChange={setEmail}
                onBack={resetAndBackToLogin}
                onSubmit={handleStartRegistration}
            />
        )
    }

    if (step === 'code') {
        return (
            <CodeStep
                title="Confirmation code"
                description="確認コードを入力してください。"
                code={code}
                message={message}
                error={error}
                loading={loading}
                onCodeChange={setCode}
                onCancel={resetAndBackToLogin}
                onResend={handleResendRegistrationCode}
                onSubmit={handleConfirmRegistration}
            />
        )
    }

    if (step === 'password') {
        return (
            <PasswordStep
                title="Password"
                description="パスワードを設定してください。"
                password={password}
                message={message}
                error={error}
                loading={loading}
                submitLabel="Next"
                loadingLabel="Saving..."
                onPasswordChange={setPassword}
                onCancel={resetAndBackToLogin}
                onSubmit={handlePasswordSubmit}
            />
        )
    }

    if (step === 'mfa') {
        return (
            <RegisterMfaStep
                mfaType={mfaType}
                error={error}
                loading={loading}
                onMfaTypeChange={setMfaType}
                onCancel={resetAndBackToLogin}
                onSubmit={handleCompleteRegistration}
            />
        )
    }

    return (
        <RegisterCompleteStep
            userId={registeredUserId}
            message={message}
            onBackToLogin={resetAndBackToLogin}
        />
    )
}