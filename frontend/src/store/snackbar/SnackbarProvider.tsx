import React, { createContext, useCallback, useContext, useMemo, useState } from "react";
import { Snackbar, Stack, Typography } from "@mui/joy";

// MUI Icons
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline";
import InfoOutlinedIcon from "@mui/icons-material/InfoOutlined";
import WarningAmberOutlinedIcon from "@mui/icons-material/WarningAmberOutlined";
import ErrorOutlineIcon from "@mui/icons-material/ErrorOutline";

type SnackType = "success" | "info" | "warn" | "error";

type SnackState = {
  open: boolean;
  message: string;
  type: SnackType;
  duration: number;
};

type NotifyOptions = {
  duration?: number;
};

type SnackbarContextValue = {
  success: (message: string, options?: NotifyOptions) => void;
  info: (message: string, options?: NotifyOptions) => void;
  warn: (message: string, options?: NotifyOptions) => void;
  error: (message: string, options?: NotifyOptions) => void;
};

const SnackbarContext = createContext<SnackbarContextValue | null>(null);

/** 타입별 UI 정의 */
function uiByType(type: SnackType) {
  switch (type) {
    // 🔵 성공
    case "success":
      return {
        border: "2px solid #1976d2",
        bg: "#f8fbff",
        text: "#0d47a1",
        Icon: CheckCircleOutlineIcon,
      };

    // 🟢 정보
    case "info":
      return {
        border: "2px solid #2e7d32",
        bg: "#f7fff8",
        text: "#1b5e20",
        Icon: InfoOutlinedIcon,
      };

    // 🟠 경고
    case "warn":
      return {
        border: "2px solid #ed6c02",
        bg: "#fff7ed",
        text: "#e65100",
        Icon: WarningAmberOutlinedIcon,
      };

    // 🔴 오류
    case "error":
    default:
      return {
        border: "2px solid #d32f2f",
        bg: "#fff8f8",
        text: "#b71c1c",
        Icon: ErrorOutlineIcon,
      };
  }
}

export function SnackbarProvider({ children }: { children: React.ReactNode }) {
  const [snack, setSnack] = useState<SnackState>({
    open: false,
    message: "",
    type: "info",
    duration: 2000,
  });

  // 연속 호출 시 재마운트용
  const [key, setKey] = useState(0);

  const openSnack = useCallback(
    (type: SnackType, message: string, options?: NotifyOptions) => {
      setKey((k) => k + 1);
      setSnack({
        open: true,
        type,
        message,
        duration: options?.duration ?? 2000,
      });
    },
    []
  );

  const value = useMemo<SnackbarContextValue>(
    () => ({
      success: (msg, opt) => openSnack("success", msg, opt),
      info: (msg, opt) => openSnack("info", msg, opt),
      warn: (msg, opt) => openSnack("warn", msg, opt),
      error: (msg, opt) => openSnack("error", msg, opt),
    }),
    [openSnack]
  );

  const ui = uiByType(snack.type);

  return (
    <SnackbarContext.Provider value={value}>
      {children}

      <Snackbar
        key={key}
        open={snack.open}
        onClose={() => setSnack((prev) => ({ ...prev, open: false }))}
        autoHideDuration={snack.duration}
        anchorOrigin={{ vertical: "top", horizontal: "center" }}
        variant="outlined"
        sx={{
          border: ui.border,
          backgroundColor: ui.bg,
          boxShadow: "sm",
          px: 1.5,
          py: 1,
          minWidth: { xs: "min(92vw, 520px)", md: 520 },
          maxWidth: "92vw",
        }}
      >
        <Stack direction="row" spacing={1} alignItems="center">
          <ui.Icon sx={{ color: ui.text, fontSize: 20 }} />
          <Typography level="body-sm" sx={{ color: ui.text, fontWeight: 700 }}>
            {snack.message}
          </Typography>
        </Stack>
      </Snackbar>
    </SnackbarContext.Provider>
  );
}

export function useAppSnackbar() {
  const ctx = useContext(SnackbarContext);
  if (!ctx) throw new Error("SnackbarProvider가 필요합니다.");
  return ctx;
}
