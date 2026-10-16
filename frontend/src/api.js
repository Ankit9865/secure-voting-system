// All communication with the Spring Boot REST API goes through this file.
const BASE = import.meta.env.VITE_API_URL ?? "http://localhost:8080";
const TOKEN_KEY = "openballot.token";

// sessionStorage: cleared when the tab closes. It is still readable by any script on the page,
// so the real protection is avoiding XSS (React escapes output by default; never use
// dangerouslySetInnerHTML). A production system would use an httpOnly cookie + refresh tokens.
export const tokenStore = {
  get: () => sessionStorage.getItem(TOKEN_KEY),
  set: (token) => sessionStorage.setItem(TOKEN_KEY, token),
  clear: () => sessionStorage.removeItem(TOKEN_KEY),
};

export class ApiError extends Error {
  constructor(status, message, fieldErrors) {
    super(message);
    this.status = status;
    this.fieldErrors = fieldErrors ?? {};
  }
}

async function request(path, { method = "GET", body, auth = true } = {}) {
  const headers = { "Content-Type": "application/json" };
  const token = tokenStore.get();
  if (auth && token) headers.Authorization = `Bearer ${token}`;

  let res;
  try {
    res = await fetch(`${BASE}${path}`, {
      method,
      headers,
      body: body ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new ApiError(0, "Cannot reach the server. Check that the API is running.");
  }

  if (res.status === 204) return null;
  const data = await res.json().catch(() => null);

  if (!res.ok) {
    if (res.status === 401 && auth) {
      // Token expired or invalid: drop it and tell the app to show the sign-in page
      tokenStore.clear();
      window.dispatchEvent(new Event("auth:expired"));
    }
    throw new ApiError(res.status, data?.message ?? "Request failed", data?.fieldErrors);
  }
  return data;
}

export const api = {
  register: (fullName, email, password) =>
    request("/api/auth/register", { method: "POST", auth: false, body: { fullName, email, password } }),
  login: (email, password) =>
    request("/api/auth/login", { method: "POST", auth: false, body: { email, password } }),
  me: () => request("/api/auth/me"),

  candidates: () => request("/api/candidates"),
  voteStatus: () => request("/api/votes/status"),
  vote: (candidateId) => request("/api/votes", { method: "POST", body: { candidateId } }),

  stats: () => request("/api/admin/stats"),
  results: () => request("/api/admin/results"),
  createCandidate: (name, party) =>
    request("/api/admin/candidates", { method: "POST", body: { name, party } }),
  removeCandidate: (id) => request(`/api/admin/candidates/${id}`, { method: "DELETE" }),
};
