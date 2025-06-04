import React, { createContext, useState, useContext } from 'react';

const LoginContext = createContext();

export function useLogin() {
  return useContext(LoginContext);
}

export function LoginProvider({ children }) {
  const [showLogin, setShowLogin] = useState(false);

  return (
    <LoginContext.Provider value={{ showLogin, setShowLogin }}>
      {children}
    </LoginContext.Provider>
  );
}