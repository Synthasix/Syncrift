import React, { useState, useEffect, useRef } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import Keyboard from "./Keyboard";
import { useBattle, useStomp } from "@/utils/StompContext";

const TypingBattle = () => {
  const { battleData } = useBattle();
  const { send } = useStomp();
  const [text] = useState(battleData.config.text);
  const [duration] = useState(battleData.config.duration);
  const [input, setInput] = useState("");
  const [timeLeft, setTimeLeft] = useState(duration);
  const [pressedKey, setPressedKey] = useState("");
  const [errors, setErrors] = useState([]);
  const inputRef = useRef(null);
  const textContainerRef = useRef(null);
  const currentCharRef = useRef(null);

  // Start timer immediately on mount
  useEffect(() => {
    if (timeLeft > 0) {
      const timer = setInterval(() => {
        setTimeLeft((prev) => prev - 1);
      }, 1000);
      return () => clearInterval(timer);
    }
  }, [timeLeft]);

  useEffect(() => {
    console.log(input);
    if (timeLeft === 0) {
      const payload = {
        battleId: battleData.battleId,
        text: input,
      };
      send("/app/battle/end", payload);
    }
  }, [timeLeft, input, battleData.battleId]);

  // Focus input on mount
  useEffect(() => {
    if (inputRef.current) {
      inputRef.current.focus();
    }
  }, []);

  // Scroll to current character when typing
  useEffect(() => {
    if (currentCharRef.current && textContainerRef.current) {
      const container = textContainerRef.current;
      const currentChar = currentCharRef.current;

      // Get container and element positions
      const containerRect = container.getBoundingClientRect();
      const charRect = currentChar.getBoundingClientRect();

      // Check if character is below the visible area
      if (charRect.top > containerRect.bottom - 50) {
        container.scrollTop += charRect.height * 2; // Scroll down (line height)
      }

      // Check if character is too far right
      if (charRect.right > containerRect.right - 50) {
        // If at line end, scrolling will happen when wrapping to next line
        // The vertical scroll above will handle this
      }
    }
  }, [input]);

  const handleChange = (e) => {
    const value = e.target.value;
    setInput(value);
    setPressedKey(value.slice(-1));

    if (onStart && value.length === 1) {
      onStart(); // Optional callback if needed on actual typing start
    }

    const newErrors = [];
    for (let i = 0; i < value.length; i++) {
      if (value[i] !== text[i]) {
        newErrors.push(i);
      }
    }
    setErrors(newErrors);
  };

  const renderText = () => {
    return text.split("").map((char, i) => {
      const typedChar = input[i];
      const isCorrect = typedChar === char;
      const isTyped = typedChar !== undefined;
      const isCurrent = input.length === i;

      let className = "text-muted-foreground";
      if (isTyped) {
        className = isCorrect ? "text-green-500" : "text-red-500";
      }
      if (isCurrent) {
        className += " border-l-2 border-blue-500 animate-pulse";
      }

      return (
        <span
          key={i}
          className={className}
          ref={isCurrent ? currentCharRef : null}
        >
          {char}
        </span>
      );
    });
  };

  return (
    <div className="w-full max-w-6xl mt-14 mx-auto p-6 space-y-4">
      <Card className="p-6 shadow-lg w-full">
        <CardContent
          ref={textContainerRef}
          className="font-mono text-xl md:text-2xl leading-relaxed whitespace-pre-wrap px-8 py-6 max-h-60 overflow-y-auto scrollbar-hide"
          onClick={() => inputRef.current?.focus()}
        >
          {renderText()}
        </CardContent>
      </Card>

      <div className="flex justify-end items-center">
        <div className="text-lg font-semibold text-white">
          Time left: {timeLeft}s
        </div>
      </div>

      <Progress value={(100 * timeLeft) / duration} className="h-2 mb-10" />

      <input
        ref={inputRef}
        className="absolute opacity-0 w-0 h-0"
        value={input}
        onChange={handleChange}
        disabled={timeLeft === 0}
        autoFocus
      />

      <Keyboard pressedKey={pressedKey} />
    </div>
  );
};

// Add CSS to hide scrollbars while keeping scroll functionality
const style = document.createElement("style");
style.textContent = `
  .scrollbar-hide {
    -ms-overflow-style: none;  /* IE and Edge */
    scrollbar-width: none;     /* Firefox */
  }
  .scrollbar-hide::-webkit-scrollbar {
    display: none;             /* Chrome, Safari and Opera */
  }
`;
document.head.appendChild(style);

export default TypingBattle;
