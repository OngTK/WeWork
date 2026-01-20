import { Link as RouterLink } from "react-router-dom";
import { Box, Link, Stack, Typography, Button } from "@mui/joy";
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
  // 1) SUPER_ADMIN은 전부 노출
  if (roles.includes("SUPER_ADMIN")) return true;

  // 2) permissions 기반 제어 (권장)
  // 예: permissions에 "MENU.ADMIN" 같은 값을 넣어두면 정확해짐
  if (permissions && permissions.includes(`MENU.${menuKey}`)) return true;

  // 3) roles 기반 간단 제어(임시)
  // 예: MANAGER는 일부만
  if (roles.includes("MANAGER")) {
    return menuKey !== "ADMIN";
  }

  // 기본: 최소 메뉴만
  return menuKey === "DASHBOARD";
}

export default function AppHeader() {
  const { account, logout } = useAuth();
  if (!account) return null;

  const visibleMenus = ALL_MENUS.filter((m) => canSeeMenu(m.key, account.roles, account.permissions));

  return (
    <Box sx={{ backgroundColor: "white", borderBottom: "1px solid", borderColor: "neutral.200" }}>
      <Stack
        direction="row"
        alignItems="center"
        justifyContent="space-between"
        sx={{ px: 2, py: 1.2, maxWidth: 1280, mx: "auto" }}
      >
        <Stack direction="row" alignItems="center" spacing={2}>
          <Box component="img" src="/WeWork_logo.png" alt="WeWork" sx={{ height: 42 }} />
          <Stack direction="row" spacing={3} sx={{ ml: 1 }}>
            {visibleMenus.map((m) => (
              <Link key={m.key} component={RouterLink} to={m.to} underline="none" sx={{ fontWeight: 600 }}>
                {m.label}
              </Link>
            ))}
          </Stack>
        </Stack>

        <Stack direction="row" spacing={1.5} alignItems="center">
          <Box sx={{ textAlign: "right" }}>
            <Typography level="title-sm">{account.name} 님</Typography>
            <Typography level="body-xs" sx={{ color: "neutral.500" }}>
              {account.roles.join(", ")}
            </Typography>
          </Box>
          <Button size="sm" variant="outlined" onClick={logout}>
            로그아웃
          </Button>
        </Stack>
      </Stack>
    </Box>
  );
}
