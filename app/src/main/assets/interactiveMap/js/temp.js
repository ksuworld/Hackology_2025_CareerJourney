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

//    scene.background = new THREE.Color(0x000000);

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
//    createEnvironment();
    createRoadPath2();
//    createRoadPath();

    // 8. Create the Journey (Nodes)
//    createJourneyGraph();

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

/**
 * Helper function to create the 2D canvas path from the 3D curve
 */
function buildRoadCanvasPath(ctx, pathCurve, groundSize, canvasSize) {
    const points = pathCurve.getPoints(200);

    ctx.beginPath();

    // Map the first 3D point (x, z) to 2D canvas coordinates (x, y)
    let firstP = points[0];
    let firstX = (firstP.x + groundSize / 2) / groundSize * canvasSize;
    let firstY = (groundSize / 2 - firstP.z) / groundSize * canvasSize;
    ctx.moveTo(firstX, firstY);

    // Draw lines to all subsequent points
    for (let i = 1; i < points.length; i++) {
        const p = points[i];
        const canvasX = (p.x + groundSize / 2) / groundSize * canvasSize;
        const canvasY = (groundSize / 2 - p.z) / groundSize * canvasSize;
        ctx.lineTo(canvasX, canvasY);
    }
}

    // --- Node Creation Functions (Revised for more stylized buildings) ---

    function createStartBuilding(position, data) {
        const buildingGroup = new THREE.Group();

        // Main block
        const baseGeometry = new THREE.BoxGeometry(3, 2.5, 3);
        const baseMaterial = new THREE.MeshStandardMaterial({ color: data.color, roughness: 0.6 });
        const base = new THREE.Mesh(baseGeometry, baseMaterial);
        base.position.y = 1.25;
        base.castShadow = true;
        base.receiveShadow = true;
        buildingGroup.add(base);

        // Roof
        const roofGeometry = new THREE.ConeGeometry(2.5, 2, 4);
        const roofMaterial = new THREE.MeshStandardMaterial({ color: 0xA0522D, roughness: 0.7 }); // Sienna brown
        const roof = new THREE.Mesh(roofGeometry, roofMaterial);
        roof.position.y = 3.25;
        roof.rotation.y = Math.PI / 4;
        roof.castShadow = true;
        roof.receiveShadow = true;
        buildingGroup.add(roof);

        buildingGroup.position.copy(position);
        return buildingGroup;
    }

    function createCheckpointBuilding(position, data) {
        const homeGroup = new THREE.Group();

        // Base
        const baseGeometry = new THREE.BoxGeometry(2, 1.5, 2);
        const baseMaterial = new THREE.MeshStandardMaterial({ color: data.color, roughness: 0.6 });
        const base = new THREE.Mesh(baseGeometry, baseMaterial);
        base.position.y = 0.75;
        base.castShadow = true;
        base.receiveShadow = true;
        homeGroup.add(base);

        // Roof
        const roofGeometry = new THREE.ConeGeometry(1.5, 1.2, 4); // 4 sides = pyramid roof
        const roofMaterial = new THREE.MeshStandardMaterial({ color: 0x8B4513, roughness: 0.7 }); // Brown
        const roof = new THREE.Mesh(roofGeometry, roofMaterial);
        roof.position.y = 1.65; // On top of the base
        roof.rotation.y = Math.PI / 4;
        roof.castShadow = true;
        roof.receiveShadow = true;
        homeGroup.add(roof);

        homeGroup.position.copy(position);
        return homeGroup;
    }

    function createMilestoneTower(position, data) {
        const towerGroup = new THREE.Group();

        // Base cylinder
        const baseGeometry = new THREE.CylinderGeometry(1, 1.2, 3, 8);
        const baseMaterial = new THREE.MeshStandardMaterial({ color: data.color, roughness: 0.5 });
        const base = new THREE.Mesh(baseGeometry, baseMaterial);
        base.position.y = 1.5;
        base.castShadow = true;
        base.receiveShadow = true;
        towerGroup.add(base);

        // Top cone
        const topGeometry = new THREE.ConeGeometry(0.8, 1.5, 8);
        const topMaterial = new THREE.MeshStandardMaterial({ color: 0x696969, roughness: 0.5 }); // Dark gray
        const top = new THREE.Mesh(topGeometry, topMaterial);
        top.position.y = 3.75;
        top.castShadow = true;
        top.receiveShadow = true;
        towerGroup.add(top);

        towerGroup.position.copy(position);
        return towerGroup;
    }


