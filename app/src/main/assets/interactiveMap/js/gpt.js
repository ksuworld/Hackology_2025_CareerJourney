import * as THREE from 'three';
import { OrbitControls } from 'OrbitControls';
import { GLTFLoader } from 'GLTFLoader';

// Scene
const scene = new THREE.Scene();

// Background gradient
const canvas = document.getElementById('journey-canvas');
canvas.width = 16;
canvas.height = 256;
const ctx = canvas.getContext('2d');
const gradient = ctx.createLinearGradient(0, 0, 0, 256);
gradient.addColorStop(0, '#4ea2ff');
gradient.addColorStop(1, '#6ee7b7');
ctx.fillStyle = gradient;
ctx.fillRect(0, 0, 16, 256);
scene.background = new THREE.CanvasTexture(canvas);

// Camera
const camera = new THREE.PerspectiveCamera(
  55,
  window.innerWidth / window.innerHeight,
  0.1,
  1000
);
camera.position.set(0, 12, 35);
camera.lookAt(0, 0, 0);

// Renderer
const renderer = new THREE.WebGLRenderer({ antialias: true });
renderer.setSize(window.innerWidth, window.innerHeight);
document.body.appendChild(renderer.domElement);

// Lighting
scene.add(new THREE.AmbientLight(0xffffff, 0.7));
const dirLight = new THREE.DirectionalLight(0xffffff, 1);
dirLight.position.set(10, 25, 10);
scene.add(dirLight);

// Smooth “hilly” path
const curvePoints = [];
for (let i = 0; i <= 20; i++) {
  const x = (i - 10) * 1.5;
  const z = Math.sin(i * 0.5) * 5;
  const y = Math.sin(i * 0.4) * 2; // creates elevation
  curvePoints.push(new THREE.Vector3(x, y, z));
}
const curve = new THREE.CatmullRomCurve3(curvePoints);

// Flattened road (extruded shape instead of tube)
const roadShape = new THREE.Shape();
roadShape.moveTo(-2, 0);
roadShape.lineTo(2, 0);
roadShape.lineTo(2, 0.1);
roadShape.lineTo(-2, 0.1);
roadShape.lineTo(-2, 0);

const extrudeSettings = {
  steps: 200,
  bevelEnabled: false,
  extrudePath: curve,
};
const roadGeometry = new THREE.ExtrudeGeometry(roadShape, extrudeSettings);
const roadMaterial = new THREE.MeshStandardMaterial({
  color: 0x2b42ff,
  roughness: 0.3,
  metalness: 0.1,
});
const road = new THREE.Mesh(roadGeometry, roadMaterial);
road.position.y = -1.5;
scene.add(road);

// Ground plane
const groundGeo = new THREE.PlaneGeometry(200, 200);
const groundMat = new THREE.MeshStandardMaterial({ color: 0x7fe3a2 });
const ground = new THREE.Mesh(groundGeo, groundMat);
ground.rotation.x = -Math.PI / 2;
ground.position.y = -3;
scene.add(ground);

// Controls
const controls = new OrbitControls(camera, renderer.domElement);
controls.enableDamping = true;
controls.target.set(0, 0, 0);
controls.update();

// Resize
window.addEventListener('resize', () => {
  camera.aspect = window.innerWidth / window.innerHeight;
  camera.updateProjectionMatrix();
  renderer.setSize(window.innerWidth, window.innerHeight);
});

// Animate
function animate() {
  requestAnimationFrame(animate);
  controls.update();
  renderer.render(scene, camera);
}
animate();
