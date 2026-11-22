import {ButtonRow} from '../../components/ButtonRow'
import {ErrorMessage} from '../../components/ErrorMessage'

export function RegisterCodeStep({
                                     code,
                                     message,
                                     error,
                                     loading,
                                     onCodeChange,
                                     onCancel,
                                     onResend,
                                     onSubmit
                                 }) {
    return (
        <section>
            <h2>Confirmation code</h2>
            <p>確認コードを入力してください。</p>
            <p>確認コードは <code>runtime-data/mails</code> 配下に出力されています。</p>

            {message && <p>{message}</p>}

            <label>
                Confirmation code
                <input
                    type="text"
                    value={code}
                    onChange={(event) => onCodeChange(event.target.value)}
                    placeholder="123456"
                    maxLength={6}
                />
            </label>

            <ErrorMessage message={error}/>

            <ButtonRow>
                <button type="button" onClick={onCancel}>
                    Cancel
                </button>
                <button
                    type="button"
                    onClick={onResend}
                    disabled={loading}
                >
                    確認コード再送
                </button>
                <button
                    type="button"
                    onClick={onSubmit}
                    disabled={loading || code.length !== 6}
                >
                    {loading ? 'Checking...' : 'Next'}
                </button>
            </ButtonRow>
        </section>
    )
}