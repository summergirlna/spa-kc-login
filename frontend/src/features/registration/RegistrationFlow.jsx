import {useState} from 'react'
import {
    completeRegistration,
    confirmRegistrationCode,
    startRegistration
} from '../../api/registrationApi'
import {RegisterCodeStep} from './RegisterCodeStep'
import {RegisterCompleteStep} from './RegisterCompleteStep'
import {RegisterEmailStep} from './RegisterEmailStep'
import {RegisterPasswordStep} from './RegisterPasswordStep'

export function RegistrationFlow({onBackToLogin}) {
    const [step, setStep] = useState('email')
    const [registrationId, setRegistrationId] = useState('')
    const [email, setEmail] = useState('')
    const [code, setCode] = useState('')
    const [password, setPassword] = useState('')
    const [registeredUserId, setRegisteredUserId] = useState('')
    const [message, setMessage] = useState('')
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)

    const resetAndBackToLogin = () => {
        setStep('email')
        setRegistrationId('')
        setEmail('')
        setCode('')
        setPassword('')
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

    const handleCompleteRegistration = () => {
        setError('')
        setMessage('')
        setLoading(true)

        completeRegistration(registrationId, password)
            .then((data) => {
                setRegisteredUserId(data.userId)
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
            <RegisterEmailStep
                email={email}
                error={error}
                loading={loading}
                onEmailChange={setEmail}
                onBack={resetAndBackToLogin}
                onSubmit={handleStartRegistration}
            />
        )
    }

    if (step === 'code') {
        return (
            <RegisterCodeStep
                code={code}
                message={message}
                error={error}
                loading={loading}
                onCodeChange={setCode}
                onCancel={resetAndBackToLogin}
                onSubmit={handleConfirmRegistration}
            />
        )
    }

    if (step === 'password') {
        return (
            <RegisterPasswordStep
                password={password}
                message={message}
                error={error}
                loading={loading}
                onPasswordChange={setPassword}
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