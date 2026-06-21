const API_BASE = import.meta.env.VITE_API_BASE || '/api';

export interface UserDTO {
  id: number;
  username: string;
  region: string;
  commune: string;
  email: string;
  phone: string;
  role: string;
}

export interface AuthResponse {
  token: string;
  user: UserDTO;
}

export interface FireReportDTO {
  id?: number;
  description: string;
  type: string;
  status: string;
  latitude: number;
  longitude: number;
  region?: string;
  commune?: string;
  reportDate?: string;
  userId?: number;
  mediaUrl?: string;
}

export interface AlertDTO {
  id?: number;
  title: string;
  message: string;
  level: string; // INFO, WARNING, DANGER
  region?: string;
  commune: string;
  createdAt?: string;
}

// Helper to get Auth headers
function getHeaders(token?: string, isMultipart = false): HeadersInit {
  const headers: Record<string, string> = {};
  if (!isMultipart) {
    headers['Content-Type'] = 'application/json';
  }
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
}

export async function login(username: string, password: string): Promise<AuthResponse | null> {
  try {
    const res = await fetch(`${API_BASE}/users/auth/login`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify({ username, password }),
    });
    if (!res.ok) return null;
    return await res.json();
  } catch (e) {
    console.error('Login error', e);
    return null;
  }
}

export async function register(payload: any): Promise<boolean> {
  try {
    const res = await fetch(`${API_BASE}/users/auth/register`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(payload),
    });
    return res.status === 201 || res.status === 200;
  } catch (e) {
    console.error('Register error', e);
    return false;
  }
}

export async function getUser(id: number, token: string): Promise<UserDTO | null> {
  try {
    const res = await fetch(`${API_BASE}/users/${id}`, {
      headers: getHeaders(token),
    });
    if (!res.ok) return null;
    return await res.json();
  } catch (e) {
    console.error('GetUser error', e);
    return null;
  }
}

export async function updateUser(id: number, payload: Partial<UserDTO>, token: string): Promise<UserDTO | null> {
  try {
    const res = await fetch(`${API_BASE}/users/${id}`, {
      method: 'PUT',
      headers: getHeaders(token),
      body: JSON.stringify(payload),
    });
    if (!res.ok) return null;
    return await res.json();
  } catch (e) {
    console.error('UpdateUser error', e);
    return null;
  }
}

// ----------------------------------------------------
// FIRE REPORTS SERVICES
// ----------------------------------------------------

export async function getFireReports(): Promise<FireReportDTO[]> {
  try {
    const res = await fetch(`${API_BASE}/reports`);
    if (!res.ok) return [];
    return await res.json();
  } catch (e) {
    console.error('GetFireReports error', e);
    return [];
  }
}

export async function createFireReport(report: FireReportDTO): Promise<FireReportDTO | null> {
  try {
    const res = await fetch(`${API_BASE}/reports`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(report),
    });
    if (!res.ok) return null;
    return await res.json();
  } catch (e) {
    console.error('CreateFireReport error', e);
    return null;
  }
}

export async function createFireReportMultipart(formData: FormData): Promise<FireReportDTO | null> {
  try {
    const res = await fetch(`${API_BASE}/reports/upload`, {
      method: 'POST',
      body: formData,
    });
    if (!res.ok) return null;
    return await res.json();
  } catch (e) {
    console.error('CreateFireReportMultipart error', e);
    return null;
  }
}

export async function updateReportStatus(id: number, status: string, token: string): Promise<FireReportDTO | null> {
  try {
    const res = await fetch(`${API_BASE}/reports/${id}/status`, {
      method: 'PUT',
      headers: getHeaders(token),
      body: JSON.stringify({ status }),
    });
    if (!res.ok) return null;
    return await res.json();
  } catch (e) {
    console.error('UpdateReportStatus error', e);
    return null;
  }
}

export async function getActiveReportsCount(type: string): Promise<number> {
  try {
    const res = await fetch(`${API_BASE}/reports/statistics/count?type=${type}`);
    if (!res.ok) return 0;
    return await res.json();
  } catch (e) {
    console.error('GetActiveReportsCount error', e);
    return 0;
  }
}

// ----------------------------------------------------
// ALERTS SERVICES
// ----------------------------------------------------

export async function getAlerts(): Promise<AlertDTO[]> {
  try {
    const res = await fetch(`${API_BASE}/alerts`);
    if (!res.ok) return [];
    return await res.json();
  } catch (e) {
    console.error('GetAlerts error', e);
    return [];
  }
}

export async function createAlert(alert: AlertDTO, token: string): Promise<AlertDTO | null> {
  try {
    const res = await fetch(`${API_BASE}/alerts`, {
      method: 'POST',
      headers: getHeaders(token),
      body: JSON.stringify(alert),
    });
    if (!res.ok) return null;
    return await res.json();
  } catch (e) {
    console.error('CreateAlert error', e);
    return null;
  }
}

// ----------------------------------------------------
// ADMINISTRATOR SERVICES
// ----------------------------------------------------

export async function getAllUsers(token: string): Promise<UserDTO[]> {
  try {
    const res = await fetch(`${API_BASE}/users`, {
      headers: getHeaders(token),
    });
    if (!res.ok) return [];
    return await res.json();
  } catch (e) {
    console.error('GetAllUsers error', e);
    return [];
  }
}

export async function updateUserRole(id: number, role: string, token: string): Promise<UserDTO | null> {
  try {
    const res = await fetch(`${API_BASE}/users/${id}/role`, {
      method: 'PUT',
      headers: getHeaders(token),
      body: JSON.stringify({ role }),
    });
    if (!res.ok) return null;
    return await res.json();
  } catch (e) {
    console.error('UpdateUserRole error', e);
    return null;
  }
}
