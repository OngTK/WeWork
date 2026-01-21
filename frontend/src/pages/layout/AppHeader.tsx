import { Link as RouterLink } from "react-router-dom";
import { Box, Link, Stack, Typography } from "@mui/joy";
import { useAuth } from "../../store/auth/AuthContext";

type MenuItem = { label: string; to: string; key: string };

const ALL_MENUS: MenuItem[] = [
  { label: "권한·직원 관리", to: "/admin", key: "ADMIN" },
  { label: "문서·결재", to: "/docs", key: "DOCS" },
  { label: "근태관리", to: "/attendance", key: "ATTENDANCE" },
  { label: "설비 예약", to: "/facility", key: "FACILITY" },
  { label: "통계·대시보드", to: "/dashboard", key: "DASHBOARD" },
];

function canSeeMenu(menuKey: string, roles: string[], permissions?: string[]) {
  if (roles.includes("ROLE_SUPER_ADMIN")) return true;
  if (permissions && permissions.includes(`MENU.${menuKey}`)) return true;

  if (roles.includes("ROLE_MANAGER")) return menuKey !== "ADMIN";

  return menuKey === "DASHBOARD";
}

export default function AppHeader() {
  const { account, logout } = useAuth();
  if (!account) return null;

  const visibleMenus = ALL_MENUS.filter((m) =>
    canSeeMenu(m.key, account.roles, account.permissions)
  );

  return (
    <Box sx={{ backgroundColor: "white", borderBottom: "4px solid", borderColor: "neutral.200" }}>
      <Box sx={{ width: "100%", py: 1.2 }}>
        <Stack
          direction="row"
          alignItems="center"
          justifyContent="space-between"
          sx={{ px: 2, maxWidth: 1440, mx: "auto" }}
        >
          {/* 좌측: 로고 + 메뉴 */}
          <Stack direction="row" alignItems="center" spacing={2} sx={{ minWidth: 0 }}>
            <Box component="img" src="/WeWork_logo.png" alt="WeWork" sx={{ height: 42 }} />

            <Stack
              direction="row"
              spacing={4}
              sx={{
                ml: 1,
                minWidth: 0,
                flexWrap: "wrap", // 좁아지면 줄바꿈 (반응형)
                rowGap: 1,
              }}
            >
              {visibleMenus.map((m) => (
                <Link
                  key={m.key}
                  component={RouterLink}
                  to={m.to}
                  underline="none"
                  sx={{ fontWeight: 600, color: "neutral.700", whiteSpace: "nowrap" }}
                >
                  {m.label}
                </Link>
              ))}
            </Stack>
          </Stack>

          {/* 우측: 사용자 정보 + 링크(내 정보 관리 | 로그아웃) */}
          <Stack alignItems="flex-end" spacing={0.5} sx={{ flexShrink: 0 }}>
            <Typography level="title-sm" sx={{ fontWeight: 700 }}>
              {account.name} 님
            </Typography>

            {/* 아래 라인은 샘플처럼 "인사팀 · 사원" 형태를 만들고 싶으면 account에 dept/position이 있어야 합니다.
               없으면 roles를 간단히 노출하거나, 추후 me 응답에 deptName/position 추가 후 교체하세요. */}
            <Typography level="body-xs" sx={{ color: "neutral.500" }}>
              {account.deptName} | {account.position} 
            </Typography>

            <Stack direction="row" spacing={1} alignItems="center">
              <Link
                component={RouterLink}
                to="/account/me"
                underline="none"
                sx={{ color: "neutral.600", fontSize: 12 }}
              >
                내 정보 관리
              </Link>

              <Typography level="body-xs" sx={{ color: "neutral.400" }}>
                |
              </Typography>

              {/* 로그아웃은 서버 호출이 필요하므로 Link처럼 보이되 onClick 처리 */}
              <Link
                component="button"
                underline="none"
                onClick={logout}
                sx={{
                  color: "neutral.600",
                  fontSize: 12,
                  cursor: "pointer",
                  background: "none",
                  border: "none",
                  p: 0,
                  "&:hover": { textDecoration: "underline" },
                }}
              >
                로그아웃
              </Link>
            </Stack>
          </Stack>
        </Stack>
      </Box>
    </Box>
  );
}
