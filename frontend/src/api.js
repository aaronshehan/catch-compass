/*
 * One place that knows how to talk to the backend.
 *
 * Handles three things every request needs: cookies for the session, the CSRF
 * token on writes, and turning failures into an ApiError that carries the
 * server's field-level messages.
 */

export class ApiError extends Error {
  constructor(message, status, fieldErrors) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.fieldErrors = fieldErrors ?? {};
  }
}

/**
 * Spring writes the CSRF token to a readable XSRF-TOKEN cookie; we send it back
 * as a header. A malicious site can make your browser POST here, but it cannot
 * read your cookies to set this header, which is what makes the check work.
 */
function csrfToken() {
  const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]*)/);
  return match ? decodeURIComponent(match[1]) : null;
}

const WRITE_METHODS = new Set(['POST', 'PUT', 'PATCH', 'DELETE']);

async function request(path, options = {}) {
  const method = (options.method ?? 'GET').toUpperCase();
  const headers = { ...(options.headers ?? {}) };

  if (WRITE_METHODS.has(method)) {
    const token = csrfToken();
    if (token) headers['X-XSRF-TOKEN'] = token;
  }

  let response;
  try {
    response = await fetch(path, { ...options, headers, credentials: 'same-origin' });
  } catch {
    throw new ApiError('Could not reach the server. Is the backend running?', 0, {});
  }

  if (!response.ok) {
    // A 401 on a normal call means the session ended while the app was open.
    // Announcing it here means every screen does not need its own check.
    if (response.status === 401 && !path.startsWith('/api/auth/')) {
      window.dispatchEvent(new CustomEvent('auth:expired'));
    }

    let problem = null;
    try {
      problem = await response.json();
    } catch {
      // Not every error response is JSON.
    }
    throw new ApiError(
      problem?.detail ?? problem?.title ?? `Request failed (${response.status})`,
      response.status,
      problem?.errors,
    );
  }

  if (response.status === 204) return null;
  return response.json();
}

function json(path, method, body) {
  return request(path, {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
}

export const api = {
  me: () => request('/api/auth/me'),
  login: (username, password) => json('/api/auth/login', 'POST', { username, password }),
  register: (username, password) => json('/api/auth/register', 'POST', { username, password }),
  logout: () => request('/api/auth/logout', { method: 'POST' }),

  journal: () => request('/api/catches'),
  catchDetail: (id) => request(`/api/catches/${id}`),

  // FormData, not JSON: the photo travels with the fields, and the browser sets
  // the multipart Content-Type (including the boundary) itself.
  createCatch: (formData) => request('/api/catches', { method: 'POST', body: formData }),

  species: () => request('/api/species'),
  lures: () => request('/api/lures'),
  lureOptions: () => request('/api/lures/options'),
  createLure: (lure) => json('/api/lures', 'POST', lure),

  conditionsOptions: () => request('/api/conditions/options'),

  conditions: (latitude, longitude, at) =>
    request(
      `/api/conditions?latitude=${encodeURIComponent(latitude)}` +
        `&longitude=${encodeURIComponent(longitude)}` +
        `&at=${encodeURIComponent(at)}`,
    ),
};
