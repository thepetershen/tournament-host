// src/utils/authFetch.js

/**
 * A wrapper for fetch that automatically adds the JWT token from localStorage
 * as an Authorization header for authenticated requests.
 *
 * Usage:
 *   import authFetch from '../utils/authFetch';
 *   const res = await authFetch('/api/protected', { method: 'GET' });
 */
export default async function authFetch(url, options = {}) {
  const token = localStorage.getItem('token');
  const headers = {
    ...(options.headers || {}),
    ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
    'Content-Type': 'application/json',
  };
  return fetch(url, {
    ...options,
    headers,
  });
}
