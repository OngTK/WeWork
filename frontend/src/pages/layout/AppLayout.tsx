import { Outlet } from "react-router-dom";
import { Box } from "@mui/joy";
import AppHeader from "./AppHeader";

export default function AppLayout() {
  return (
    <Box sx={{ minHeight: "100vh", backgroundColor: "#f6f7f9" }}>
      <AppHeader />
      <Box sx={{ p: 2 }}>
        <Outlet />
      </Box>
    </Box>
  );
}
