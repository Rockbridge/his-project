
export const BASE_URL = "http://localhost:8080/api/v1";
export const AUTH_HEADER = "Basic " + btoa("admin:dev-password"); // TODO: später aus ENV laden

export async function apiRequest(endpoint, init = {}) {
  const res = await fetch(`${BASE_URL}${endpoint}`, {
    headers: {
      "Content-Type": "application/json",
      Authorization: AUTH_HEADER,
      ...(init.headers || {}),
    },
    ...init,
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}: ${res.statusText}`);
  return res.json();
}
