import * as THREE from 'three';
import { OrbitControls } from 'OrbitControls';
import { GLTFLoader } from 'GLTFLoader';

// --- Global Variables ---
let scene, camera, renderer, controls;
const nodeObjects = []; // Array to store clickable nodes
let raycaster, mouse;
let pathCurve; // Global reference to the road path

// --- Animation Variables ---
let isAnimating = false;
let animationStartTime;
const animationDuration = 1500;
let startCameraPos = new THREE.Vector3();
let endCameraPos = new THREE.Vector3();
let startControlsTarget = new THREE.Vector3();
let endControlsTarget = new THREE.Vector3();

// --- DOM Elements ---
const canvas = document.getElementById('journey-canvas');
const infoPanel = document.getElementById('info-panel');
const nodeTitle = document.getElementById('node-title');
const nodeDescription = document.getElementById('node-description');
const closePanelButton = document.getElementById('close-panel');

// --- Student Journey Data ---
const journeyNodes = [
    { name: "Orientation", type: "start", color: 0xFF6347, description: "Welcome! Your first step on this amazing journey." }, // Tomato Red
    { name: "First Day of Class", type: "checkpoint", color: 0xF6E05E, description: "Classes begin. Find your rooms and meet your professors." }, // Yellow
    { name: "Join a Club", type: "checkpoint", color: 0xED8936, description: "Get involved! Find a club that matches your interests." }, // Orange
    { name: "First Midterm", type: "milestone", color: 0x4299E1, description: "Your first big test. You've got this!" }, // Blue
    { name: "Fall Break", type: "checkpoint", color: 0x68D391, description: "A well-deserved rest. Recharge for the rest of the semester." }, // Green
    { name: "Final Exams (Sem 1)", type: "milestone", color: 0x9F7AEA, description: "Finish the semester strong!" }, // Purple
    { name: "Winter Break", type: "checkpoint", color: 0x4FD1C5, description: "Enjoy the holidays and relax." }, // Teal
    { name: "Start Semester 2", type: "checkpoint", color: 0xF6E05E, description: "A fresh start for a new semester." }, // Yellow
    { name: "Declare Major", type: "milestone", color: 0xF56565, description: "A big decision! Choose your path of study." }, // Red
    { name: "Spring Midterms", type: "milestone", color: 0xED8936, description: "Halfway through the second semester. Keep it up!" }, // Orange
    { name: "Apply for Internship", type: "checkpoint", color: 0x4299E1, description: "Start looking for real-world experience." }, // Blue
    { name: "Spring Break", type: "checkpoint", color: 0x68D391, description: "One last break before the final push." }, // Green
    { name: "Final Exams (Sem 2)", type: "milestone", color: 0x9F7AEA, description: "The end of your first year is in sight!" }, // Purple
    { name: "Summer Internship", type: "milestone", color: 0x4FD1C5, description: "Apply your knowledge in the field." }, // Teal
    { name: "Start Sophomore Year", type: "checkpoint", color: 0xF6E05E, description: "Welcome back! You're not a first-year anymore." }, // Yellow
    { name: "Research Project", type: "milestone", color: 0xF56565, description: "Dive deep into a topic that interests you." }, // Red
    { name: "Junior Year", type: "checkpoint", color: 0xED8936, description: "The upper-level classes begin." }, // Orange
    { name: "Senior Thesis", type: "milestone", color: 0x4299E1, description: "Your capstone project. Make it count!" }, // Blue
    { name: "Job Applications", type: "checkpoint", color: 0x68D391, description: "Prepare your resume and start applying." }, // Green
    { name: "Graduation Day", type: "milestone", color: 0x3182CE, description: "You did it! Congratulations, graduate!" }  // Darker Blue
];

// --- Easing Function ---
function easeInOutCubic(t) {
    return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
}

