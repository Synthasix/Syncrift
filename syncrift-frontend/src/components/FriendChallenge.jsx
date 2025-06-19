import { useState } from 'react';
import { Search, GamepadIcon, X } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Avatar, AvatarImage, AvatarFallback } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { useAuth } from '@/utils/AuthContext';

export default function FriendChallenge({ battleType, onClose }) {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedFriend, setSelectedFriend] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const { token, friends } = useAuth();
  
  console.log(friends);

  const onlineFriends = friends.filter(friend =>
    friend.status == "ONLINE" &&
    friend.username.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const sendChallengeRequest = async () => {
    if (!selectedFriend) return;
    
    setIsLoading(true);
    
    try {
      const response = await fetch('http://localhost:8081/api/challenges/create', {
        method: 'POST',
        headers: {
          Authorization : `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username: selectedFriend.username,
          eventType: battleType
        }),
      });
      
      if (!response.ok) {
        throw new Error('Failed to send challenge request');
      }
      
      // Handle successful response
      const data = await response.json();
      console.log('Challenge created:', data);
      
      // Close the modal after successful request
      onClose();
    } catch (error) {
      console.error('Error sending challenge:', error);
      // You could add error handling UI here
    } finally {
      setIsLoading(false);
      setSelectedFriend(null);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center from-black/80 via-gray-900/80 to-gray-800/80 backdrop-blur-sm">
      {!selectedFriend ? (
        <Card className="relative w-full max-w-md from-gray-900 via-black to-gray-900 text-white border border-gray-800">
          <Button
            variant="ghost"
            size="icon"
            className="absolute right-4 top-4 h-8 w-8 text-gray-400 hover:bg-gray-800/50"
            onClick={onClose}
          >
            <X className="h-4 w-4" />
          </Button>

          <CardHeader>
            <CardTitle className="text-3xl font-bold text-gray-100">
              Challenge a Friend
            </CardTitle>
            <p className="text-sm text-gray-400">
              Select an online friend to start a {battleType} battle
            </p>
          </CardHeader>

          <CardContent>
            <div className="relative mb-6">
              <div className="absolute inset-y-0 left-3 flex items-center pointer-events-none">
                <Search size={16} className="text-gray-500" />
              </div>
              <Input
                type="text"
                placeholder="Search online friends..."
                className="pl-10 pr-4 bg-white/10 border-white/20 text-white placeholder:text-white/70 focus-visible:ring-purple-500"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>

            <div className="mb-4 flex items-center justify-between">
              <Badge variant="outline" className="bg-gray-800 text-green-400 border-green-500 flex gap-2 items-center">
                <span className="h-2 w-2 bg-green-500 rounded-full"></span>
                <span>{onlineFriends.length} Online</span>
              </Badge>
            </div>

            <div className="overflow-y-auto max-h-96 space-y-3">
              {onlineFriends.length > 0 ? (
                onlineFriends.map(friend => (
                  <div
                    key={friend.id}
                    className="flex items-center justify-between p-3 rounded-lg bg-gray-800/30 hover:bg-gray-800/50 transition-colors border border-gray-800"
                  >
                    <div className="flex items-center gap-3">
                      <div className="relative">
                        <Avatar className="border border-gray-700">
                          <AvatarImage src={friend.profilePicture} alt={friend.username} />
                          <AvatarFallback className="bg-gray-700">{friend.username.charAt(0)}</AvatarFallback>
                        </Avatar>
                        <span className="absolute bottom-0 right-0 w-3 h-3 bg-green-500 rounded-full border-2 border-gray-900"></span>
                      </div>
                      <div>
                        <h3 className="font-medium text-white">{friend.username}</h3>
                        <p className="text-sm text-gray-400">
                          Online
                        </p>
                      </div>
                    </div>
                    <Button
                      variant="default"
                      className="bg-purple-600 hover:bg-purple-700 flex items-center gap-1"
                      onClick={() => setSelectedFriend(friend)}
                    >
                      <GamepadIcon size={16} />
                      <span>Challenge</span>
                    </Button>
                  </div>
                ))
              ) : (
                <div className="text-center py-8 text-gray-500 border border-gray-800 rounded-lg bg-gray-900/30">
                  <p>No online friends found</p>
                  {searchQuery && <p className="text-sm mt-2">Try a different search term</p>}
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      ) : (
        <Card className="relative w-full max-w-sm from-gray-900 via-black to-gray-900 text-white border border-gray-800">
          <Button
            variant="ghost"
            size="icon"
            className="absolute right-4 top-4 h-8 w-8 text-gray-400 hover:bg-gray-800/50"
            onClick={() => setSelectedFriend(null)}
          >
            <X className="h-4 w-4" />
          </Button>

          <CardHeader>
            <CardTitle className="text-2xl font-bold text-gray-100">
              Challenge {selectedFriend.username}
            </CardTitle>
            <p className="text-sm text-gray-400">
              Send a {battleType} battle request
            </p>
          </CardHeader>

          <CardContent>
            <div className="flex items-center gap-3 mb-6 p-3 rounded-lg bg-gray-800/30 border border-gray-800">
              <Avatar className="border border-gray-700">
                <AvatarImage src={selectedFriend.profilePicture} alt={selectedFriend.username} />
                <AvatarFallback className="bg-gray-700">{selectedFriend.username.charAt(0)}</AvatarFallback>
              </Avatar>
              <div>
                <h3 className="font-medium text-white">{selectedFriend.username}</h3>
                <p className="text-sm text-gray-400">
                  Online
                </p>
              </div>
            </div>

            <div className="flex justify-end gap-2">
              <Button
                variant="outline"
                className="bg-gray-800/50 hover:bg-gray-800 text-white border-gray-700"
                onClick={() => setSelectedFriend(null)}
              >
                Cancel
              </Button>
              <Button
                variant="default"
                className="bg-purple-600 hover:bg-purple-700"
                disabled={isLoading}
                onClick={sendChallengeRequest}
              >
                {isLoading ? (
                  <span className="flex items-center gap-2">
                    <div className="h-4 w-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                    Sending...
                  </span>
                ) : (
                  "Send Challenge"
                )}
              </Button>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}