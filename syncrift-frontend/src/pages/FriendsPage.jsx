import { useState, useEffect, useRef } from "react";
import {
  Search,
  UserPlus,
  Check,
  X,
  Users,
  Clock,
  RefreshCw,
  UserMinus,
} from "lucide-react";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { useAuth } from "../utils/AuthContext";
import { useStomp } from "@/utils/StompContext";

export default function FriendsPage() {
  const [searchQuery, setSearchQuery] = useState('');
  const [newFriendUsername, setNewFriendUsername] = useState('');
  const [isAddFriendDialogOpen, setIsAddFriendDialogOpen] = useState(false);
  const [statusFilter, setStatusFilter] = useState("all");
  const [removeFriendUsername, setRemoveFriendUsername] = useState(null);
  const [isRemoveDialogOpen, setIsRemoveDialogOpen] = useState(false);
  const [error, setError] = useState(null);
  
  const { 
    user, 
    token, 
    friends, 
    setFriends,
    pendingRequests, 
    fetchFriends, 
    fetchPendingRequests,
    loading 
  } = useAuth();

  const { connected, subscribeWithCleanup } = useStomp();

  useEffect(() => {
    fetchFriends();
    fetchPendingRequests();

    if (!connected) return;

    const unsubscribe = subscribeWithCleanup(
      "/user/topic/user/status",
      (msg) => {
        try {
          const data = JSON.parse(msg.body);
          // Update the friend's status without showing a notification
          setFriends((prevFriends) =>
            prevFriends.map((friend) =>
              friend.username === data.username
                ? { ...friend, status: data.status }
                : friend
            )
          );
        } catch (err) {
          console.error("Error parsing status update:", err);
        }
      }
    );

    return unsubscribe;
  }, [connected]);

  const handleAcceptRequest = async (username) => {
    try {
      const response = await fetch(
        `http://localhost:8081/api/friends/request/${username}/accept`,
        {
          method: "PUT",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) throw new Error('Failed to accept request');
      
      await fetchFriends();
      await fetchPendingRequests();
    } catch (err) {
      setError("Error accepting friend request. Please try again.");
      console.error(err);
    }
  };

  const handleDeclineRequest = async (username) => {
    try {
      const response = await fetch(
        `http://localhost:8081/api/friends/request/${username}/decline`,
        {
          method: "DELETE",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) throw new Error("Failed to decline request");
      
      await fetchPendingRequests();
    } catch (err) {
      setError("Error declining friend request. Please try again.");
      console.error(err);
    }
  };

  const handleSendFriendRequest = async () => {
    if (!newFriendUsername.trim()) return;

    try {
      const response = await fetch(
        "http://localhost:8081/api/friends/request",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({ username: newFriendUsername }),
        }
      );

      if (!response.ok) throw new Error("Failed to send friend request");

      setNewFriendUsername("");
      setIsAddFriendDialogOpen(false);
    } catch (err) {
      setError("Error sending friend request. Please try again.");
      console.error(err);
    }
  };

  const handleRemoveFriend = async () => {
    if (!removeFriendUsername) return;

    try {
      const response = await fetch(
        `http://localhost:8081/api/friends/${removeFriendUsername}`,
        {
          method: "DELETE",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) throw new Error("Failed to remove friend");
      
      await fetchFriends();
      setIsRemoveDialogOpen(false);
      setRemoveFriendUsername(null);
    } catch (err) {
      setError("Error removing friend. Please try again.");
      console.error(err);
    }
  };

  const openRemoveDialog = (username) => {
    setRemoveFriendUsername(username);
    setIsRemoveDialogOpen(true);
  };

  const filteredFriends = friends.filter((friend) => {
    const fullName = `${friend.firstName} ${friend.lastName}`.toLowerCase();
    const username = friend.username.toLowerCase();
    const query = searchQuery.toLowerCase();
    const matchesSearch = fullName.includes(query) || username.includes(query);

    if (statusFilter === "all") {
      return matchesSearch;
    } else {
      return matchesSearch && friend.status === statusFilter.toUpperCase();
    }
  });

  return (
    <div className="container mx-auto py-6 max-w-4xl">
      <h1 className="text-3xl font-bold mt-14 mb-6">Friends</h1>

      {error && (
        <Alert className="mb-4 bg-red-50" variant="destructive">
          <AlertTitle>Error</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <Tabs defaultValue="friends">
        <div className="flex justify-between items-center mb-4">
          <TabsList>
            <TabsTrigger value="friends" className="px-4">
              <Users className="w-4 h-4 mr-2" />
              Friends
            </TabsTrigger>
            <TabsTrigger value="pending" className="px-4">
              <Clock className="w-4 h-4 mr-2" />
              Pending
              {pendingRequests.length > 0 && (
                <Badge className="ml-2 bg-red-500">
                  {pendingRequests.length}
                </Badge>
              )}
            </TabsTrigger>
          </TabsList>

          <div className="flex space-x-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => {
                fetchFriends();
                fetchPendingRequests();
              }}
            >
              <RefreshCw className="w-4 h-4 mr-2" />
              Refresh
            </Button>

            <Dialog
              open={isAddFriendDialogOpen}
              onOpenChange={setIsAddFriendDialogOpen}
            >
              <DialogTrigger asChild>
                <Button size="sm">
                  <UserPlus className="w-4 h-4 mr-2" />
                  Add Friend
                </Button>
              </DialogTrigger>
              <DialogContent>
                <DialogHeader>
                  <DialogTitle>Add Friend</DialogTitle>
                  <DialogDescription>
                    Enter the username of the person you want to add as a
                    friend.
                  </DialogDescription>
                </DialogHeader>
                <Input
                  placeholder="Username"
                  value={newFriendUsername}
                  onChange={(e) => setNewFriendUsername(e.target.value)}
                />
                <DialogFooter>
                  <Button
                    variant="outline"
                    onClick={() => setIsAddFriendDialogOpen(false)}
                  >
                    Cancel
                  </Button>
                  <Button onClick={handleSendFriendRequest}>
                    Send Request
                  </Button>
                </DialogFooter>
              </DialogContent>
            </Dialog>
          </div>
        </div>

        <TabsContent value="friends">
          <div className="flex gap-2 mb-4">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
              <Input
                placeholder="Search friends..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-9"
              />
            </div>
            <Select value={statusFilter} onValueChange={setStatusFilter}>
              <SelectTrigger className="w-40">
                <SelectValue placeholder="Filter" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Friends</SelectItem>
                <SelectItem value="online">Online</SelectItem>
                <SelectItem value="in-battle">In Battle</SelectItem>
                <SelectItem value="offline">Offline</SelectItem>
              </SelectContent>
            </Select>
          </div>

          {loading ? (
            <div className="flex justify-center py-10">
              <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-gray-900"></div>
            </div>
          ) : (
            <div className="border rounded-md">
              {filteredFriends.length === 0 ? (
                <div className="py-10 text-center">
                  <p className="text-gray-500">No friends found</p>
                </div>
              ) : (
                <div className="divide-y">
                  {filteredFriends.map((friend) => (
                    <FriendListItem
                      key={friend.id}
                      friend={friend}
                      onRemove={() => openRemoveDialog(friend.username)}
                    />
                  ))}
                </div>
              )}
            </div>
          )}
        </TabsContent>

        <TabsContent value="pending">
          {pendingRequests.length === 0 ? (
            <div className="text-center py-10">
              <h3 className="text-lg font-medium">
                No pending friend requests
              </h3>
              <p className="text-gray-500 mt-2">
                When someone sends you a friend request, it will appear here.
              </p>
            </div>
          ) : (
            <div className="border rounded-md divide-y">
              {pendingRequests.map((request) => (
                <div
                  key={request.id}
                  className="flex items-center justify-between p-4"
                >
                  <div className="flex items-center gap-3">
                    <Avatar>
                      <AvatarFallback>
                        {request.firstName?.[0]}
                        {request.lastName?.[0]}
                      </AvatarFallback>
                    </Avatar>
                    <div>
                      <p className="font-medium">
                        {request.firstName} {request.lastName}
                      </p>
                      <p className="text-sm text-gray-500">
                        @{request.username}
                      </p>
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleDeclineRequest(request.username)}
                    >
                      <X className="w-4 h-4 mr-1" /> Decline
                    </Button>
                    <Button
                      size="sm"
                      onClick={() => handleAcceptRequest(request.username)}
                    >
                      <Check className="w-4 h-4 mr-1" /> Accept
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </TabsContent>
      </Tabs>

      <AlertDialog open={isRemoveDialogOpen} onOpenChange={setIsRemoveDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Remove Friend</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to remove this friend? This action cannot be
              undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel onClick={() => setRemoveFriendUsername(null)}>
              Cancel
            </AlertDialogCancel>
            <AlertDialogAction onClick={handleRemoveFriend}>
              Remove
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}

function FriendListItem({ friend, onRemove }) {
  const statusColor =
    {
      ONLINE: "bg-green-500",
      OFFLINE: "bg-gray-300",
      "IN-BATTLE": "bg-yellow-500",
    }[friend.status] || "bg-gray-300";

  const statusText =
    {
      ONLINE: "Online",
      OFFLINE: "Offline",
      "IN-BATTLE": "In Battle",
    }[friend.status] || "Unknown";

  return (
    <div className="flex items-center justify-between p-4 hover:bg-neutral-900">
      <div className="flex items-center gap-3">
        <div className="relative">
          <Avatar>
            <AvatarFallback>
              {friend.firstName?.[0]}
              {friend.lastName?.[0]}
            </AvatarFallback>
          </Avatar>
          <span
            className={`absolute bottom-0 right-0 block h-3 w-3 rounded-full ${statusColor} ring-2 ring-white`}
          />
        </div>
        <div>
          <p className="font-medium">
            {friend.firstName} {friend.lastName}
          </p>
          <div className="flex items-center gap-2">
            <p className="text-sm text-gray-500">@{friend.username}</p>
            <span className="text-xs text-gray-400">• {statusText}</span>
          </div>
        </div>
      </div>
      <Button variant="outline" size="sm" onClick={onRemove}>
        <UserMinus className="w-4 h-4 mr-1" /> Remove
      </Button>
    </div>
  );
}
