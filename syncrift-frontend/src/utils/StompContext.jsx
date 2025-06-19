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
import { useNavigate } from "react-router-dom";

// Create Battle Context
const BattleContext = createContext(null);
export const useBattle = () => useContext(BattleContext);

const StompContext = createContext(null);
export const useStomp = () => useContext(StompContext);

export const StompProvider = ({ children }) => {
  const { user, token } = useAuth();
  const navigate = useNavigate();
  const stompClientRef = useRef(null);
  const [connected, setConnected] = useState(false);
  const subscriptions = useRef([]);
  const reconnectTimeout = useRef(null);

  const [battleData, setBattleData] = useState(null);

  const updateBattleData = (data) => {
    setBattleData(data);
  };

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

        // Resubscribe to stored destinations
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

  // 👇 Custom subscription logic
  useEffect(() => {
    if (!connected) return;

    const unsubscribe = subscribeWithCleanup("/user/topic/battle/create", (message) => {
      const body = JSON.parse(message.body);
      console.log("📩 Received battle message:", body);

      if (body.message === "CREATED") {
        setBattleData(body);
        localStorage.removeItem("battleId");
        navigate("/room");
      }
    });

    return () => {
      unsubscribe?.();
    };
  }, [connected]);

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
    <BattleContext.Provider value={{ battleData, updateBattleData }}>
      <StompContext.Provider value={{ connected, send, subscribe, subscribeWithCleanup }}>
        {children}
      </StompContext.Provider>
    </BattleContext.Provider>
  );
};
