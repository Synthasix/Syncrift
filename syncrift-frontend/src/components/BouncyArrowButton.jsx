import { Button } from "@/components/ui/button";
import { ChevronDown } from "lucide-react";
import { cn } from "@/lib/utils";
import React from "react";

export default function BouncyArrowButton({ onClick, className }) {
  return (
    <div className="flex items-center justify-center w-full h-full">
      <Button
        variant="ghost"
        onClick={onClick}
        className={cn(
          "w-44 h-11 rounded-full bg-white/10 text-white hover:bg-white/20 animate-bounce-slow text-xl font-semibold gap-3 flex items-center justify-center",
          className
        )}
      >
        <span>Get Started</span>
        <ChevronDown className="h-8 w-8" />
      </Button>
    </div>
  );
}