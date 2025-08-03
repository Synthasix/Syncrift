import React, { useEffect, useRef, useState } from "react";
import * as monaco from "monaco-editor";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { ScrollArea } from "@/components/ui/scroll-area";
import { toast } from "sonner";

function CSSBattle() {
  const editorRef = useRef(null);
  const monacoRef = useRef(null);

  const [htmlCode, setHtmlCode] = useState(`<div class="box"></div>`);
  const [cssCode, setCssCode] = useState(`.box {
  width: 100px;
  height: 100px;
  background: #dd6b4d;
}`);

  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (typeof window === "undefined") return;

    const timeout = setTimeout(() => {
      if (editorRef.current && !monacoRef.current) {
        const combinedCode = `<style>\n${cssCode}\n</style>\n${htmlCode}`;

        monacoRef.current = monaco.editor.create(editorRef.current, {
          value: combinedCode,
          language: "html",
          theme: "vs-dark",
          automaticLayout: true,
          minimap: { enabled: false },
        });

        monacoRef.current.onDidChangeModelContent(() => {
          const updatedValue = monacoRef.current.getValue();
          const [stylePart, ...htmlParts] = updatedValue.split(/<\/style>\s*/);
          const cssContent = stylePart.replace(/<style>/, "").trim();
          const htmlContent = htmlParts.join("<\/style>").trim();
          setCssCode(cssContent);
          setHtmlCode(htmlContent);
        });
      }
    }, 0);

    return () => {
      clearTimeout(timeout);
      monacoRef.current?.dispose();
    };
  }, []);

  useEffect(() => {
    const iframe = document.getElementById("preview-frame");
    if (!iframe) return;
    const iframeDoc = iframe.contentDocument || iframe.contentWindow.document;
    iframeDoc.open();
    iframeDoc.write(`
      <style>${cssCode}</style>
      ${htmlCode}
    `);
    iframeDoc.close();
  }, [htmlCode, cssCode]);

  const handleSubmit = async () => {
    setIsLoading(true);
    try {
      const response = await fetch("https://your-backend-api.com/submit", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ html: htmlCode, css: cssCode }),
      });

      if (!response.ok) throw new Error("Submission failed");
      const result = await response.json();
      toast.success("Submission successful!");
      console.log("Backend response:", result);
    } catch (error) {
      toast.error("Submission failed. Please try again.");
      console.error("Error submitting code:", error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="pt-[64px] p-6 h-[calc(100vh-64px)] w-full overflow-hidden bg-background text-white font-mono">
      <div className="grid grid-cols-3 gap-6 h-full">
        {/* Combined Editor */}
        <Card className="bg-muted flex flex-col shadow-xl">
          <CardContent className="p-4 flex-1 flex flex-col space-y-4">
            <h2 className="text-lg font-semibold text-primary">HTML + CSS Editor</h2>
            <div ref={editorRef} className="border rounded-lg h-[400px]" />
            <Button onClick={handleSubmit} disabled={isLoading}>
              {isLoading ? "Submitting..." : "Submit"}
            </Button>
          </CardContent>
        </Card>

        {/* Live Preview */}
        <Card className="bg-muted shadow-xl">
          <CardContent className="p-4 h-full flex flex-col">
            <h2 className="text-lg font-semibold text-primary mb-2">Live Preview</h2>
            <iframe
              id="preview-frame"
              className="flex-1 rounded-lg border border-white bg-white"
              title="Live Preview"
            />
          </CardContent>
        </Card>

        {/* Target Reference */}
        <Card className="bg-muted shadow-xl">
          <CardContent className="p-4 h-full flex flex-col">
            <h2 className="text-lg font-semibold text-primary mb-2">Target</h2>
            <img
              src="https://via.placeholder.com/150"
              alt="Target"
              className="w-full h-48 object-contain border border-white rounded-md"
            />
            <Separator className="my-4" />
            <div className="flex gap-2 justify-between">
              <div className="flex-1 text-center p-2 rounded-md border border-gray-600 font-bold cursor-pointer hover:bg-accent">
                Color 1
              </div>
              <div className="flex-1 text-center p-2 rounded-md border border-gray-600 font-bold cursor-pointer hover:bg-accent">
                Color 2
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

export default CSSBattle;
