import {ErrorMessage} from './ErrorMessage'

export function CodeStep({
                             title,
                             description,
                             code,
                             message,
                             error,
                             loading,
                             submitLabel = 'Next',
                             loadingLabel = 'Checking...',
                             onCodeChange,
                             onCancel,
                             onSubmit,
                             onResend
                         }) {
    return (
        <section>
            <h2>{title}</h2>
            <p>{description}</p>
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

            <button type="button" onClick={onCancel}>
                Cancel
            </button>

            {onResend && (
                <button
                    type="button"
                    onClick={onResend}
                    disabled={loading}
                >
                    Resend code
                </button>
            )}

            <button
                type="button"
                onClick={onSubmit}
                disabled={loading || code.length !== 6}
            >
                {loading ? loadingLabel : submitLabel}
            </button>
        </section>
    )
}