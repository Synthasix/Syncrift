"use client";
import { Link, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Bell, UserCircle, Users, LogOut } from "lucide-react";
import { useAuth } from "../utils/AuthContext";
import { useState } from "react";
import LoginPage from "./LoginPage";
import SignUp from "./SignUp";
import NotificationPopup from "./NotificationPopup";
import { useLogin } from "@/utils/LoginContext";

export default function Navbar() {
  // const [showLogin, setShowLogin] = useState(false);
  const {showLogin, setShowLogin} = useLogin();
  const [showSignup, setShowSignup] = useState(false); 
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handlefriendOnclick = (e) =>{
    e.preventDefault();
    navigate('/friends');
  }
  
  // Initialize with empty notifications array
  const [notifications, setNotifications] = useState([]);


  return (
    <>
      <header className="fixed top-0 w-full z-50 backdrop-blur-md bg-black/30 border-b border-white/10 shadow-sm">
        <div className="container mx-auto flex h-16 items-center justify-between px-2">
          <Link to="/" className="text-xl font-bold tracking-tight text-white">
            Syncrift
          </Link>

          <div className="flex items-center space-x-4">
            {!user ? (
              <>
                <Button
                  variant="outline"
                  className="border-white/10 text-white hover:bg-white/40 hover:text-black"
                  onClick={() => {
                    setShowLogin(true);
                  }}
                >
                  Login
                </Button>
                <Button
                  className="bg-white/10 text-white hover:bg-white/20"
                  onClick={() => {
                    setShowSignup(true);
                  }}
                >
                  Sign Up
                </Button>
              </>
            ) : (
              <>
                <NotificationPopup 
                  notifications={notifications} 
                  setNotifications={setNotifications}
                >
                  <Button
                    variant="ghost"
                    size="icon"
                    className="text-white hover:bg-white/10 relative"
                  >
                    <Bell className="h-5 w-5" />
                  </Button>
                </NotificationPopup>

                <Button
                  variant="ghost"
                  size="icon"
                  className="text-white hover:bg-white/10"
                  onClick={handlefriendOnclick}
                >
                  <Users className="h-5 w-5" />
                </Button>
                <Button
                  variant="ghost"
                  size="icon"
                  className="text-white hover:bg-white/10"
                >
                  <UserCircle className="h-6 w-6" />
                </Button>
                <Button
                  variant="ghost"
                  size="icon"
                  className="text-white hover:bg-white/10"
                  onClick={logout}
                >
                  <LogOut className="h-6 w-6" />
                </Button>
              </>
            )}
          </div>
        </div>
      </header>

      {/* Login Overlay */}
      {showLogin && (
        <div className="fixed inset-0 z-[60]">
          <div
            className="absolute inset-0 bg-black/30 backdrop-blur-lg"
            onClick={() => setShowLogin(false)}
          />
          <div className="relative flex items-center justify-center h-full w-full p-4">
            {console.log(showLogin)}
            <LoginPage onClose={() => setShowLogin(false)} />
          </div>
        </div>
      )}

      {/* Signup Overlay */}
      {showSignup && (
        <div className="fixed inset-0 z-[60]">
          <div
            className="absolute inset-0 bg-black/30 backdrop-blur-lg"
            onClick={() => setShowSignup(false)}
          />
          <div className="relative flex items-center justify-center h-full w-full p-4">
            <SignUp onClose={() => setShowSignup(false)} />
          </div>
        </div>
      )}
    </>
  );
}