// BattleCards.jsx
import { useState, useEffect } from 'react';
import { Card } from "@/components/ui/card";
import { Keyboard, Palette, Code } from "lucide-react";
import { useAuth } from '@/utils/AuthContext';
import FriendChallenge from './FriendChallenge';
import { useLogin } from '@/utils/LoginContext';

export default function BattleCards() {
  const [selectedBattle, setSelectedBattle] = useState(null);
  const { user, friends } = useAuth();
  const {setShowLogin} = useLogin();


  if (user) {
    console.log(friends)
  }

  return (
    <div className="min-h-screen pt-40 pb-32 relative">
      <div className="grid grid-cols-1 md:grid-cols-3 gap-10 max-w-7xl mx-auto">
        {/* Typing Battle Card */}
        <Card className="group bg-gray-900 p-8 rounded-xl transition-all duration-500 hover:duration-300 min-h-[400px] flex flex-col 
                        border border-gray-800 hover:border-purple-500/30
                        shadow-lg hover:shadow-purple-500/30
                        transform hover:-translate-y-2 hover:rotate-[1deg]">
          <div className="flex flex-col items-center space-y-6 flex-grow">
            <Keyboard className="w-16 h-16 text-purple-400 group-hover:text-purple-300 transition-colors" />
            <h2 className="text-3xl font-bold text-white group-hover:text-purple-200 transition-colors">Typing Battle</h2>
            <p className="text-gray-300 group-hover:text-gray-100 text-center text-lg transition-colors">
              Test your typing speed against other developers. Race against time
              and compete for the top spot in the leaderboard!
            </p>
            <button
              onClick={() => {
                if (!user) {
                  setShowLogin(true);
                } else {
                  setSelectedBattle("TB");
                }
              }}
              className="mt-6 px-8 py-3 bg-purple-600 hover:bg-purple-500 rounded-lg font-medium text-lg transition-all
                         group-hover:scale-105 group-hover:shadow-md group-hover:shadow-purple-500/50"
            >
              Start Typing
            </button>
          </div>
        </Card>

        {/* CSS Battle Card */}
        <Card className="group bg-gray-900 p-8 rounded-xl transition-all duration-500 hover:duration-300 min-h-[400px] flex flex-col 
                        border border-gray-800 hover:border-blue-500/30
                        shadow-lg hover:shadow-blue-500/30
                        transform hover:-translate-y-2 hover:rotate-[1deg]">
          <div className="flex flex-col items-center space-y-6 flex-grow">
            <Palette className="w-16 h-16 text-blue-400 group-hover:text-blue-300 transition-colors" />
            <h2 className="text-3xl font-bold text-white group-hover:text-blue-200 transition-colors">CSS Battle</h2>
            <p className="text-gray-300 group-hover:text-gray-100 text-center text-lg transition-colors">
              Show off your CSS skills! Replicate complex designs with minimal
              code and compete for the most efficient solutions.
            </p>
            <button
              onClick={() =>{
                if (!user) {
                  setShowLogin(true);
                } else {
                  setSelectedBattle("CSS");
                }
              }}
              className="mt-6 px-8 py-3 bg-blue-600 hover:bg-blue-500 rounded-lg font-medium text-lg transition-all
                         group-hover:scale-105 group-hover:shadow-md group-hover:shadow-blue-500/50"
            >
              Start Styling
            </button>
          </div>
        </Card>

        {/* Coding Battle Card */}
        <Card className="group bg-gray-900 p-8 rounded-xl transition-all duration-500 hover:duration-300 min-h-[400px] flex flex-col 
                        border border-gray-800 hover:border-green-500/30
                        shadow-lg hover:shadow-green-500/30
                        transform hover:-translate-y-2 hover:rotate-[1deg]">
          <div className="flex flex-col items-center space-y-6 flex-grow">
            <Code className="w-16 h-16 text-green-400 group-hover:text-green-300 transition-colors" />
            <h2 className="text-3xl font-bold text-white group-hover:text-green-200 transition-colors">Coding Battle</h2>
            <p className="text-gray-300 group-hover:text-gray-100 text-center text-lg transition-colors">
              Solve algorithmic challenges head-to-head against other
              programmers. Speed and efficiency will decide the winner!
            </p>
            <button
              onClick={() => {
                if (!user) {
                  setShowLogin(true);
                } else {
                  setSelectedBattle("CF");
                }
              }}
              className="mt-6 px-8 py-3 bg-green-600 hover:bg-green-500 rounded-lg font-medium text-lg transition-all
                         group-hover:scale-105 group-hover:shadow-md group-hover:shadow-green-500/50"
            >
              Start Coding
            </button>
          </div>
        </Card>
      </div>

      {selectedBattle && (
        <div className="fixed inset-0 z-[999]">
          {/* Dimmed background with blur - clicking outside closes popup */}
          <div
            className="absolute inset-0 bg-black/30 backdrop-blur-lg"
            onClick={() => setSelectedBattle(null)}
          />

          {/* Centered modal */}
          <div className="fixed inset-0 flex items-center justify-center p-4">
              <FriendChallenge
                battleType={selectedBattle}
                onClose={() => setSelectedBattle(null)}
              />
          </div>
        </div>
      )}
    </div>
  );
}