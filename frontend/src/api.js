/*
 * One place that knows how to talk to the backend.
 *
 * Every failed request becomes an ApiError carrying the field-level messages
 * from the server's RFC 9457 response, so a form can render errors.weightKg
 * next to the weight input without parsing anything itself.
 */

export class ApiError extends Error {
  constructor(message, status, fieldErrors) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.fieldErrors = fieldErrors ?? {};
  }
}

async function request(path, options = {}) {
  let response;
  try {
    response = await fetch(path, options);
  } catch {
    // Network-level failure: server down, connection dropped, DNS.
    throw new ApiError('Could not reach the server. Is the backend running?', 0, {});
  }

  if (!response.ok) {
    let problem = null;
    try {
      problem = await response.json();
    } catch {
      // Not every error response is JSON; fall through to a generic message.
    }
    throw new ApiError(
      problem?.detail ?? problem?.title ?? `Request failed (${response.status})`,
      response.status,
      problem?.errors,
    );
  }

  if (response.status === 204) {
    return null;
  }
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
  journal: () => request('/api/catches'),
  catchDetail: (id) => request(`/api/catches/${id}`),

  // FormData, not JSON: the photo travels with the fields, and the browser
  // sets the multipart Content-Type (including the boundary) itself.
  createCatch: (formData) => request('/api/catches', { method: 'POST', body: formData }),

  species: () => request('/api/species'),
  lures: () => request('/api/lures'),
  lureOptions: () => request('/api/lures/options'),
  createLure: (lure) => json('/api/lures', 'POST', lure),

  conditions: (latitude, longitude, at) =>
    request(
      `/api/conditions?latitude=${encodeURIComponent(latitude)}` +
        `&longitude=${encodeURIComponent(longitude)}` +
        `&at=${encodeURIComponent(at)}`,
    ),
};
