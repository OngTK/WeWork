import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import LoginPage from "./pages/auth/LoginPage";
import AppLayout from "./pages/layout/AppLayout";
import MyAccountPage from "./pages/account/MyAccountPage";
import { AuthProvider, useAuth } from "./store/auth/AuthContext";
import { SnackbarProvider } from "./store/snackbar/SnackbarProvider";

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isLoading } = useAuth();
  if (isLoading) return <div />; // 로딩 스켈레톤으로 바꿔도 됨
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return <>{children}</>;
}

export default function App() {
  return (
    <>
      <SnackbarProvider>
        <AuthProvider>
          <BrowserRouter>
            <Routes>
              <Route path="/login" element={<LoginPage />} />

              <Route
                path="/"
                element={
                  <ProtectedRoute>
                    <AppLayout />
                  </ProtectedRoute>
                }
              >
                {/* <Route index element={<DashboardPage />} /> */}
                {/* 권한/직원관리 */}
                {/* 문서/결재 */}
                {/* 근태관리 */}
                {/* 설비예약 */}
                {/* 통계/대시보드 */}
                <Route path="account/me" element={<MyAccountPage />} />
              </Route>

              <Route path="*" element={<Navigate to="/" replace />} />

            </Routes>
          </BrowserRouter>
        </AuthProvider>
      </SnackbarProvider>
    </>
  );
}
