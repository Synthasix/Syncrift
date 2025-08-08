import React, { useEffect, useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import {
  X,
  Users,
  Settings,
  Play,
  Code2,
  Gamepad2,
  Loader2,
} from 'lucide-react';
import { useAuth } from '@/utils/AuthContext';
import { Avatar, AvatarImage, AvatarFallback } from '@/components/ui/avatar';
import { SERVER } from "../utils/constant.js"

export default function CodeforcesBattlePopup({ onClose }) {
  const { token, friends, fetchFriends } = useAuth();
  const [config, setConfig] = useState({
    questions: 3,
    minRating: 800,
    maxRating: 1500,
    difficulty: 'medium',
    duration: 15,
    invitedFriends: []
  });
  const [isConfirming, setIsConfirming] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const server = SERVER;
  const difficultyOptions = [
    { id: 'easy', name: 'Easy', minRating: 800, maxRating: 1200, color: 'bg-green-500' },
    { id: 'medium', name: 'Medium', minRating: 1200, maxRating: 1800, color: 'bg-yellow-500' },
    { id: 'hard', name: 'Hard', minRating: 1800, maxRating: 2500, color: 'bg-red-500' }
  ];
  useEffect(() => {
    fetchFriends();
  }, [fetchFriends])
  const onlineFriends = friends.filter(friend => friend.status?.toLowerCase() === "online");

  const updateDifficulty = (difficultyId) => {
    const difficulty = difficultyOptions.find(d => d.id === difficultyId);
    setConfig(prev => ({
      ...prev,
      difficulty: difficultyId,
      minRating: difficulty.minRating,
      maxRating: difficulty.maxRating
    }));
  };

  const handleFriendSelect = (username) => {
    const friend = onlineFriends.find(f => f.username === username);
    if (friend) {
      setConfig(prev => ({ ...prev, invitedFriends: [friend] }));
    }
  };

  const handleStartBattle = () => {
    if (config.minRating > config.maxRating) {
      alert('Minimum rating cannot be greater than maximum rating!');
      return;
    }
    if (config.invitedFriends.length === 0) {
      alert('You must invite a friend to start a battle!');
      return;
    }
    setError(null);
    setIsConfirming(true);
  };

  const sendChallengeRequest = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const response = await fetch(`${server}/api/challenges/create`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          eventType: 'CF',
          username: config.invitedFriends.map(f => f.username),
          questions: config.questions,
          minRating: config.minRating,
          maxRating: config.maxRating,
          duration: config.duration,
        }),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to send challenge request');
      }

      const data = await response.json();
      console.log('Challenge created:', data);
      onClose();
    } catch (error) {
      console.error('Error sending challenge:', error);
      setError(error.message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center px-4">
      {!isConfirming ? (
        // Initial popup
        <Card className="to-gray-600 border-gray-700 w-full max-w-xl h-auto">
          {/* Header */}
          <CardHeader className="border-b border-gray-700 p-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <div className="p-1 bg-green-600/20 rounded-lg">
                  <Code2 className="w-5 h-5 text-green-400" />
                </div>
                <CardTitle className="text-white text-lg">Codeforces Battle Setup</CardTitle>
              </div>
              <Button variant="ghost" size="sm" onClick={onClose} className="text-gray-400 hover:text-white h-8 w-8 p-0">
                <X className="w-4 h-4" />
              </Button>
            </div>
          </CardHeader>

          {/* Content */}
          <CardContent className="p-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Settings Column */}
              <div className="space-y-3">
                <h3 className="text-white font-medium text-sm mb-3 flex items-center gap-1">
                  <Settings className="w-4 h-4 text-blue-400" />
                  Battle Settings
                </h3>

                {/* Questions & Duration */}
                <div className="grid grid-cols-2 gap-2">
                  <div>
                    <Label className="text-gray-300 text-xs mb-1 block">Questions</Label>
                    <Select value={config.questions.toString()} onValueChange={(value) => setConfig(prev => ({ ...prev, questions: parseInt(value) }))}>
                      <SelectTrigger className="bg-gray-800 border-gray-600 text-white h-8 text-sm">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent className="bg-gray-800 border-gray-600">
                        {[1, 3, 5, 10].map(val => (
                          <SelectItem key={val} value={val.toString()}>{val}</SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <Label className="text-gray-300 text-xs mb-1 block">Duration</Label>
                    <Select value={config.duration.toString()} onValueChange={(value) => setConfig(prev => ({ ...prev, duration: parseInt(value) }))}>
                      <SelectTrigger className="bg-gray-800 border-gray-600 text-white h-8 text-sm">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent className="bg-gray-800 border-gray-600">
                        {[5, 10, 15, 30, 45, 60].map(min => (
                          <SelectItem key={min} value={min.toString()}>{min}m</SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                </div>

                {/* Rating */}
                <div>
                  <Label className="text-gray-300 text-xs mb-1 block">Rating Range</Label>
                  <div className="grid grid-cols-2 gap-1">
                    <Input
                      type="number"
                      value={config.minRating}
                      onChange={(e) => setConfig(prev => ({ ...prev, minRating: parseInt(e.target.value) || 800 }))}
                      className="bg-gray-800 border-gray-600 text-white h-8 text-sm"
                      placeholder="Min"
                    />
                    <Input
                      type="number"
                      value={config.maxRating}
                      onChange={(e) => setConfig(prev => ({ ...prev, maxRating: parseInt(e.target.value) || 1500 }))}
                      className="bg-gray-800 border-gray-600 text-white h-8 text-sm"
                      placeholder="Max"
                    />
                  </div>
                </div>

                {/* Difficulty and Challenge Button */}
                <div className="flex items-end gap-2">
                  <div className="flex-1">
                    <Label className="text-gray-300 text-xs mb-1 block">Difficulty</Label>
                    <div className="grid grid-cols-3 gap-1">
                      {difficultyOptions.map((difficulty) => (
                        <Button
                          key={difficulty.id}
                          variant={config.difficulty === difficulty.id ? "default" : "outline"}
                          size="sm"
                          className={`h-8 text-xs ${config.difficulty === difficulty.id
                            ? 'bg-green-600 hover:bg-green-700 border-green-500 text-white'
                            : 'bg-gray-800 hover:bg-gray-700 border-gray-600 text-gray-300'
                            }`}
                          onClick={() => updateDifficulty(difficulty.id)}
                        >
                          <div className={`w-2 h-2 rounded-full ${difficulty.color} mr-1`} />
                          {difficulty.name}
                        </Button>
                      ))}
                    </div>
                  </div>


                </div>
              </div>

              {/* Friends Column */}
              <div className="flex flex-col justify-between h-full">
                <div className="space-y-4">
                  <h3 className="text-white font-medium text-sm mb-2 flex items-center gap-1">
                    <Users className="w-4 h-4 text-purple-400" />
                    Invite Friend
                    <Badge variant="secondary" className="bg-purple-600/20 text-purple-300 text-xs ml-2">
                      {onlineFriends.length} online
                    </Badge>
                  </h3>

                  <Select onValueChange={handleFriendSelect} value={config.invitedFriends[0]?.username || ""}>
                    <SelectTrigger className="bg-gray-800 border-gray-600 text-white h-10">
                      <SelectValue placeholder="Select a friend" />
                    </SelectTrigger>
                    <SelectContent className="bg-gray-800 border-gray-600 max-h-32 overflow-y-auto">
                      {onlineFriends.length === 0 ? (
                        <div className="text-gray-400 text-center py-2 text-sm">No friends online</div>
                      ) : (
                        onlineFriends.map(friend => (
                          <SelectItem key={friend.id} value={friend.username}>
                            <div className="flex items-center gap-2">
                              <Avatar className="w-5 h-5 border border-gray-700">
                                <AvatarImage src={friend.profilePicture} />
                                <AvatarFallback>{friend.username[0]}</AvatarFallback>
                              </Avatar>
                              <div className="text-white text-sm">{friend.username}</div>
                            </div>
                          </SelectItem>
                        ))
                      )}
                    </SelectContent>
                  </Select>

                  {config.invitedFriends[0] && (
                    <div className="bg-gray-800/50 rounded-lg p-2 border border-gray-700 mt-2">
                      <div className="flex items-center gap-2">
                        <Avatar className="w-8 h-8 border border-gray-700">
                          <AvatarImage src={config.invitedFriends[0].profilePicture} />
                          <AvatarFallback>{config.invitedFriends[0].username.charAt(0)}</AvatarFallback>
                        </Avatar>
                        <div>
                          <div className="text-white text-sm font-medium">{config.invitedFriends[0].username}</div>
                          <div className="text-xs text-green-400">Ready to battle</div>
                        </div>
                      </div>
                    </div>
                  )}
                </div>

                {/* Stick Button at Bottom */}
                <Button
                  onClick={handleStartBattle}
                  className="h-8 bg-gradient-to-r from-purple-600 to-purple-600 hover:from-purple-700 hover:to-purple-700 text-white font-semibold"
                >
                  <Gamepad2 className="h-4 w-4 mr-1" />
                  Challenge
                </Button>
              </div>

            </div>

          </CardContent>
        </Card>
      ) : (
        // Confirm popup
        <Card className="relative w-full max-w-sm bg-gray-900 text-white border border-gray-800">
          <Button
            variant="ghost"
            size="icon"
            className="absolute right-2 top-2 h-6 w-6 text-gray-400 hover:bg-gray-800/50"
            onClick={() => setIsConfirming(false)}
            disabled={isLoading}
          >
            <X className="h-3 w-3" />
          </Button>

          <CardHeader className="pb-3">
            <CardTitle className="text-lg font-bold text-gray-100">
              Challenge {config.invitedFriends[0]?.username}
            </CardTitle>
            <p className="text-xs text-gray-400">Send challenge to your friend</p>
          </CardHeader>

          <CardContent className="pt-0">
            {error && (
              <div className="bg-red-900/50 text-red-400 border border-red-800 p-2 rounded-md mb-3 text-sm">
                <p>Error: {error}</p>
              </div>
            )}

            <div className="flex items-center gap-3 p-2 rounded-lg bg-gray-800/30 border border-gray-800 mb-4">
              <Avatar className="border border-gray-700 w-8 h-8">
                <AvatarImage src={config.invitedFriends[0]?.profilePicture} />
                <AvatarFallback>{config.invitedFriends[0]?.username.charAt(0)}</AvatarFallback>
              </Avatar>
              <div>
                <h3 className="font-medium text-white text-sm">{config.invitedFriends[0]?.username}</h3>
                <p className="text-xs text-green-400">Online</p>
              </div>
            </div>

            {/* Right-aligned small buttons */}
            <div className="flex justify-end gap-2">
              <Button
                variant="outline"
                size="sm"
                className="h-8 text-xs border-gray-600 text-white bg-gray-800 hover:bg-gray-700"
                onClick={() => setIsConfirming(false)}
                disabled={isLoading}
              >
                Cancel
              </Button>
              <Button
                size="sm"
                className="h-8 text-xs bg-purple-600 hover:bg-purple-700 text-white px-3"
                onClick={sendChallengeRequest}
                disabled={isLoading}
              >
                {isLoading ? (
                  <span className="flex items-center gap-1">
                    <Loader2 className="h-3 w-3 animate-spin" />
                    Sending...
                  </span>
                ) : (
                  <>
                    <Gamepad2 className="h-4 w-4 mr-1" />
                    Send
                  </>
                )}
              </Button>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
