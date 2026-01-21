import { Outlet } from "react-router-dom";
import { Box } from "@mui/joy";
import AppHeader from "./AppHeader";

export default function AppLayout() {
  return (
    // [1] 1440px 바깥 영역 배경 (살짝 다른 색)
    <Box
      sx={{
        minHeight: "100vh",
        width: "100vw",
        backgroundColor: "#eef1f5", // 바깥 배경 (조금 더 어둡게)
        py: { xs: 0, md: 2 },       // 큰 화면에서 위아래 여백
      }}
    >
      {/* [2] 실제 앱 컨테이너: maxWidth 1440px */}
      <Box
        sx={{
          width: "100%",
          maxWidth: "1440px",
          mx: "auto",              // 가운데 정렬
          minHeight: "100vh",
          backgroundColor: "#f6f7f9", // 안쪽 배경(조금 밝게)
          boxShadow: { xs: "none", md: "sm" },
          borderRadius: { xs: 0, md: 16 },
          overflow: "hidden",      // header border 등 깔끔하게
          display: "flex",
          flexDirection: "column",
        }}
      >
        <AppHeader />

        {/* [3] 본문: 반응형 padding */}
        <Box
          component="main"
          sx={{
            p: { xs: 2, md: 3 },
            minHeight: "calc(100vh - 64px)", // 헤더 높이에 맞춰 조정(필요 시)
          }}
        >
          <Outlet />
        </Box>
      </Box>
    </Box>
  );
}
