import React, { useEffect, useRef, useState } from "react";
import * as monaco from "monaco-editor";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { toast } from "sonner";
import { Code2, Play, Target } from "lucide-react";
import kr from "monaco-themes/themes/krTheme.json";
import { useBattle, useStomp } from "@/utils/StompContext";

function CSSBattle() {
  const { battleData } = useBattle();
  const { send } = useStomp();
  const [url] = useState(battleData.config.imageUrl);
  const [duration] = useState(battleData.config.duration);
  const [targetColors] = useState(battleData.config.colorCode || []);
  const editorRef = useRef(null);
  const monacoRef = useRef(null);
  const submittedRef = useRef(false); // ✅ Prevent double submission

  const initialCode = `<style>
.box {
  width: 100px;
  height: 100px;
  background: #dd6b4d;
}
</style>

<div class="box"></div>`;

  const [isLoading, setIsLoading] = useState(false);
  const [hasSubmitted, setHasSubmitted] = useState(false);
  const [timeLeft, setTimeLeft] = useState(duration);

  // ✅ Fixed Timer - Calculate from end time instead of countdown
  useEffect(() => {
    const endTime = Date.now() + (duration * 1000);
    
    const updateTimer = () => {
      const now = Date.now();
      const remaining = Math.max(0, Math.ceil((endTime - now) / 1000));
      
      setTimeLeft(remaining);
      
      if (remaining <= 0 && !submittedRef.current) {
        handleAutoSubmit();
      }
    };

    // Update immediately
    updateTimer();
    
    const interval = setInterval(updateTimer, 1000);
    
    // Handle visibility change to update timer when tab becomes active
    const handleVisibilityChange = () => {
      if (!document.hidden) {
        updateTimer();
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);
    
    return () => {
      clearInterval(interval);
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, []); // ✅ Run once on mount only

  // Monaco Editor setup
  useEffect(() => {
    if (typeof window === "undefined") return;

    const timeout = setTimeout(() => {
      if (editorRef.current && !monacoRef.current) {
        monaco.editor.defineTheme("kr", kr);
        monacoRef.current = monaco.editor.create(editorRef.current, {
          value: initialCode,
          language: "html",
          theme: "kr",
          automaticLayout: true,
          minimap: { enabled: false },
          fontSize: 18,
          lineHeight: 24,
          fontFamily: "'JetBrains Mono', 'Fira Code', Consolas, monospace",
          scrollBeyondLastLine: false,
          wordWrap: "on",
          bracketPairColorization: { enabled: true },
          lineNumbers: "off",
          padding: { top: 16, bottom: 8 },
        });
      }
    }, 0);

    return () => {
      clearTimeout(timeout);
      monacoRef.current?.dispose();
    };
  }, []);

  // Live preview update
  useEffect(() => {
    const interval = setInterval(() => {
      const iframe = document.getElementById("preview-frame");
      if (!iframe || !monacoRef.current) return;
      const iframeDoc = iframe.contentDocument || iframe.contentWindow.document;
      iframeDoc.open();
      iframeDoc.write(`
        <!DOCTYPE html>
        <html><head></head><body>
        ${monacoRef.current.getValue()}
        </body></html>`);
      iframeDoc.close();
    }, 1000);

    return () => clearInterval(interval);
  }, []);

  // ✅ Manual submit
  const handleSubmit = async () => {
    if (submittedRef.current || hasSubmitted) return;

    submittedRef.current = true;
    setHasSubmitted(true);
    setIsLoading(true);

    try {
      const latestCode = monacoRef.current?.getValue() ?? "";
      const payload = {
        battleId: battleData.battleId,
        text: latestCode.trim(),
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

  // ✅ Auto submit - guarded
  const handleAutoSubmit = () => {
    console.log("AUTO SUBMIT CALLED"); // For debugging

    if (submittedRef.current || hasSubmitted) return;

    submittedRef.current = true;
    setHasSubmitted(true);

    const latestCode = monacoRef.current?.getValue() ?? "";
    const payload = {
      battleId: battleData.battleId,
      text: latestCode.trim(),
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
            <div ref={editorRef} className="flex-1 border-y" />
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
              <div className="text-sm font-medium text-muted-foreground">
                Color Palette
              </div>
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