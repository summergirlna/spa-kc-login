import {ButtonRow} from "../../components/ButtonRow.jsx";
import {ErrorMessage} from "../../components/ErrorMessage.jsx";

export function RegisterMfaStep({
                                    mfaType,
                                    error,
                                    loading,
                                    onMfaTypeChange,
                                    onCancel,
                                    onSubmit
}) {
    return (
        <section>
            <h2>Multi-factor authentication</h2>
            <p>多要素認証の方法を選択してください。</p>

            <label>
                <input
                    type="radio"
                    name="mfaType"
                    value="TOTP"
                    checked={mfaType === 'TOTP'}
                    onChange={(event) => onMfaTypeChange(event.target.value)}
                />
                認証アプリを使う
            </label>

            <label>
                <input
                    type="radio"
                    name="mfaType"
                    value="EMAIL"
                    checked={mfaType === 'EMAIL'}
                    onChange={(event) => onMfaTypeChange(event.target.value)}
                />
                メール確認コードを使う
            </label>

            <label>
                <input
                    type="radio"
                    name="mfaType"
                    value="WEBAUTHN"
                    checked={mfaType === 'WEBAUTHN'}
                    onChange={(event) => onMfaTypeChange(event.target.value)}
                />
                セキュリティキーまたは生体認証を使う
            </label>

            <ErrorMessage message={error}/>

            <ButtonRow>
                <button type="button" onClick={onCancel}>
                    Cancel
                </button>
                <button
                    type="button"
                    onClick={onSubmit}
                    disabled={loading || !mfaType}
                >
                    {loading ? 'Registering...' : 'Register'}
                </button>
            </ButtonRow>
        </section>
    )

}