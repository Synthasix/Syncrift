import React, { useEffect, useRef, useState } from "react";
import AceEditor from "react-ace";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { toast } from "sonner";
import { Code2, Play, Target } from "lucide-react";
import { useBattle, useStomp } from "@/utils/StompContext";

// Ace Editor: load mode & theme
import "ace-builds/src-noconflict/mode-html";
import "ace-builds/src-noconflict/theme-tomorrow_night_bright";
import "ace-builds/src-noconflict/ext-language_tools";

function CSSBattle() {
  const { battleData } = useBattle();
  const { send } = useStomp();
  const [url] = useState(battleData.config.imageUrl);
  const [duration] = useState(battleData.config.duration);
  const [targetColors] = useState(battleData.config.colorCode || []);

  const submittedRef = useRef(false);
  const initialCode = `<style>
.box {
  width: 100px;
  height: 100px;
  background: #dd6b4d;
}
</style>

<div class="box"></div>`;

  const [code, setCode] = useState(initialCode);
  const [isLoading, setIsLoading] = useState(false);
  const [hasSubmitted, setHasSubmitted] = useState(false);
  const [timeLeft, setTimeLeft] = useState(duration);

  // ✅ Timer Logic
  useEffect(() => {
    const endTime = Date.now() + duration * 1000;

    const updateTimer = () => {
      const now = Date.now();
      const remaining = Math.max(0, Math.ceil((endTime - now) / 1000));
      setTimeLeft(remaining);

      if (remaining <= 0 && !submittedRef.current) {
        handleAutoSubmit();
      }
    };

    updateTimer();
    const interval = setInterval(updateTimer, 1000);
    document.addEventListener("visibilitychange", () => {
      if (!document.hidden) updateTimer();
    });

    return () => clearInterval(interval);
  }, []);

  // ✅ Instant Live Preview Update
  const updatePreview = (htmlCode) => {
    const iframe = document.getElementById("preview-frame");
    if (!iframe) return;
    const iframeDoc = iframe.contentDocument || iframe.contentWindow.document;
    iframeDoc.open();
    iframeDoc.write(`<!DOCTYPE html><html><head></head><body>${htmlCode}</body></html>`);
    iframeDoc.close();
  };

  useEffect(() => {
    updatePreview(code); // Initial render
  }, []);

  // ✅ Submit Handlers
  const handleSubmit = async () => {
    if (submittedRef.current || hasSubmitted) return;

    submittedRef.current = true;
    setHasSubmitted(true);
    setIsLoading(true);

    try {
      const payload = {
        battleId: battleData.battleId,
        text: code.trim(),
      };
      send("/app/battle/end", payload);
      await new Promise((resolve) => setTimeout(resolve, 1500));

      toast.success("Submission successful!", {
        description: "Your code has been submitted for evaluation.",
        duration: 4000,
      });
    } catch (error) {
      toast.error("Submission failed", {
        description: "Please check your connection and try again.",
        duration: 4000,
      });
      console.error("Error submitting code:", error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleAutoSubmit = () => {
    if (submittedRef.current || hasSubmitted) return;

    submittedRef.current = true;
    setHasSubmitted(true);

    const payload = {
      battleId: battleData.battleId,
      text: code.trim(),
    };
    send("/app/battle/end", payload);

    toast.success("Time's up! Code submitted.");
  };

  return (
    <div className="h-screen w-full bg-black text-white pt-16">
      <div className="h-[calc(100vh-64px)] grid grid-cols-3 gap-0">
        {/* Code Editor */}
        <Card className="rounded-none border-r border-l-0 border-t-0 border-b-0 bg-black">
          <div className="px-3 py-0.5 text-xl font-bold flex items-center gap-1 justify-between">
            <div className="flex items-center gap-1">
              <Code2 className="h-4 w-4" />
              Code Editor
            </div>
            <span className="text-sm font-mono text-muted-foreground">
              Time Left: {Math.floor(timeLeft / 60)}:{(timeLeft % 60).toString().padStart(2, "0")}
            </span>
          </div>
          <CardContent className="p-0 flex flex-col h-[calc(100%-24px)]">
            <AceEditor
              mode="html"
              theme="tomorrow_night_bright"
              name="ace-editor"
              value={code}
              onChange={(val) => {
                setCode(val);
                updatePreview(val);
              }}
              editorProps={{ $blockScrolling: true }}
              fontSize={18}
              width="100%"
              height="100%"
              setOptions={{
                enableBasicAutocompletion: true,
                enableLiveAutocompletion: true,
                showLineNumbers: true,
                tabSize: 2,
              }}
              style={{
                flex: 1,
                fontFamily: "'JetBrains Mono', 'Fira Code', Consolas, monospace",
                borderTop: "1px solid #333",
                borderBottom: "1px solid #333",
              }}
            />
            <div className="p-4 space-y-3">
              <div className="flex items-center justify-center text-sm text-muted-foreground">
                <span>Ready to submit?</span>
              </div>
              <Button
                onClick={handleSubmit}
                disabled={isLoading || hasSubmitted}
                className="w-full gap-2"
                size="lg"
              >
                <Play className="h-4 w-4" />
                {isLoading ? "Submitting..." : hasSubmitted ? "Submitted" : "Submit Solution"}
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* Live Preview */}
        <Card className="rounded-none border-r border-l-0 border-t-0 border-b-0 bg-black">
          <div className="px-3 py-0.5 text-xl font-bold">Live Preview</div>
          <CardContent className="p-0 flex flex-col h-[calc(100%-24px)]">
            <div className="flex-1 bg-white border-y">
              <iframe
                id="preview-frame"
                className="w-full h-full"
                title="Live Preview"
                style={{ backgroundColor: "white" }}
              />
            </div>
          </CardContent>
        </Card>

        {/* Target Image & Colors */}
        <Card className="rounded-none border-r-0 border-l-0 border-t-0 border-b-0 bg-black">
          <div className="px-3 py-0.5 text-xl font-bold flex items-center justify-between">
            <div className="flex items-center gap-1">
              <Target className="h-4 w-4" />
              Target Design
            </div>
            <Badge variant="outline" className="text-[10px] px-1.5 py-0">
              400×300px
            </Badge>
          </div>
          <CardContent className="p-0 flex flex-col h-[calc(100%-24px)]">
            <img
              src={url}
              alt="Target Design"
              onError={(e) => {
                e.currentTarget.src = "";
                e.currentTarget.alt = "Image failed to load";
              }}
              className="object-contain"
            />
            <div className="p-4 space-y-3">
              <div className="text-sm font-medium text-muted-foreground">Color Palette</div>
              <div className="grid grid-cols-3 gap-2">
                {targetColors.map((color, index) => (
                  <button
                    key={index}
                    className="h-10 rounded-md border border-border hover:border-primary transition-colors flex items-center justify-center text-xs font-mono font-medium"
                    style={{
                      backgroundColor: color,
                      color: color === "#FFFFFF" ? "#000" : "#fff",
                    }}
                    onClick={() => {
                      navigator.clipboard.writeText(color);
                      toast.success("Color copied!", {
                        description: `${color} copied to clipboard`,
                        duration: 2000,
                      });
                    }}
                  >
                    {color}
                  </button>
                ))}
              </div>
              <div className="text-xs text-muted-foreground text-center">
                Click colors to copy to clipboard
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

export default CSSBattle;