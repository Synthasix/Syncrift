import { useEffect, useMemo } from 'react';
import { extend, useThree, useFrame } from '@react-three/fiber';
import { EffectComposer } from 'three/examples/jsm/postprocessing/EffectComposer';
import { RenderPass } from 'three/examples/jsm/postprocessing/RenderPass';
import { ShaderPass } from 'three/examples/jsm/postprocessing/ShaderPass';
import { UnrealBloomPass } from 'three/examples/jsm/postprocessing/UnrealBloomPass';
import * as THREE from 'three';
import AsciiShader from '../shaders/AsciiShader';

extend({ EffectComposer, RenderPass, ShaderPass, UnrealBloomPass });

const AsciiEffect = () => {
  const { gl, scene, camera, size } = useThree();
  const composer = useMemo(() => {
    if (size.width < 1 || size.height < 1) return null; // Prevent zero-size composer

    const composer = new EffectComposer(gl);
    const renderPass = new RenderPass(scene, camera);
    
    // Add fallback for potential null returns
    if (!renderPass) return null;

    const bloomPass = new UnrealBloomPass(
      new THREE.Vector2(size.width, size.height),
      0.5,
      0.4,
      0.85
    );

    const asciiPass = new ShaderPass(AsciiShader);
    asciiPass.uniforms.resolution.value = [size.width, size.height];
    asciiPass.uniforms.charSize.value = 6;

    composer.addPass(renderPass);
    composer.addPass(bloomPass);
    composer.addPass(asciiPass);

    return composer;
  }, [gl, scene, camera, size]);

  useEffect(() => {
    if (composer && size.width > 0 && size.height > 0) {
      composer.setSize(size.width, size.height);
    }
  }, [composer, size]);

  useFrame(() => {
    if (composer) composer.render();
  }, 1);

  return null;
};

export default AsciiEffect;