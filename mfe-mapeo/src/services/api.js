const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080/api';

export async function login(username, password) {
  const res = await fetch(`${API_BASE}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  });
  if (!res.ok) return null;
  return await res.json();
}

export async function getUser(id) {
  const res = await fetch(`${API_BASE}/users/${id}`);
  if (!res.ok) return null;
  return await res.json();
}

export async function updateUser(id, username) {
  const res = await fetch(`${API_BASE}/users/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username }),
  });
  if (!res.ok) return null;
  return await res.json();
}
