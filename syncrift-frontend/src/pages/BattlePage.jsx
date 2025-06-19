import TypingBattle from '@/components/TypingBattle';
import { useAuth } from '@/utils/AuthContext';
import { useBattle, useStomp } from '@/utils/StompContext';
import { useEffect ,  } from 'react';
import { useNavigate } from 'react-router-dom';


const BattlePage = () => {
  const { battleData, updateBattleData } = useBattle();
  const { token } = useAuth();
  const { subscribeWithCleanup } = useStomp()
  const navigate = useNavigate()
  let id = localStorage.getItem("battleId");
  
  useEffect(() => {
    if (!token) return;

    const cleanup = subscribeWithCleanup(
      "/user/topic/battle/end",
      (message) => {
        const data = JSON.parse(message.body);
        battleData.result = data.result;
        console.log("battle data: ", battleData);

        updateBattleData(battleData);
        navigate("/battleresult");
      }
    );

    return cleanup;
  }, [token]);
 
  // access through url or browser page navigation
  useEffect(()=>{
    if (!battleData?.battleId || battleData.battleId == id) {
      navigate("/", {replace : true});
    } else {
      localStorage.setItem("battleId", battleData.battleId);
    }
  }, [])

  // don't render if the battle already happened
  if (!battleData?.battleId || battleData.battleId == id) {
    return null;
  }

  switch (battleData.category) {
    case 'TB':
      return <TypingBattle />;
    case 'CSS':
      return <CssBattle data={battleData} />;
    case 'CF':
      return <CodeforcesBattle data={battleData} />;
    default:
      return <div>Unknown battle type</div>;
  }
};

export default BattlePage;
