import { useState, useEffect } from "react";
import { X, Check } from "lucide-react";
import { Button } from "@/components/ui/button";
import { ScrollArea } from "@/components/ui/scroll-area";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { useAuth } from "../utils/AuthContext";
import SockJS from "sockjs-client";
import * as Stomp from "stompjs";
import { cn } from "@/lib/utils";
import { useStomp } from "@/utils/StompContext";

export default function NotificationPopup({
  children,
  notifications,
  setNotifications,
}) {
  const [open, setOpen] = useState(false);
  const { user, token, fetchFriends } = useAuth();
  const { connected, subscribeWithCleanup } = useStomp();

  console.log("NotificationPopup Render:", { user, notifications, open });

  useEffect(() => {
    if (!connected) return;

    const notifUnsub = subscribeWithCleanup("/user/topic/notifications", (msg) => {
      console.log("Received Notification:", msg.body);
      try {
        const data = JSON.parse(msg.body);
        console.log("Parsed Notification Data:", data.senderUsername);
        addNotification({
          id: data.id || Date.now().toString(),
          type: data.type,
          from: data.senderUsername,
          challengeId: data.challengeId,
          content: data.message,
        });
      } catch (error) {
        console.error("Error processing notification:", error);
      }
    });

    const challengeUnsub = subscribeWithCleanup("/user/topic/challenge", (message) => {
      console.log("Received Challenge:", message.body);
      try {
        const data = JSON.parse(message.body);
        console.log("Parsed Challenge Data:", data);
        addNotification({
          id: `challenge-${data.challengeId}`,
          type: "challenge",
          from: data.senderUsername,
          challengeId: data.challengeId,
          content: "You received a new challenge!",
        });
      } catch (error) {
        console.error("Error processing challenge:", error);
      }
    });

    return () => {
      notifUnsub();
      challengeUnsub();
    };
  }, [connected]);

  const addNotification = (notification) => {
    setNotifications((prev) => {
      const exists = prev.some((n) => n.id === notification.id);
      if (!exists) {
        const newNotifications = [
          ...prev,
          {
            ...notification,
            read: false,
            time: new Date().toLocaleTimeString(),
          },
        ];
        console.log("Updated Notifications:", newNotifications);
        return newNotifications;
      }
      return prev;
    });
  };

  const handleAccept = async (id) => {
    console.log("Accepting Notification:", id);
    const notification = notifications.find((n) => n.id === id);
    if (!notification) {
      console.error("Notification not found:", id);
      return;
    }

    try {
      if (notification.type === "challenge" && notification.challengeId) {
        console.log("Accepting Challenge:", notification.challengeId);
        const response = await fetch(
          `http://localhost:8081/api/challenges/${notification.challengeId}/accept`,
          {
            method: "POST",
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );
        console.log("Challenge Accept Response:", response.status);
      } else if (notification.type === "friend_request") {
        console.log("Accepting Friend Request from:", notification);
        const response = await fetch(
          `http://localhost:8081/api/friends/request/${notification.from}/accept`,
          {
            method: "PUT",
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );

        // Refresh friends list in global state
        await fetchFriends();
      }

      setNotifications((prev) => prev.filter((n) => n.id !== id));
    } catch (error) {
      console.error("Error accepting notification:", error);
    }
  };

  const handleDecline = async (id) => {
    console.log("Declining Notification:", id);
    const notification = notifications.find((n) => n.id === id);
    if (!notification) {
      console.error("Notification not found:", id);
      return;
    }

    try {
      if (notification.type === "challenge" && notification.challengeId) {
        console.log("Declining Challenge:", notification.challengeId);
        const response = await fetch(
          `http://localhost:8081/api/challenges/${notification.challengeId}/decline`,
          {
            method: "POST",
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );
      } else if (notification.type === "friend_request") {
        console.log("Declining Friend Request from:", notification.from);
        const response = await fetch(
          `http://localhost:8081/api/friends/request/${notification.from}/decline`,
          {
            method: 'DELETE',
            headers: {
              'Authorization': `Bearer ${token}`
            }
          }
        );

        if (!response.ok) throw new Error('Failed to decline request');

        // Optionally refresh friends list if needed
        await fetchFriends();
      }

      setNotifications((prev) => prev.filter((n) => n.id !== id));
    } catch (error) {
      console.error("Error declining notification:", error);
    }
  };

  const markAllAsRead = () => {
    console.log("Marking all notifications as read");
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
  };

  const unreadCount = notifications.filter((n) => !n.read).length;

  const getNotificationContent = (notification) => {
    switch (notification.type) {
      case "friend_request":
        return {
          title: "Friend Request",
          description: `${notification.from} sent you a friend request`,
          action: true,
        };
      case "challenge":
        return {
          title: "New Challenge",
          description: `${notification.from} challenged you to a game`,
          action: true,
        };
      case "message":
        return {
          title: "New Message",
          description: notification.content,
          action: false,
        };
      default:
        return {
          title: "Notification",
          description: notification.content,
          action: false,
        };
    }
  };

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <div className="relative">
          {children}
          {unreadCount > 0 && (
            <Badge
              variant="destructive"
              className="absolute -top-1 -right-1 h-5 w-5 flex items-center justify-center p-0"
            >
              {unreadCount}
            </Badge>
          )}
        </div>
      </PopoverTrigger>
      <PopoverContent
        className="w-80 p-0"
        align="end"
        sideOffset={5}
      >
        <div className="flex flex-col max-h-[400px]">
          <div className="flex items-center justify-between p-4 border-b sticky top-0 bg-popover z-10">
            <h4 className="font-medium">Notifications</h4>
            {unreadCount > 0 && (
              <Button
                variant="ghost"
                size="sm"
                className="text-xs"
                onClick={markAllAsRead}
              >
                Mark all as read
              </Button>
            )}
          </div>

          <ScrollArea className="h-[350px]">
            {notifications.length === 0 ? (
              <div className="text-center p-4 text-muted-foreground">
                No notifications
              </div>
            ) : (
              <div className="py-2">
                {notifications.map((notification) => {
                  const { title, description, action } =
                    getNotificationContent(notification);
                  return (
                    <div
                      key={notification.id}
                      className={cn(
                        "px-4 py-3 border-b last:border-b-0",
                        !notification.read && "bg-muted/50"
                      )}
                    >
                      <div className="flex justify-between items-start mb-1">
                        <AlertTitle className="text-sm font-medium">
                          {title}
                        </AlertTitle>
                        <span className="text-xs text-muted-foreground">
                          {notification.time}
                        </span>
                      </div>
                      <AlertDescription className="text-xs text-muted-foreground mb-2">
                        {description}
                      </AlertDescription>
                      {action && (
                        <div className="flex space-x-2 mt-2">
                          <Button
                            size="sm"
                            className="h-8 text-xs flex-1"
                            onClick={() => handleAccept(notification.id)}
                          >
                            <Check className="h-3 w-3 mr-1" /> Accept
                          </Button>
                          <Button
                            size="sm"
                            variant="outline"
                            className="h-8 text-xs flex-1"
                            onClick={() => handleDecline(notification.id)}
                          >
                            <X className="h-3 w-3 mr-1" /> Decline
                          </Button>
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            )}
          </ScrollArea>
        </div>
      </PopoverContent>
    </Popover>
  );
}