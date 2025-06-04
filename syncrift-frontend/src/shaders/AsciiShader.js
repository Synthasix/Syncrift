const AsciiShader = {
    uniforms: {
      tDiffuse: { value: null },
      resolution: { value: null },
      charSize: { value: 8 },
    },
    vertexShader: `
      varying vec2 vUv;
      void main() {
        vUv = uv;
        gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
      }
    `,
    fragmentShader: `
      uniform sampler2D tDiffuse;
      uniform vec2 resolution;
      uniform float charSize;
      varying vec2 vUv;
  
      const float ascii_chars = 20.0;
  
      float character(float n, vec2 p) {
        p = floor(p*vec2(4.0, 4.0) + 2.5);
        if (clamp(p.x, 0.0, 4.0) == p.x && clamp(p.y, 0.0, 4.0) == p.y) {
          if (int(mod(n/exp2(p.y*4.0 + p.x), 2.0)) == 1) return 1.0;
        }
        return 0.0;
      }
  
      float getBrightness(vec3 color) {
        return (color.r * 0.299 + color.g * 0.587 + color.b * 0.114);
      }
  
      void main() {
        vec2 uv = vUv;
        vec2 texSize = resolution.xy / charSize;
        vec2 cell = floor(uv * texSize);
        vec2 cellUv = fract(uv * texSize);
        vec2 cellQuad = cellUv * 2.0 - 1.0;
        
        vec4 texel = texture2D(tDiffuse, cell / texSize);
        float brightness = getBrightness(texel.rgb);
        
        float asciiIndex = floor(brightness * ascii_chars);
        float ascii = 0.0;
        
        if (brightness < 0.2) {
          ascii = character(4194304.0, cellQuad);
        } else {
          ascii = character(16766335.0, cellQuad);
        }
        
        vec3 color = texel.rgb * 0.7 + vec3(ascii) * 0.3;
        gl_FragColor = vec4(color, 1.0);
      }
    `
  };
  
  export default AsciiShader;