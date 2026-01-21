import http from "./http";

type LoginReq = { loginId: string; password: string };
type LoginRes = { accessToken: string }; // 서버 응답에 맞게 수정

export const authApi = {
  
  // 🍃 [AUTH_010] 로그인
  async login(body: LoginReq): Promise<LoginRes> {
    const { data } = await http.post("/api/auth/login", body);
    return data;
  },
  // 🍃 [AUTH_011] 로그아웃
  async logout(): Promise<void> {
    await http.post("/api/auth/logout");
  },
};
