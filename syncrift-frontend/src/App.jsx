import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Nav from "./components/Navbar";
import LandingPage from "./pages/LandingPage";
import { AuthProvider, useAuth } from "./utils/AuthContext";
import { ProtectedRoute } from "./utils/ProtectedRoute";
import { Toaster } from "sonner";
import FriendsPage from "./pages/FriendsPage";
import { StompProvider } from "./utils/StompContext";
import { LoginProvider } from "./utils/LoginContext";

function AppContent() {
  const { loading } = useAuth();

  if (loading) {
    return (
      <div className="flex items-center justify-center h-screen">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-gray-900" />
      </div>
    );
  }

  return (
    <>
      <Toaster position="bottom-right" richColors />
      <Nav />
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route element={<ProtectedRoute />}>
          <Route path="/friends" element={<FriendsPage />} />
        </Route>
      </Routes>
    </>
  );
}

function App() {
  return (
    <AuthProvider>
      <StompProvider>
        <LoginProvider>
          <AppContent />
        </LoginProvider>
      </StompProvider>
    </AuthProvider>
  );
}

export default App;
