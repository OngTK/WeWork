import http from "./http";
import type { MyAccount } from "../store/auth/AuthContext";

export const accountApi = {
  // 🍃 [ACCOUNT_001] 내 정보 조회
  async me(): Promise<MyAccount> {
    const { data } = await http.get("/api/account/me");
    return data;
  },
};
