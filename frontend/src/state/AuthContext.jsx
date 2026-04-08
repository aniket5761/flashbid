/* eslint-disable react-refresh/only-export-components */
import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";

const AuthContext = createContext(null);

const initialUser = (() => {
  const raw = window.localStorage.getItem("flashbid_user");
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
})();

export function AuthProvider({ children }) {
  const [token, setToken] = useState(window.localStorage.getItem("flashbid_token"));
  const [user, setUser] = useState(initialUser);

  useEffect(() => {
    if (token) {
      window.localStorage.setItem("flashbid_token", token);
    } else {
      window.localStorage.removeItem("flashbid_token");
    }
  }, [token]);

  useEffect(() => {
    if (user) {
      window.localStorage.setItem("flashbid_user", JSON.stringify(user));
    } else {
      window.localStorage.removeItem("flashbid_user");
    }
  }, [user]);

  const login = useCallback((authPayload) => {
    setToken(authPayload.token);
    setUser(authPayload.user);
  }, []);

  const updateUser = useCallback((nextUser) => {
    setUser(nextUser);
  }, []);

  const logout = useCallback(() => {
    setToken(null);
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({
      token,
      user,
      isAuthenticated: Boolean(token),
      login,
      updateUser,
      logout
    }),
    [login, logout, token, updateUser, user]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) {
    throw new Error("useAuth must be used inside AuthProvider.");
  }
  return value;
}
