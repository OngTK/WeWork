import http from "./http";
import type { MyAccount, SexCode } from "../store/auth/AuthContext";


export const accountApi = {

  // 🍃 [ACCOUNT_001] 내 정보 조회
  async me(): Promise<MyAccount> {
    const { data } = await http.get("/api/account/me");
    return data;
  },

  // 🍃 [ACCOUNT_002] 내 정보 수정 
  async updateMyInfo(
    body: {
      name: string;
      birthday: string;
      email: string;
      sex: SexCode;
    }):
    Promise<void> {
    await http.put("/api/account/me", body);
  },
};
