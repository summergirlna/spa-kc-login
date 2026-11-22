import {ErrorMessage} from './ErrorMessage'

export function EmailStep({
                              title,
                              description,
                              email,
                              message,
                              error,
                              loading,
                              submitLabel = 'Send',
                              loadingLabel = 'Sending...',
                              onEmailChange,
                              onBack,
                              onSubmit
                          }) {
    return (
        <section>
            <h2>{title}</h2>
            <p>{description}</p>

            {message && <p>{message}</p>}

            <label>
                Email
                <input
                    type="email"
                    value={email}
                    onChange={(event) => onEmailChange(event.target.value)}
                    placeholder="user@example.com"
                />
            </label>

            <ErrorMessage message={error}/>

            <button type="button" onClick={onBack}>
                Back
            </button>
            <button
                type="button"
                onClick={onSubmit}
                disabled={loading || email.length === 0}
            >
                {loading ? loadingLabel : submitLabel}
            </button>
        </section>
    )
}