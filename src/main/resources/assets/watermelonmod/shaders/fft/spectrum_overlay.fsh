#version 330

uniform sampler2D InSampler;
in vec2 texCoord;
layout(std140) uniform SamplerInfo { vec2 OutSize; vec2 InSize; };
layout(std140) uniform FftConfig { vec4 Params; }; // x = overlay opacity
out vec4 fragColor;

void main() {
    // A square panel keeps both frequency axes (-0.5..0.5 cycles/pixel) at the
    // same scale, so stripe angles and spacings read true. screenquad UV has
    // y=0 at the bottom, hence this is top-right.
    const float panelHeight = 0.30;
    float panelWidth = panelHeight * OutSize.y / OutSize.x;
    vec2 panelSize = vec2(panelWidth, panelHeight);
    vec2 panelOrigin = vec2(0.97 - panelWidth, 0.74);
    vec2 panelUv = (texCoord - panelOrigin) / panelSize;
    if (panelUv.x < 0.0 || panelUv.x > 1.0 || panelUv.y < 0.0 || panelUv.y > 1.0) discard;
    vec3 spectrum = texture(InSampler, panelUv).rgb;
    float edgeDistance = min(min(panelUv.x, 1.0 - panelUv.x), min(panelUv.y, 1.0 - panelUv.y));
    float border = edgeDistance < 0.015 ? 1.0 : 0.0;
    vec3 panelColor = mix(spectrum, vec3(0.25, 0.70, 1.0), border);
    fragColor = vec4(panelColor, clamp(Params.x, 0.0, 1.0));
}
