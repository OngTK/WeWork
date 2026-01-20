import http from "./http";
import type { MyAccount } from "../store/auth/AuthContext";

export const accountApi = {
  async me(): Promise<MyAccount> {
    const { data } = await http.get("/api/account/me");
    return data;
  },
};
