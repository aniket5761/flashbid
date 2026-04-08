import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "",
  headers: {
    "Content-Type": "application/json"
  }
});

api.interceptors.request.use((config) => {
  const token = window.localStorage.getItem("flashbid_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export function toApiDateTime(value) {
  if (!value) {
    return "";
  }
  return value.length === 16 ? `${value}:00` : value;
}

export function getErrorMessage(error) {
  const data = error?.response?.data;
  const message = typeof data?.message === "string" ? data.message.trim() : "";
  const details = typeof data?.details === "string" ? data.details.trim() : "";
  const hasUsableDetails = details && !details.startsWith("uri=");

  if (message === "Validation failed!" && hasUsableDetails) {
    return details;
  }

  if (hasUsableDetails && message) {
    return `${message} ${details}`;
  }

  if (hasUsableDetails) {
    return details;
  }

  return (
    message ||
    error?.message ||
    "Something went wrong."
  );
}

export default api;
