import {ErrorMessage} from './ErrorMessage'

export function PasswordStep({
                                 title,
                                 description,
                                 password,
                                 message,
                                 error,
                                 loading,
                                 submitLabel = 'Next',
                                 loadingLabel = 'Submitting...',
                                 onPasswordChange,
                                 onCancel,
                                 onSubmit
                             }) {
    return (
        <section>
            <h2>{title}</h2>
            <p>{description}</p>

            {message && <p>{message}</p>}

            <label>
                Password
                <input
                    type="password"
                    value={password}
                    onChange={(event) => onPasswordChange(event.target.value)}
                    placeholder="Password"
                />
            </label>

            <ErrorMessage message={error}/>

            <button type="button" onClick={onCancel}>
                Cancel
            </button>
            <button
                type="button"
                onClick={onSubmit}
                disabled={loading || password.length === 0}
            >
                {loading ? loadingLabel : submitLabel}
            </button>
        </section>
    )
}