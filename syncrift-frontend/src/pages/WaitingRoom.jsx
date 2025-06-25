import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar";
import { useBattle, useStomp } from "@/utils/StompContext";
import { useAuth } from "@/utils/AuthContext";
import { Timer } from "lucide-react";
import { replace, useNavigate } from "react-router-dom";

export default function BattleWaitingPage() {
  const [timeLeft, setTimeLeft] = useState(30);
  const [isReady, setIsReady] = useState(false);
  const { send, subscribeWithCleanup } = useStomp();
  const { token } = useAuth();
  const [expired, setExpired] = useState(false);
  const { battleData, updateBattleData } = useBattle();
  const navigate = useNavigate();
  let id = localStorage.getItem("battleId");

  // check to prevent access if exited from battle or
  // access through url or browser page navigation
  useEffect(()=>{
    if (!battleData?.battleId || battleData.battleId == id) {
      navigate("/", {replace : true});
    } else {
      localStorage.setItem("battleId", battleData.battleId);
    }
  }, [])

  // Timer countdown (frontend-only, no timeout API calls)
  useEffect(() => {
    if (timeLeft > 0) {
      const timer = setTimeout(() => {
        setTimeLeft(timeLeft - 1);
      }, 1000);
      return () => clearTimeout(timer);
    }
  }, [timeLeft]);

  // Subscribe to battle start command
  useEffect(() => {
    if (!token) return;
    localStorage.removeItem("battleId");
    const cleanup = subscribeWithCleanup(
      "/user/topic/battle/start",
      (message) => {
        const data = JSON.parse(message.body);
        console.log("data", data);
        battleData.config = data.config;
        // console.log("battle data: ", battleData);
        updateBattleData(battleData);
        navigate("/battle");
      }
    );

    return cleanup;
  }, [token]);

  const handleReady = () => {
    if (!isReady && battleData?.battleId) {
      // Convert to numeric value first
      const battleId = Number(battleData.battleId);
      send("/app/battle/ready", battleId);
      setIsReady(true);
    }
  };

  // don't render if the battle already happened
  if (!battleData?.battleId || battleData.battleId == id) {
    return null;
  }

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-background p-4">
      <Card className="w-full max-w-3xl">
        <CardHeader className="text-center border-b">
          <CardTitle className="text-2xl font-bold">
            Battle Waiting Room
          </CardTitle>
          <div className="flex items-center justify-center gap-2 text-muted-foreground">
            <Timer size={18} />
            <span>{timeLeft} seconds remaining</span>
          </div>
        </CardHeader>

        <CardContent className="pt-6">
          <div className="flex justify-center items-center gap-16">
            {/* Player */}
            <div className="flex flex-col items-center gap-3">
              <Avatar className="w-32 h-32 border-2 border-primary">
                <AvatarImage src="/api/placeholder/400/400" alt="Your avatar" />
                <AvatarFallback className="text-xl">YOU</AvatarFallback>
              </Avatar>
              <div className="text-center">
                <h3 className="font-bold text-xl">You</h3>
                {isReady && <span className="text-green-500">✔ Ready</span>}
              </div>
            </div>

            <div className="text-4xl font-bold text-primary">VS</div>

            {/* Opponent - No status tracking */}
            <div className="flex flex-col items-center gap-3">
              <Avatar className="w-32 h-32 border-2 border-destructive">
                <AvatarImage
                  src="/api/placeholder/400/400"
                  alt="Opponent avatar"
                />
                <AvatarFallback className="text-xl">OPP</AvatarFallback>
              </Avatar>
              <div className="text-center">
                <h3 className="font-bold text-xl">Opponent</h3>
              </div>
            </div>
          </div>

          {timeLeft <= 0 && (
            <div className="mt-10 p-4 bg-destructive/10 rounded-md text-center">
              <p className="text-destructive font-medium">Time expired!</p>
            </div>
          )}
        </CardContent>

        <CardFooter className="flex justify-center pt-4 pb-6">
          <div className="relative w-2/3">
            {!isReady && timeLeft > 0 && (
              <div className="absolute inset-0 rounded-md overflow-hidden">
                <div
                  className="h-full bg-primary/20"
                  style={{
                    width: `${(timeLeft / 30) * 100}%`,
                    transition: "width 1s linear",
                  }}
                />
              </div>
            )}

            <Button
              className={`w-full h-12 text-lg font-medium relative z-10 ${
                isReady
                  ? "bg-green-400 hover:bg-green-700"
                  : "bg-muted/80 hover:bg-muted"
              }`}
              variant={isReady ? "default" : "secondary"}
              onClick={handleReady}
              disabled={isReady || expired}
            >
              {isReady ? "READY!" : "I'M READY"}
            </Button>
          </div>
        </CardFooter>
      </Card>
    </div>
  );
}
