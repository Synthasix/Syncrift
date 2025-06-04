import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Nav from "./components/Navbar";
import LandingPage from "./pages/LandingPage";
import { AuthProvider, useAuth } from "./utils/AuthContext";
import { Toaster } from "sonner";

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
      </Routes>
    </>
  );
}

function App() {
  return (
    <AuthProvider>
          <AppContent />
    </AuthProvider>
  );
}

export default App;