function createJourneyGraph() {
        const journeyGroup = new THREE.Group();

        const nodesCount = journeyNodes.length;

        // Note: pathCurve is now global, no need to redefine

        for (let i = 0; i < nodesCount; i++) {
            const u = i / (nodesCount - 1);
            const currentNodePos = pathCurve.getPointAt(u);
            currentNodePos.y += 0.2; // Lift nodes slightly above the road surface

            const nodeData = journeyNodes[i];
            let nodeModel;

            switch(nodeData.type) {
                case 'start':
                    nodeModel = createStartBuilding(currentNodePos, nodeData);
                    break;
                case 'checkpoint':
                    nodeModel = createCheckpointBuilding(currentNodePos, nodeData);
                    break;
                case 'milestone':
                    nodeModel = createMilestoneTower(currentNodePos, nodeData);
                    break;
                default:
                    nodeModel = createCheckpointBuilding(currentNodePos, nodeData);
            }

            // Store data for click events
            // NEW: Storing 'u', 'position', and 'description'
            nodeModel.userData = {
                name: nodeData.name,
                description: nodeData.description,
                u: u,
                position: currentNodePos.clone()
            };

            journeyGroup.add(nodeModel);

            // Add the main mesh (or group) to clickable objects
            nodeObjects.push(nodeModel);
        }

        scene.add(journeyGroup);
    }

function createRoadPath() {
// NEW Neumorphic background colors
        const skyColor = new THREE.Color(0xE0E5EC); // Light grey-blue
        const groundColor = new THREE.Color(0xF0F5FB); // Very light grey

        const canvasBg = document.createElement('canvas');
        canvasBg.width = 1;
        canvasBg.height = 256;
        const ctxBg = canvasBg.getContext('2d');

        const gradient = ctxBg.createLinearGradient(0, 0, 0, canvasBg.height);
        // Gradient from light grey-blue (top) to pure light grey (bottom)
        gradient.addColorStop(0, '#' + skyColor.getHexString());
        gradient.addColorStop(1, '#' + groundColor.getHexString());
        ctxBg.fillStyle = gradient;
        ctxBg.fillRect(0, 0, 1, canvasBg.height);

        scene.background = new THREE.CanvasTexture(canvasBg);
        scene.background.needsUpdate = true;

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
const roadSegments = 100;

// Create a canvas element for the gradient
const canvas = document.createElement('canvas');
canvas.width = 256;
canvas.height = 1;
const context = canvas.getContext('2d');

//background-image: linear-gradient(to top, #f43b47 0%, #453a94 100%);
// Define the gradient
const roadGradient = context.createLinearGradient(0, 0, canvas.width, 0);
//gradient.addColorStop(0, '#0f0c29');
//gradient.addColorStop(0.5, '#302b63');
//gradient.addColorStop(1, '#24243e');

roadGradient.addColorStop(0, '#010439');
roadGradient.addColorStop(0.5, '#010861');
roadGradient.addColorStop(1, '#010439');

context.fillStyle = roadGradient;
context.fillRect(0, 0, canvas.width, canvas.height);

// Create a CanvasTexture from the canvas
const gradientTexture = new THREE.CanvasTexture(canvas);

const roadGeometry = new THREE.TubeGeometry(pathCurve, roadSegments, 2, 20, false);
const roadMaterial = new THREE.MeshStandardMaterial(
{
color: 0xF0F0F0,
//map: new THREE.TextureLoader().load('/assets/interactiveMap/textures/universe-gradient.jpg'),
//map: new THREE.TextureLoader().load('/assets/interactiveMap/textures/temp3.webp'),
map: gradientTexture,
roughness: 0.7,
metalness: 0.1,
side: THREE.DoubleSide
}
);
const road = new THREE.Mesh(roadGeometry, roadMaterial);
road.receiveShadow = true;
road.castShadow = true;
scene.add(road);

}

