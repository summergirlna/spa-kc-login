import {useState} from "react";
import {confirmEmailChange, startEmailChange} from "../../api/accountApi.js";
import keycloak from "../../keycloak.js";
import {EmailStep} from "../../components/EmailStep.jsx";
import {CodeStep} from "../../components/CodeStep.jsx";

export function EmailChangeFlow({onCancel}) {
    const [step, setStep] = useState('email')
    const [newEmail, setNewEmail] = useState('')
    const [changeId, setChangeId] = useState('')
    const [code, setCode] = useState('')
    const [message, setMessage] = useState('')
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)

    const resetAndCancel = () => {
        setStep('email')
        setNewEmail('')
        setChangeId('')
        setCode('')
        setMessage('')
        setError('')
        onCancel()
    }

    const handleStartEmailChange = () => {
        setError('')
        setMessage('')
        setLoading(true)

        startEmailChange(keycloak.token, newEmail)
            .then((data) => {
                setChangeId(data.changeId)
                setMessage('メールアドレス変更用の確認コードを出力しました。runtime-data/mails を確認してください。')
                setStep('code')
            })
            .catch((error) => {
                setError(error.message)
            })
            .finally(() => {
                setLoading(false)
            })
    }

    const handleConfirmEmailChange = () => {
        setError('')
        setMessage('')
        setLoading(true)

        confirmEmailChange(keycloak.token, changeId, code)
            .then(() => {
                setMessage('メールアドレスを変更しました。再ログインしてください。')
                setStep('complete')
            })
            .catch((error) => {
                setError(error.message)
            })
            .finally(() => {
                setLoading(false)
            })
    }

    const logout = () => {
        keycloak.logout({
            redirectUri: window.location.origin
        })
    }

    if (step === 'email') {
        return (
            <EmailStep
                title="Change email"
                description="新しいメールアドレスを入力してください。"
                email={newEmail}
                message={message}
                error={error}
                loading={loading}
                submitLabel="Send code"
                loadingLabel="Sending..."
                onEmailChange={setNewEmail}
                onBack={resetAndCancel}
                onSubmit={handleStartEmailChange}
            />
        )
    }

    if (step === 'code') {
        return (
            <CodeStep
                title="Email change code"
                description="新しいメールアドレスに送信された確認コードを入力してください。"
                code={code}
                message={message}
                error={error}
                loading={loading}
                onCodeChange={setCode}
                onCancel={resetAndCancel}
                onSubmit={handleConfirmEmailChange}
            />
        )
    }

    return (
        <section>
            <h2>Email changed</h2>

            {message && <p>{message}</p>}

            <button type="button" onClick={logout}>
                Login again
            </button>
        </section>
    )
}