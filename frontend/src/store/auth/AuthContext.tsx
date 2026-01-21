import React, { createContext, useContext, useEffect, useMemo, useState } from "react";
import { authApi } from "../../api/authApi";
import { accountApi } from "../../api/accountApi";

export type MyAccount = {
  empId: number;
  name: string;
  deptName?: string;
  roles: string[];        // 예: ["SUPER_ADMIN", "MANAGER"]
  permissions?: string[]; // 예: ["AUTH.LOGIN", "ACCOUNT.READ", ...] 또는 CRUD코드
};

type AuthState = {
  isLoading: boolean;
  isAuthenticated: boolean;
  account: MyAccount | null;
  login: (loginId: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  refreshMe: () => Promise<void>;
};

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [isLoading, setIsLoading] = useState(true);
  const [account, setAccount] = useState<MyAccount | null>(null);

  const isAuthenticated = !!account;

  async function refreshMe() {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      setAccount(null);
      return;
    }
    const me = await accountApi.me();
    setAccount(me);
  }

  async function login(loginId: string, password: string) {
    const res = await authApi.login({ loginId, password });
    // 서버 응답 구조에 맞게 조정: accessToken 필드명 등
    localStorage.setItem("accessToken", res.accessToken);
    await refreshMe();
  }

  async function logout() {
    try {
      await authApi.logout();
    } finally {
      localStorage.removeItem("accessToken");
      setAccount(null);
    }
  }

  useEffect(() => {
    (async () => {
      try {
        await refreshMe();
      } finally {
        setIsLoading(false);
      }
    })();
  }, []);

  const value = useMemo<AuthState>(
    () => ({
      isLoading,
      isAuthenticated,
      account,
      login,
      logout,
      refreshMe,
    }),
    [isLoading, isAuthenticated, account]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("AuthProvider가 필요합니다.");
  return ctx;
}
