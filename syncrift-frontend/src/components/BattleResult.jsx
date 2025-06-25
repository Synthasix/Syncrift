import React, { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Trophy, Star, Home } from 'lucide-react';
import confetti from 'canvas-confetti';
import { motion } from 'framer-motion';
import { useBattle } from '@/utils/StompContext';
import { useAuth } from '@/utils/AuthContext';

const BattleResults = () => {
  const location = useLocation();
  const navigate = useNavigate();

  const { battleData } = useBattle()
  const { user } = useAuth();


  // const opponent = location.state?.opponent || { username: 'Unknown Opponent' };
  // const user = location.state?.user || { username: 'You' };
  // const userWon = location.state?.userWon || true;
  // const userScore = location.state?.userScore || 0;
  // const opponentScore = location.state?.opponentScore || 0;
  // const timeSpent = location.state?.timeSpent || 0;

  const opponent = battleData.challenger.username === user
    ? battleData.opponent.username
    : battleData.challenger.username;

  const userWon = battleData.result.winnerUsername === user;

  const userScore = userWon
    ? battleData.result.winnerScore
    : battleData.result.loserScore;

  const opponentScore = userWon
    ? battleData.result.loserScore
    : battleData.result.winnerScore;

  const timeSpent = battleData.config.duration;
  // const opponent = battleData

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  const getExpGain = () =>
    userWon ? Math.floor(Math.random() * 15) + 15 : Math.floor(Math.random() * 5) + 5;

  const getRatingChange = () =>
    userWon ? Math.floor(Math.random() * 15) + 10 : -(Math.floor(Math.random() * 10) + 5);

  const handleGoHome = () => {
    navigate('/');
  };

  useEffect(() => {
    if (userWon && typeof window !== 'undefined') {
      confetti({
        particleCount: 100,
        spread: 70,
        origin: { y: 0.6 },
      });
    }
  }, [userWon]);

  return (
    <div className="max-w-xl mx-auto mt-20 p-6 space-y-8">
      <motion.div
        className="text-center"
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
      >
        <div className="inline-flex items-center justify-center p-4 rounded-full bg-muted/20 mb-6">
          {userWon ? (
            <Trophy className="h-12 w-12 text-yellow-500" />
          ) : (
            <Star className="h-12 w-12 text-blue-500" />
          )}
        </div>
        <h2 className="text-3xl font-bold mb-2">
          {userWon ? 'Victory!' : 'Good effort!'}
        </h2>
        <p className="text-muted-foreground">
          {userWon
            ? 'Congratulations, you won the battle!'
            : "Keep practicing, you'll win next time!"}
        </p>
      </motion.div>

      <motion.div
        className="bg-muted/20 rounded-lg p-6 space-y-4"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.3, duration: 0.5 }}
      >
        <div className="flex justify-between items-center">
          <div className="text-center flex-1">
            <p className="text-sm text-muted-foreground mb-1">{user || 'You'}</p>
            <p className="text-3xl font-bold">{userScore}</p>
          </div>
          <div className="text-center font-bold text-xl">vs</div>
          <div className="text-center flex-1">
            <p className="text-sm text-muted-foreground mb-1">{opponent}</p>
            <p className="text-3xl font-bold">{opponentScore}</p>
          </div>
        </div>

        <div className="pt-4 border-t border-muted space-y-2">
          <div className="flex justify-between text-sm">
            <span className="text-muted-foreground">Battle Time:</span>
            <span>{formatTime(timeSpent)}</span>
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-muted-foreground">Experience Gained:</span>
            <span className="text-green-500">+{getExpGain()} XP</span>
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-muted-foreground">Rating Change:</span>
            <span className={getRatingChange() >= 0 ? 'text-green-500' : 'text-red-500'}>
              {getRatingChange() >= 0 ? '+' : ''}
              {getRatingChange()}
            </span>
          </div>
        </div>
      </motion.div>

      <motion.div
        className="pt-4"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.6, duration: 0.5 }}
      >
        <Button variant="ghost" onClick={handleGoHome} className="w-full">
          <Home className="mr-2 h-4 w-4" />
          Return Home
        </Button>
      </motion.div>
    </div>
  );
};

export default BattleResults;