function createRoadPath2() {
    // 1. Define the 3D Path
    const pathPoints = [
        new THREE.Vector3(0, 0, 35),
        new THREE.Vector3(5, 0, 25),
        new THREE.Vector3(-5, 0, 15),
        new THREE.Vector3(10, 0, 0),
        new THREE.Vector3(0, 0, -10),
        new THREE.Vector3(-15, 0, -20),
        new THREE.Vector3(-5, 0, -30),
        new THREE.Vector3(10, 0, -40),
        new THREE.Vector3(0, 0, -50),
    ];

    pathCurve = new THREE.CatmullRomCurve3(pathPoints, false, 'centripetal', 0.5);

    // --- 2. Create the "Inset" Road as a 2D Texture ---

    const groundSize = 100;
    const canvasSize = 1024;

    // Neumorphic colors

//        gradient.addColorStop(0, '#dee9fd');
//        gradient.addColorStop(1, '#eaf2fd');
    const groundColor = '#dee9fd';
    const shadowDark = 'rgba(163, 177, 198, 0.7)';
    const shadowLight = 'rgba(255, 255, 255, 1)';
    const roadColor = '#4A90E2';

    // Settings for the effect
    const roadWidth = 40;     // Total width of the "trench"
    const shadowOffset = 3;   // How far to offset the shadows

    // The final blue road will be slightly thinner
    const innerRoadWidth = roadWidth - shadowOffset;

    // Create a 2D canvas to draw on
    const canvas = document.createElement('canvas');
    canvas.width = canvasSize;
    canvas.height = canvasSize;
    const ctx = canvas.getContext('2d');

    // Step A: Draw the solid light-grey ground
    ctx.fillStyle = groundColor;
    ctx.fillRect(0, 0, canvasSize, canvasSize);

    // --- FIX: Use sharp line caps and joins for clean alignment ---
    ctx.lineCap = 'butt';
    ctx.lineJoin = 'miter';

    // --- Pass 1: Draw the DARK SHADOW (offset to bottom-right) ---
    ctx.save();
    ctx.translate(shadowOffset, shadowOffset); // Offset the whole canvas
    buildRoadCanvasPath(ctx, pathCurve, groundSize, canvasSize); // Build the path
    ctx.strokeStyle = shadowDark;
    ctx.lineWidth = roadWidth;
    ctx.stroke();
    ctx.restore(); // Reset the canvas transform

    // --- Pass 2: Draw the LIGHT HIGHLIGHT (offset to top-left) ---
    ctx.save();
    ctx.translate(-shadowOffset, -shadowOffset); // Offset the other way
    buildRoadCanvasPath(ctx, pathCurve, groundSize, canvasSize); // Build the path
    ctx.strokeStyle = shadowLight;
    ctx.lineWidth = roadWidth;
    ctx.stroke();
    ctx.restore();

    // --- Pass 3: Draw the BLUE ROAD on top ---
    // This covers the messy seam in the middle and looks clean.
    buildRoadCanvasPath(ctx, pathCurve, groundSize, canvasSize);
    ctx.strokeStyle = roadColor;
    ctx.lineWidth = innerRoadWidth;
    ctx.stroke();

    // 3. Create the 3D Ground Plane
    const groundTexture = new THREE.CanvasTexture(canvas);
    groundTexture.needsUpdate = true;

    const groundGeometry = new THREE.PlaneGeometry(groundSize, groundSize);
    const groundMaterial = new THREE.MeshStandardMaterial({
        map: groundTexture,
        roughness: 0.9,
        metalness: 0.1
    });

    const ground = new THREE.Mesh(groundGeometry, groundMaterial);
    ground.rotation.x = -Math.PI / 2;
    ground.position.y = 0;
    ground.receiveShadow = true;
    scene.add(ground);
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