function init() {
    // 1. Scene
    scene = new THREE.Scene();

    scene.background = new THREE.Color(0x87CEEB);

    // 2. Camera
    camera = new THREE.PerspectiveCamera(45, window.innerWidth / window.innerHeight, 0.1, 1000);
    //camera.position.set(0, 90, 40);
    camera.position.set(10, 100, 80);
    camera.lookAt(0, 0, 0);

    // 3. Renderer
    renderer = new THREE.WebGLRenderer({ canvas: canvas, antialias: true, alpha: true }); // Alpha true for CSS background
    renderer.setSize(window.innerWidth, window.innerHeight);
    renderer.setPixelRatio(window.devicePixelRatio);
    renderer.shadowMap.enabled = true;
    renderer.shadowMap.type = THREE.PCFSoftShadowMap;

    // 4. Lights
    const ambientLight = new THREE.AmbientLight(0xffffff, 0.5);
    scene.add(ambientLight);

    const hemisphereLight = new THREE.HemisphereLight(0xeeeeff, 0x999966, 0.5);
    scene.add(hemisphereLight);

    const directionalLight = new THREE.DirectionalLight(0xffffff, 0.6);
    directionalLight.position.set(20, 50, 30);
    directionalLight.castShadow = true;
    scene.add(directionalLight);

    directionalLight.shadow.mapSize.width = 2048;
    directionalLight.shadow.mapSize.height = 2048;
    directionalLight.shadow.camera.near = 0.5;
    directionalLight.shadow.camera.far = 150;
    directionalLight.shadow.camera.left = -60;
    directionalLight.shadow.camera.right = 60;
    directionalLight.shadow.camera.top = 60;
    directionalLight.shadow.camera.bottom = -60;

    // 5. Controls
    controls = new OrbitControls(camera, renderer.domElement);
    controls.enableDamping = true;
    controls.dampingFactor = 0.05;
    controls.target.set(0, 0, 0);
//    controls.minPolarAngle = Math.PI / 4;
//    controls.maxPolarAngle = Math.PI / 2;
    controls.minDistance = 30;
//    controls.maxDistance = 100;

    controls.mouseButtons = {
        LEFT: THREE.MOUSE.PAN,
        MIDDLE: THREE.MOUSE.DOLLY,
        RIGHT: THREE.MOUSE.ROTATE
    };

    controls.touches = {
        ONE: THREE.TOUCH.PAN,
        TWO: THREE.TOUCH.DOLLY_ROTATE
    };

    // 6. Raycaster for clicking
    raycaster = new THREE.Raycaster();
    mouse = new THREE.Vector2();

    // 7. Create the Environment (Ground, Road, Trees, Static Buildings)
    //createEnvironment();
    createRoadPath();

    // 8. Create the Journey (Nodes)
    //createJourneyGraph();

    // 9. Event Listeners
    window.addEventListener('resize', onWindowResize, false);
    window.addEventListener('click', onClick, false);
    window.addEventListener('touchstart', (event) => {
        // Handle touch as click
        if (event.touches.length === 1) {
            // Check if touch is on the panel itself
            if (infoPanel.contains(event.target)) {
                return;
            }
            onClick(event.touches[0]);
        }
    }, false);

    // NEW: Close panel listener
    closePanelButton.addEventListener('click', () => {
        infoPanel.classList.remove('info-panel-open');
    });

    // 10. Start Animation
    animate();
}

function createRoadPath() {

// Road Track - using a curve for a winding path
const pathPoints = [
    new THREE.Vector3(0, 0, 35),    // Start - Bottom center
    new THREE.Vector3(5, 0, 25),    // Right
    new THREE.Vector3(-5, 0, 15),   // Left
    new THREE.Vector3(10, 0, 0),    // Right
    new THREE.Vector3(0, 0, -10),   // Center
    new THREE.Vector3(-15, 0, -20), // Far Left
    new THREE.Vector3(-5, 0, -30),  // Right
    new THREE.Vector3(10, 0, -40),  // Far Right
    new THREE.Vector3(0, 0, -50),   // End - Top Center
];

// Assign to global variable
pathCurve = new THREE.CatmullRomCurve3(pathPoints, false, 'centripetal', 0.5);

// Road Geometry
const roadWidth = 7;
const roadSegments = 48;

const roadGeometry = new THREE.TubeGeometry(pathCurve, roadSegments, 1, 3, false);
const roadMaterial = new THREE.MeshStandardMaterial({ color: 0xF0F0F0, roughness: 0.7, metalness: 0.1 });
const road = new THREE.Mesh(roadGeometry, roadMaterial);
road.receiveShadow = true;
road.castShadow = true;
scene.add(road);

}

