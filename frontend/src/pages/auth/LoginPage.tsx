import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Box, Button, Card, Divider, Input, Stack, Typography } from "@mui/joy";
import { useAuth } from "../../store/auth/AuthContext";

export default function LoginPage() {
  const navigate = useNavigate();
  const { login, isAuthenticated, isLoading } = useAuth();

  const [loginId, setLoginId] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  // 📌 이미 로그인 상태면 안내 후 홈으로 이동
  useEffect(() => {
    if (isLoading) return; // AuthProvider가 me() 확인 끝날 때까지 대기
    if (isAuthenticated) {
      // 0.8초 정도 멘트 보여주고 이동
      const t = setTimeout(() => {
        navigate("/", { replace: true });
      }, 800);
      return () => clearTimeout(t);
    }
  }, [isAuthenticated, isLoading, navigate]);

  // 📌 
  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setErrorMsg(null);
    setSubmitting(true);
    try {
      await login(loginId, password);
      navigate("/", { replace: true });
    } catch (err: any) {
      // 서버 에러 구조에 맞게 메시지 파싱 조정 가능
      setErrorMsg("아이디 또는 비밀번호를 확인해주세요.");
    } finally {
      setSubmitting(false);
    }
  }

    // ✅ 추가: 로그인 상태면 Login UI 대신 안내 화면
  if (!isLoading && isAuthenticated) {
    return (
      <Box
        sx={{
          minHeight: "100vh",
          width: "100vw",
          backgroundColor: "#f6f7f9",
          display: "grid",
          placeItems: "center",
          p: 2,
        }}
      >
        <Card variant="outlined" sx={{ width: "min(520px, 96vw)", p: 3, borderRadius: 16 }}>
          <Stack spacing={1.5} sx={{ textAlign: "center" }}>
            <Typography level="h4">이미 로그인한 상태입니다.</Typography>
            <Typography level="body-sm" sx={{ color: "neutral.500" }}>
              잠시 후 메인 화면으로 이동합니다.
            </Typography>
            <Button onClick={() => navigate("/", { replace: true })}>지금 이동</Button>
          </Stack>
        </Card>
      </Box>
    );
  }

  return (
    <Box
      sx={{
        minHeight: "100vh",
        width: "100vw",
        backgroundColor: "#f6f7f9",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
      }}
    >
      <Card
        variant="outlined"
        sx={{
          width: "100%",
          maxWidth: "1280px",   // ← 여기서만 최대폭 제어
          height: { xs: "auto", md: 560 },
          borderRadius: 16,
          boxShadow: "sm",
          p: 3,
        }}
      >
        <Stack direction={{ xs: "column", md: "row" }} sx={{ height: "100%" }} spacing={3}>
          {/* 좌측 로고 영역 */}
          <Box
            sx={{
              flex: 1,
              display: "grid",
              placeItems: "center",
              borderRadius: 12,
            }}
          >
            {/* 실제 로고 경로로 변경 */}
            <Box
              component="img"
              src="/WeWork_logo.png"
              alt="WeWork"
              sx={{ width: "min(360px, 70%)", opacity: 0.95 }}
            />
          </Box>

          <Divider orientation="vertical" sx={{ display: { xs: "none", md: "block" } }} />

          {/* 우측 로그인 폼 */}
          <Box sx={{ flex: 1, display: "grid", placeItems: "center" }}>
            <Box sx={{ width: "min(380px, 92%)" }}>
              <Typography level="h2" sx={{ mb: 3, textAlign: "center" }}>
                로그인
              </Typography>

              <form onSubmit={onSubmit}>
                <Stack spacing={2.2}>
                  <Input
                    placeholder="아이디"
                    value={loginId}
                    onChange={(e) => setLoginId(e.target.value)}
                    required
                  />
                  <Input
                    placeholder="비밀번호"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />

                  {errorMsg && (
                    <Typography level="body-sm" color="danger">
                      {errorMsg}
                    </Typography>
                  )}

                  <Button type="submit" loading={submitting} sx={{ mt: 0.5 }}>
                    로그인
                  </Button>

                  <Button
                    variant="plain"
                    onClick={() => navigate("/account/password/find")}
                    sx={{ color: "neutral.500" }}
                  >
                    비밀번호 찾기
                  </Button>
                </Stack>
              </form>
            </Box>
          </Box>
        </Stack>
      </Card>
    </Box>
  );
}
