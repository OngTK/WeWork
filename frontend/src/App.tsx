import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import LoginPage from "./pages/auth/LoginPage";
import AppLayout from "./pages/layout/AppLayout";
// import DashboardPage from "./pages/DashboardPage";
import { AuthProvider, useAuth } from "./store/auth/AuthContext";

function ProtectedRoute({ children }: { children: JSX.Element }) {
  const { isAuthenticated, isLoading } = useAuth();
  if (isLoading) return <div />; // 로딩 스켈레톤으로 바꿔도 됨
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return children;
}

export default function App() {
  return (
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
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