// --- Event Handlers ---
function onWindowResize() {
    camera.aspect = window.innerWidth / window.innerHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(window.innerWidth, window.innerHeight);
}

function onClick(event) {
    // Check if we are already animating, if so, ignore click
    if (isAnimating) return;

    // NEW: Check if click is on the info panel itself
    if (infoPanel.contains(event.target)) {
        return;
    }

    mouse.x = (event.clientX / window.innerWidth) * 2 - 1;
    mouse.y = -(event.clientY / window.innerHeight) * 2 + 1;
    raycaster.setFromCamera(mouse, camera);
    const intersects = raycaster.intersectObjects(nodeObjects, true); // Set recursive to true

    if (intersects.length > 0) {
        // Find the top-level group/mesh that holds the userData
        let clickedObject = intersects[0].object;
        while (clickedObject.parent && !clickedObject.userData.name) {
            clickedObject = clickedObject.parent;
        }

        const nodeData = clickedObject.userData;

        if (nodeData.name && nodeData.u !== undefined) {
            // 1. Show Info Panel
            nodeTitle.textContent = nodeData.name;
            nodeDescription.textContent = nodeData.description; // Set description
            infoPanel.classList.add('info-panel-open'); // UPDATED

            // 2. Trigger Animation
            const u = nodeData.u;
            const nodePos = nodeData.position.clone();

            isAnimating = true;
            animationStartTime = Date.now();
            controls.enabled = false; // Disable controls during animation

            // Store Start State
            startCameraPos.copy(camera.position);
            startControlsTarget.copy(controls.target);

            // Calculate End State (Street View)

            // 1. Target to look at: The node itself, at eye level
            endControlsTarget.copy(nodePos);
            endControlsTarget.y = 1.5; // Look at center of building

            // 2. Target camera position:
            // MODIFIED LOGIC: Position camera halfway between this node and the previous one
            // to prevent the previous node from blocking the view.
            if (u === 0) {
                // Special case for the first node (u=0)
                // Position camera "in front" of the start
                const tangent = pathCurve.getTangentAt(0).normalize();
                endCameraPos.copy(nodePos);
                endCameraPos.sub(tangent.multiplyScalar(7)); // Move 7 units "in front" (backwards)
                endCameraPos.y = 3;
            } else {
                // For all other nodes, position camera halfway to the previous node
                const deltaU = 1 / (journeyNodes.length - 1); // Get the 'u' distance between nodes
                const cameraU = u - (deltaU / 2); // Get 'u' value halfway to previous node

                endCameraPos = pathCurve.getPointAt(cameraU);
                endCameraPos.y = 3; // "Street level" camera height
            }

        } else {
            infoPanel.classList.remove('info-panel-open'); // UPDATED
        }
    } else {
        infoPanel.classList.remove('info-panel-open'); // UPDATED
    }
}

// --- Animation Loop ---
function animate() {
    requestAnimationFrame(animate);

    // NEW: Animation logic
    if (isAnimating) {
        const now = Date.now();
        const elapsed = now - animationStartTime;
        let t = elapsed / animationDuration;

        if (t >= 1) {
            t = 1;
            isAnimating = false;
            controls.enabled = true; // Re-enable controls
        }

        const easedT = easeInOutCubic(t); // Apply easing

        // Interpolate camera position
        camera.position.lerpVectors(startCameraPos, endCameraPos, easedT);

        // Interpolate controls target (what to look at)
        controls.target.lerpVectors(startControlsTarget, endControlsTarget, easedT);

    }

    controls.update();
    renderer.render(scene, camera);

    // Animate windmill blades
    scene.traverse(function(object) {
        if (object.userData.bladesPivot) {
            object.userData.bladesPivot.rotation.z += 0.02; // Rotate around Z-axis (which now points forward)
        }
    });
}

// --- Run ---
init();