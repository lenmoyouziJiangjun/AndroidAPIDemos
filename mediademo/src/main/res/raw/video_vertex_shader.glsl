uniform mat4 uMVPMatrix;
uniform mat4 uTextureMatrix;
attribute vec4 aPosition;
attribute vec4 aTextureCoord;
varying vec2 vTextureCoord;

void main() {
    gl_Position = uMVPMatrix * aPosition * vec4(1, -1, 1, 1);
    vTextureCoord = (uTextureMatrix * aTextureCoord).xy;
}
