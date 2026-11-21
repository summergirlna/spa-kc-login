const API_BASE_URL = ''

export function getJson(path, options = {}) {
    return fetch(`${API_BASE_URL}${path}`, options)
        .then(handleJsonResponse)
}

export function postJson(path, body, options = {}) {
    return fetch(`${API_BASE_URL}${path}`, {
        ...options,
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            ...options.headers
        },
        body: JSON.stringify(body)
    }).then(handleJsonResponse)
}

function handleJsonResponse(response) {
    return response.json()
        .then((data) => {
            if (!response.ok) {
                throw new Error(data.message ?? `API request failed: ${response.status}`)
            }

            return data
        })
}