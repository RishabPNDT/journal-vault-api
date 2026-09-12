const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

export async function request(path, options = {}) {
  const token = localStorage.getItem('journal_token')
  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}), ...options.headers }
  })
  if (response.status === 204) return null
  const body = await response.json().catch(() => ({}))
  if (!response.ok) throw new Error(body.message || 'Something went wrong. Please try again.')
  return body
}
