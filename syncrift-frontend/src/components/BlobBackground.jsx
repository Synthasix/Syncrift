import React from "react";
import { Canvas } from "@react-three/fiber";
import { OrbitControls } from "@react-three/drei";
import AsciiBlob from "./AsciiBlob";
import AsciiEffect from "./AsciiEffect";

function BlobBackground() {
  return (
    <div className="fixed inset-0 w-full h-full">
      <Canvas camera={{ position: [0, 0, 8] }}>
        <ambientLight intensity={0.1} />
        <pointLight position={[10, 10, 10]} />

        {/* Top blobs */}
        <AsciiBlob
          position={[-9, 4, 0]}
          color="#ff4477"
          emissiveColor="#440022"
        />
        <AsciiBlob
          position={[9, 4, 0]}
          color="#44ddff"
          emissiveColor="#002244"
        />

        {/* Bottom blob - positioned to be visible in battle page */}
        <AsciiBlob
          position={[0, -6.5, 0]}
          color="#44ddff"
          emissiveColor="#002244"
        />

        <AsciiEffect />
        <OrbitControls
          enableZoom={false}
          enablePan={false}
          enableRotate={false}
        />
      </Canvas>
    </div>
  );
}

export default BlobBackground;