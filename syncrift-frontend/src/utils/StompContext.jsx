import React, {
  createContext,
  useContext,
  useEffect,
  useRef,
  useState,
} from "react";
import SockJS from "sockjs-client";
import { Stomp } from "@stomp/stompjs";
import { useAuth } from "./AuthContext";

const StompContext = createContext(null);
export const useStomp = () => useContext(StompContext);

export const StompProvider = ({ children }) => {
  const { user, token } = useAuth();
  const stompClientRef = useRef(null);
  const [connected, setConnected] = useState(false);
  const subscriptions = useRef([]);
  const reconnectTimeout = useRef(null);

  const connect = () => {
    const socket = new SockJS("http://localhost:8081/ws");
    const stompClient = Stomp.over(socket);

    stompClient.reconnectDelay = 0; 

    stompClient.connect(
      { Authorization: `Bearer ${token}` },
      () => {
        console.log("✅ Connected to STOMP");
        stompClientRef.current = stompClient;
        setConnected(true);

        subscriptions.current.forEach(({ destination, callback }) => {
          stompClient.subscribe(destination, callback);
        });
      },
      (error) => {
        console.error("❌ STOMP error", error);
        setConnected(false);
        attemptReconnect();
      }
    );
  };

  const attemptReconnect = () => {
    if (reconnectTimeout.current) return;

    console.log("🔁 Attempting reconnect in 3s...");
    reconnectTimeout.current = setTimeout(() => {
      reconnectTimeout.current = null;
      connect();
    }, 3000);
  };

  const subscribe = (destination, callback) => {
    const subEntry = { destination, callback };

    if (
      !subscriptions.current.some(
        (s) => s.destination === destination && s.callback === callback
      )
    ) {
      subscriptions.current.push(subEntry);
    }

    if (connected && stompClientRef.current) {
      return stompClientRef.current.subscribe(destination, callback);
    }

    return null;
  };

  const send = (destination, body) => {
    if (connected && stompClientRef.current) {
      stompClientRef.current.send(destination, {}, JSON.stringify(body));
    } else {
      console.warn("Cannot send message. STOMP not connected.");
    }
  };

  const subscribeWithCleanup = (destination, callback) => {
    const subscription = subscribe(destination, callback);
  
    return () => {
      if (subscription) {
        subscription.unsubscribe();
        subscriptions.current = subscriptions.current.filter(
          (s) => s.destination !== destination || s.callback !== callback
        );
        console.log(`🧹 Unsubscribed from ${destination}`);
      }
    };
  };

  useEffect(() => {
    if (!token) return;

    connect();

    return () => {
      if (reconnectTimeout.current) clearTimeout(reconnectTimeout.current);
      if (stompClientRef.current?.connected) {
        stompClientRef.current.disconnect(() => {
          console.log("👋 Disconnected from STOMP");
        });
      }
    };
  }, [token]);

  return (
    <StompContext.Provider value={{ connected, send, subscribe, subscribeWithCleanup }}>
      {children}
    </StompContext.Provider>
  );
};
