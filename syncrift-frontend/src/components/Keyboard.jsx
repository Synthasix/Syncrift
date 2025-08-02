import React from 'react';
import { Button } from '@/components/ui/button';

const keys = [
  ['Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P'],
  ['A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L'],
  ['Z', 'X', 'C', 'V', 'B', 'N', 'M'],
  ['SPACE']
];

const Keyboard = ({ pressedKey }) => {
  const getKeyVariant = (key) => {
    if (key === 'SPACE' && (pressedKey === ' ' || pressedKey === 'Space')) return 'default';
    return pressedKey && pressedKey.toUpperCase() === key ? 'default' : 'secondary';
  };

  return (
    <div className="mt-6 flex flex-col items-center space-y-2">
      {keys.map((row, rowIndex) => (
        <div key={rowIndex} className="flex space-x-2">
          {row.map((key) => (
            <Button
              key={key}
              variant={getKeyVariant(key)}
              size={key === 'SPACE' ? 'lg' : 'default'}
              className={`${
                key === 'SPACE' 
                  ? 'px-8 text-xs' 
                  : 'w-10 h-10 md:w-12 md:h-12'
              } transition-colors duration-100`}
            >
              {key === 'SPACE' ? 'SPACE' : key}
            </Button>
          ))}
        </div>
      ))}
    </div>
  );
};

export default Keyboard;