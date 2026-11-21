export function ErrorMessage({label = 'Error', message}) {
    if (!message) {
        return null
    }

    return (
        <p className="error-message">
            {label}: {message}
        </p>
    )
}