import * as THREE from 'three';
import { OrbitControls } from 'OrbitControls';
import { GLTFLoader } from 'GLTFLoader';
import { DRACOLoader } from 'DRACOLoader';

// --- Global Variables ---
let scene, camera, renderer, controls;
const nodeObjects = [];
let raycaster, mouse;
let pathCurve;
let currentMilestoneIndex = 0;

// --- Loading Manager ---
const manager = new THREE.LoadingManager();
const gltfLoader = new GLTFLoader(manager);
const dracoLoader = new DRACOLoader(manager);

// --- Animation Variables ---
let isAnimating = false;
let animationStartTime;
const animationDuration = 600;
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
const loadingOverlay = document.getElementById('loading-overlay');
const loadingBar = document.getElementById('loading-bar');
const loadingPercentage = document.getElementById('loading-percentage');

// --- Student Journey Data ---
const journeyNodes = [
    { nodeId: 1, name: "Orientation", type: "start", color: 0xFF6347, description: "Welcome! Your first step on this amazing journey." }, // Tomato Red
    { nodeId: 2, name: "First Day of Class", type: "checkpoint", color: 0xF6E05E, description: "Classes begin. Find your rooms and meet your professors." }, // Yellow
    { nodeId: 3, name: "Join a Club", type: "checkpoint", color: 0xED8936, description: "Get involved! Find a club that matches your interests." }, // Orange
    { nodeId: 4, name: "First Midterm", type: "milestone", color: 0x4299E1, description: "Your first big test. You've got this!" }, // Blue
    { nodeId: 5, name: "Fall Break", type: "checkpoint", color: 0x68D391, description: "A well-deserved rest. Recharge for the rest of the semester." }, // Green
    { nodeId: 6, name: "Final Exams (Sem 1)", type: "milestone", color: 0x9F7AEA, description: "Finish the semester strong!" }, // Purple
    { nodeId: 7, name: "Winter Break", type: "checkpoint", color: 0x4FD1C5, description: "Enjoy the holidays and relax." }, // Teal
    { nodeId: 8, name: "Start Semester 2", type: "checkpoint", color: 0xF6E05E, description: "A fresh start for a new semester." }, // Yellow
    { nodeId: 9, name: "Declare Major", type: "milestone", color: 0xF56565, description: "A big decision! Choose your path of study." }, // Red
    { nodeId: 10, name: "Spring Midterms", type: "milestone", color: 0xED8936, description: "Halfway through the second semester. Keep it up!" }, // Orange
    { nodeId: 11, name: "Apply for Internship", type: "checkpoint", color: 0x4299E1, description: "Start looking for real-world experience." }, // Blue
    { nodeId: 12, name: "Spring Break", type: "checkpoint", color: 0x68D391, description: "One last break before the final push." }, // Green
    { nodeId: 13, name: "Final Exams (Sem 2)", type: "milestone", color: 0x9F7AEA, description: "The end of your first year is in sight!" }, // Purple
    { nodeId: 14, name: "Summer Internship", type: "milestone", color: 0x4FD1C5, description: "Apply your knowledge in the field." }, // Teal
    { nodeId: 15, name: "Start Sophomore Year", type: "checkpoint", color: 0xF6E05E, description: "Welcome back! You're not a first-year anymore." }, // Yellow
    { nodeId: 16, name: "Research Project", type: "milestone", color: 0xF56565, description: "Dive deep into a topic that interests you." }, // Red
    { nodeId: 17, name: "Junior Year", type: "checkpoint", color: 0xED8936, description: "The upper-level classes begin." }, // Orange
    { nodeId: 18, name: "Senior Thesis", type: "milestone", color: 0x4299E1, description: "Your capstone project. Make it count!" }, // Blue
    { nodeId: 19, name: "Job Applications", type: "checkpoint", color: 0x68D391, description: "Prepare your resume and start applying." }, // Green
    { nodeId: 20, name: "Graduation Day", type: "milestone", color: 0x3182CE, description: "You did it! Congratulations, graduate!" }  // Darker Blue
];

// --- Easing Function ---
function easeInOutCubic(t) {
    return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
}

function init() {

    // --- 1. Setup Loading Manager Callbacks ---
    manager.onStart = function (url, itemsLoaded, itemsTotal) {
        console.log('Started loading: ' + url);
        loadingOverlay.style.display = 'flex';
    };

    manager.onLoad = function () {
        console.log('Loading complete!');
        loadingOverlay.style.display = 'none';
    };

    manager.onProgress = function (url, itemsLoaded, itemsTotal) {
        const progress = (itemsLoaded / itemsTotal) * 100;
        loadingBar.style.width = progress + '%';
        loadingPercentage.textContent = Math.round(progress) + '%';
    };

    // --- 2. Setup Draco Loader ---
    // You MUST host these decoder files from your server.
    // They are in the 'three/examples/jsm/libs/draco/' folder of the three.js download.
    // "TODO" Update this path to where you will store them
    dracoLoader.setDecoderPath('/js/libs/draco/');
    gltfLoader.setDRACOLoader(dracoLoader);

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
    //loadGroundModel();
    createRoadPath2();

    // 8. Create the Journey (Nodes)
    createJourneyGraph();

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

    document.getElementById('top-view-btn').addEventListener('click', animateToTopView);
    document.getElementById('focus-milestone-btn').addEventListener('click', focusNextMilestone);

    // NEW: Close panel listener
    closePanelButton.addEventListener('click', () => {
        infoPanel.classList.remove('info-panel-open');
    });

    // 10. Start Animation
    animate();
}

/**
 * Smoothly animates the camera to a top-down view of the whole map.
 */
function animateToTopView() {
    // Stop if we're already moving
    if (isAnimating) return;

    isAnimating = true;
    animationStartTime = Date.now();
    controls.enabled = false;

    // Store Start State
    startCameraPos.copy(camera.position);
    startControlsTarget.copy(controls.target);

    // Calculate End State (Top-down view)
    // We'll look at the center of the path (around 0, 0, -10) from high up
    endCameraPos.set(0, 100, 10); // High up (y=100), slightly angled
    endControlsTarget.set(0, 0, -10); // Look at the center of the path
}

/**
 * Finds the next milestone and animates to a slanted top-down view.
 */
function focusNextMilestone() {
    if (isAnimating) return;

    // 1. Find the next milestone (Same as before)
    let nextMilestone = null;
    let nextMilestoneIndex = -1;

    // Start searching from *after* the current index
    for (let i = currentMilestoneIndex + 1; i < journeyNodes.length; i++) {
        if (journeyNodes[i].type === 'milestone') {
            nextMilestone = journeyNodes[i];
            nextMilestoneIndex = i;
            break;
        }
    }

    // If no milestone is found, loop back to the first one
    if (!nextMilestone) {
        for (let i = 0; i < journeyNodes.length; i++) {
            if (journeyNodes[i].type === 'milestone') {
                nextMilestone = journeyNodes[i];
                nextMilestoneIndex = i;
                break;
            }
        }
    }

    if (!nextMilestone) return; // No milestones found at all

    // Update the current index
    currentMilestoneIndex = nextMilestoneIndex;
    const nodeData = nextMilestone;

    // 2. Open the info panel (Same as before)
    nodeTitle.textContent = nodeData.name;
    nodeDescription.textContent = nodeData.description;
    infoPanel.classList.add('info-panel-open');

    // --- 3. UPDATED: Animate the camera to "Slanted Top View" ---

    const u = currentMilestoneIndex / (journeyNodes.length - 1);
    const nodePos = pathCurve.getPointAt(u); // Position of the milestone
    const deltaU = 1 / (journeyNodes.length - 1);

    isAnimating = true;
    animationStartTime = Date.now();
    controls.enabled = false;

    // Store Start State
    startCameraPos.copy(camera.position);
    startControlsTarget.copy(controls.target);

    // --- NEW CAMERA LOGIC ---

    // 1. Calculate new End Controls Target (what to look at)
    // We want to look *past* the milestone, at the path ahead.
    // Let's aim ~3-4 nodes ahead to frame the next section.
    let targetU = u + (4 * deltaU);
    if (targetU > 1) targetU = 1; // Don't look past the end

    endControlsTarget = pathCurve.getPointAt(targetU);
    endControlsTarget.y = 0; // Look at the road level

    // 2. Calculate new End Camera Position (where the camera is)
    // "slanted top view" and "slightly from far"
    // We'll position the camera "over" the milestone, but high up and pulled back.
    endCameraPos.copy(nodePos); // Start at the milestone's XZ
    endCameraPos.y = 30;        // "slanted top angle" (high up)
    endCameraPos.z += 25;       // "slightly from far" (pull back from the node)
}

function loadGroundModel() {
            // Instantiate the loader
            const loader = new GLTFLoader();

            const modelPath = '/assets/interactiveMap/models/grass.glb';

            loader.load(
                modelPath,
                // onLoad callback
                function (gltf) {
                    const model = gltf.scene;

                    // Set position (slightly below y=0 to align with road)
                    model.position.y = -0.5;

                    // Set scale
                    // The old plane was 150x150 units.
                    // --- You MUST adjust this scale to match your model's original size ---
                    model.scale.set(75, 75, 75); // Example scale, ADJUST AS NEEDED

                    // Enable shadow receiving for all meshes in the model
                    model.traverse(function (node) {
                        if (node.isMesh) {
                            node.receiveShadow = true;
                        }
                    });

                    // Add the loaded model to the scene
                    scene.add(model);
                },
                // onProgress callback (optional)
                function (xhr) {
                    // console.log((xhr.loaded / xhr.total * 100) + '% loaded');
                },
                // onError callback (important)
                function (error) {
                    console.error('An error happened while loading the ground model:', error);
                    // As a fallback, create the old green plane
                    const groundGeometry = new THREE.PlaneGeometry(150, 150);
                    const groundMaterial = new THREE.MeshStandardMaterial({ color: 0xA0E6A0, roughness: 0.8, metalness: 0.1 });
                    const ground = new THREE.Mesh(groundGeometry, groundMaterial);
                    ground.rotation.x = -Math.PI / 2;
                    ground.position.y = -0.5;
                    ground.receiveShadow = true;
                    scene.add(ground);
                }
            );
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

    function createStartNode(position, data, clickData) {
        const towerGroup = new THREE.Group();
        towerGroup.position.copy(position);

        const modelPath = '/assets/interactiveMap/models/start_shield.glb';

        gltfLoader.load(
            modelPath,
            // ----- onLoad callback -----
            // This function runs when the model has successfully loaded
            (gltf) => {
                const model = gltf.scene;
                model.userData = clickData;

                model.scale.set(0.5, 0.5, .5);
                 model.rotation.y = -0.5;
                 model.rotation.x = 0;
                 model.position.y = 2.8;

                // Set shadows and apply the data.color to all its parts
                model.traverse((child) => {
                    if (child.isMesh) {
                        child.castShadow = true;
                        child.receiveShadow = true;

                    }
                });

                // Add the fully loaded model to the group
                towerGroup.add(model);
            },
            // ----- onProgress callback (optional) -----
            undefined,
            // ----- onError callback -----
            (error) => {
                console.error('An error happened while loading the tower model:', error);
                // As a fallback, you could add the old cone/cylinder code here
            }
        );

        towerGroup.position.copy(position);
        return towerGroup;
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

    function createMileStoneNode(position, data, clickData) {
        const towerGroup = new THREE.Group();
        towerGroup.position.copy(position);

        const modelPath = '/assets/interactiveMap/models/milestone.glb';

        gltfLoader.load(
            modelPath,
            // ----- onLoad callback -----
            // This function runs when the model has successfully loaded
            (gltf) => {
                const model = gltf.scene;
                model.userData = clickData;

                // --- Adjust model scale, position, and rotation ---
                // You will likely need to tweak these values to make
                // your model fit and orient correctly on the path.
                model.scale.set(1, 1, 1);       // Example: scale it down
                // model.rotation.y = Math.PI;  // Example: rotate it
                 model.position.y = 1.2;      // Example: move it up

                // Set shadows and apply the data.color to all its parts
                model.traverse((child) => {
                    if (child.isMesh) {
                        child.castShadow = true;
                        child.receiveShadow = true;

                        // This applies the milestone's color to your model's material
//                        if (child.material) {
//                            // We clone the material so each node can have a unique color
//                            child.material = child.material.clone();
//                            child.material.color.set(data.color);
//                        }
                    }
                });

                // Add the fully loaded model to the group
                towerGroup.add(model);
            },
            // ----- onProgress callback (optional) -----
            undefined,
            // ----- onError callback -----
            (error) => {
                console.error('An error happened while loading the tower model:', error);
                // As a fallback, you could add the old cone/cylinder code here
            }
        );

        towerGroup.position.copy(position);
        return towerGroup;
    }

function createJourneyGraph() {
    const journeyGroup = new THREE.Group();
    const nodesCount = journeyNodes.length;

    for (let i = 0; i < nodesCount; i++) {
        const u = i / (nodesCount - 1);
        const currentNodePos = pathCurve.getPointAt(u);
        currentNodePos.y += 0.2;

        const nodeData = journeyNodes[i];

        // --- Create the userData object FIRST ---
        const clickData = {
            nodeId: nodeData.nodeId,
            name: nodeData.name,
            description: nodeData.description,
            u: u,
            position: currentNodePos.clone()
        };

        let nodeModel;

        switch(nodeData.type) {
            case 'start':
                nodeModel = createStartNode(currentNodePos, nodeData, clickData);
                break;
            case 'checkpoint':
                nodeModel = createCheckpointBuilding(currentNodePos, nodeData);
                break;
            case 'milestone':
                nodeModel = createMileStoneNode(currentNodePos, nodeData, clickData); // Assuming this exists
                break;
            default:
                nodeModel = createCheckpointBuilding(currentNodePos, nodeData);
        }

        // Set userData on the main group. This is still
        // correct for programmatic models like createCheckpointBuilding.
        nodeModel.userData = clickData;

        journeyGroup.add(nodeModel);
        nodeObjects.push(nodeModel);
    }
    scene.add(journeyGroup);
}

function createJourneyGraphOrg() {
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
                    nodeModel = createStartNode(currentNodePos, nodeData);
                    break;
                case 'checkpoint':
                    nodeModel = createCheckpointBuilding(currentNodePos, nodeData);
                    break;
                case 'milestone':
                    nodeModel = createMileStoneNode(currentNodePos, nodeData);
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
/**
 * Helper function to create the 2D canvas path from the 3D curve
 */
function buildRoadCanvasPath(ctx, pathCurve, groundSize, canvasSize) {
    // Get many points from the 3D curve to draw a smooth 2D line
    const points = pathCurve.getPoints(200);

    ctx.beginPath();

    // Map the first 3D point (x, z) to 2D canvas coordinates (x, y)
    let firstP = points[0];
    let firstX = (firstP.x + groundSize / 2) / groundSize * canvasSize;
    let firstY = (groundSize / 2 + firstP.z) / groundSize * canvasSize;
    ctx.moveTo(firstX, firstY);

    // Draw lines to all subsequent points
    for (let i = 1; i < points.length; i++) {
        const p = points[i];
        // Map 3D world (x, z) to 2D canvas (x, y)
        const canvasX = (p.x + groundSize / 2) / groundSize * canvasSize;
        // This line correctly maps the Z-axis
        const canvasY = (groundSize / 2 + p.z) / groundSize * canvasSize;
        ctx.lineTo(canvasX, canvasY);
    }
}

function createRoadPath() {
    // 1. SET SCENE BACKGROUND (Your existing code)
//    const skyColor = new THREE.Color(0xE0E5EC);
//    const groundColor = new THREE.Color(0xdee9fd);
//
//    const canvasBg = document.createElement('canvas');
//    canvasBg.width = 1;
//    canvasBg.height = 256;
//    const ctxBg = canvasBg.getContext('2d');
//
//    const gradient = ctxBg.createLinearGradient(0, 0, 0, canvasBg.height);
//    gradient.addColorStop(0, '#dee9fd');
//    gradient.addColorStop(1, '#eaf2fd');
//    ctxBg.fillStyle = gradient;
//    ctxBg.fillRect(0, 0, 1, canvasBg.height);

//    scene.background = new THREE.CanvasTexture(canvasBg);
//    scene.background.needsUpdate = true;

    // 2. DEFINE 3D PATH (This is the single source of truth)
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

    // This curve is used for BOTH the ground and the tube
    pathCurve = new THREE.CatmullRomCurve3(pathPoints, false, 'centripetal', 0.5);

    // --- 3. CREATE THE GROUND PLANE WITH "INSET" SHADOWS ---

    // The 3D size of the ground plane
    const groundSize = 550;
    // The 2D resolution of the texture
    const canvasSize = 1024;

    // Colors for the ground (must match your scene background)
    const groundPaintColor = '#eaf2fd';
    const shadowDark = 'rgba(163, 177, 198, 0.7)';
    const shadowLight = 'rgba(255, 255, 255, 1)';

    // Settings for the shadow effect
    const roadTrenchWidth = 40; // Width of the "trench" in pixels
    const shadowOffset = 3;

    // Create a 2D canvas to draw on
    const canvasGround = document.createElement('canvas');
    canvasGround.width = canvasSize;
    canvasGround.height = canvasSize;
    const ctxGround = canvasGround.getContext('2d');

    // Step A: Draw the solid light-colored ground
    ctxGround.fillStyle = groundPaintColor;
    ctxGround.fillRect(0, 0, canvasSize, canvasSize);

    // Use sharp line caps and joins for clean alignment
    ctxGround.lineCap = 'butt';
    ctxGround.lineJoin = 'miter';

    // Pass 1: Draw the DARK SHADOW (offset to bottom-right)
    ctxGround.save();
    ctxGround.translate(shadowOffset, shadowOffset);
    buildRoadCanvasPath(ctxGround, pathCurve, groundSize, canvasSize); // Uses the curve
    ctxGround.strokeStyle = shadowDark;
    ctxGround.lineWidth = roadTrenchWidth;
    ctxGround.stroke();
    ctxGround.restore();

    // Pass 2: Draw the LIGHT HIGHLIGHT (offset to top-left)
    ctxGround.save();
    ctxGround.translate(-shadowOffset, -shadowOffset);
    buildRoadCanvasPath(ctxGround, pathCurve, groundSize, canvasSize); // Uses the curve
    ctxGround.strokeStyle = shadowLight;
    ctxGround.lineWidth = roadTrenchWidth;
    ctxGround.stroke();
    ctxGround.restore();

    // Pass 3: "Cut out" the middle with the ground color
    buildRoadCanvasPath(ctxGround, pathCurve, groundSize, canvasSize); // Uses the curve
    ctxGround.strokeStyle = groundPaintColor;
    ctxGround.lineWidth = roadTrenchWidth - (shadowOffset * 2);
    ctxGround.stroke();

    // Create the 3D ground plane
    const groundTexture = new THREE.CanvasTexture(canvasGround);
    groundTexture.needsUpdate = true;

    // Make sure the 3D plane geometry matches the groundSize
    const groundGeometry = new THREE.PlaneGeometry(groundSize, groundSize);
    const groundMaterial = new THREE.MeshStandardMaterial({
        map: groundTexture,
        roughness: 0.9,
        metalness: 0.1
    });

    const ground = new THREE.Mesh(groundGeometry, groundMaterial);
    ground.rotation.x = -Math.PI / 2;
    ground.position.y = 0; // Sits at y=0
    ground.receiveShadow = true;
    scene.add(ground);


    // --- 4. CREATE YOUR TUBE (Your existing code) ---

//    const roadSegments = 100;
//    const canvas = document.createElement('canvas');
//    canvas.width = 256;
//    canvas.height = 1;
//    const context = canvas.getContext('2d');
//    const roadGradient = context.createLinearGradient(0, 0, canvas.width, 0);
//
//    roadGradient.addColorStop(0, '#010430');
//    roadGradient.addColorStop(0.5, '#010861');
//    roadGradient.addColorStop(1, '#010439');
//
//    context.fillStyle = roadGradient;
//    context.fillRect(0, 0, canvas.width, canvas.height);
//
//    const gradientTexture = new THREE.CanvasTexture(canvas);
//
//    // The tube is built from the EXACT SAME pathCurve
//    const roadGeometry = new THREE.TubeGeometry(pathCurve, roadSegments, 2, 20, false);
//    const roadMaterial = new THREE.MeshStandardMaterial(
//    {
////        color: 0xF0F0F0,
//        map: gradientTexture,
//        roughness: 0.7,
//        metalness: 0.1,
//        side: THREE.DoubleSide
//    }
//    );
//    const road = new THREE.Mesh(roadGeometry, roadMaterial);
//
//    // Lift the tube slightly so it sits "in" the trench
//    road.position.y = 0.1;
//
//    road.receiveShadow = true;
//    road.castShadow = true;
//    scene.add(road);
}

//original
function createRoadPath2() {
// NEW Neumorphic background colors
        const skyColor = new THREE.Color(0xE0E5EC); // Light grey-blue
        const groundColor = new THREE.Color(0xdee9fd); // Very light grey

        const canvasBg = document.createElement('canvas');
        canvasBg.width = 1;
        canvasBg.height = 256;
        const ctxBg = canvasBg.getContext('2d');

        const gradient = ctxBg.createLinearGradient(0, 0, 0, canvasBg.height);
        // Gradient from light grey-blue (top) to pure light grey (bottom)
        gradient.addColorStop(0, '#dee9fd');
        gradient.addColorStop(1, '#eaf2fd');
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
////gradient.addColorStop(0, '#0f0c29');
////gradient.addColorStop(0.5, '#302b63');
////gradient.addColorStop(1, '#24243e');
//
roadGradient.addColorStop(0, '#010439');
roadGradient.addColorStop(0.5, '#010861');
roadGradient.addColorStop(1, '#010439');
//
//// Define the gradient
//const roadGradient = context.createLinearGradient(0, 0, canvas.width, 0);
//
//// NEW "INSET" GRADIENT
//roadGradient.addColorStop(0, '#eaf2fd');   // Light Edge (matches background)
//roadGradient.addColorStop(0.5, '#010861'); // Dark Center (the "shadow")
//roadGradient.addColorStop(1, '#eaf2fd');   // Light Edge (matches background)

context.fillStyle = roadGradient;
context.fillRect(0, 0, canvas.width, canvas.height);

// Create a CanvasTexture from the canvas
const gradientTexture = new THREE.CanvasTexture(canvas);

//const roadGeometry = new THREE.TubeGeometry(pathCurve, roadSegments, 2, 20, false);
// Use 6 or 8 radial segments instead of 20 to create flat, beveled sides
const roadGeometry = new THREE.TubeGeometry(pathCurve, roadSegments, 2, 20, false);
const roadMaterial = new THREE.MeshStandardMaterial(
{
color: 0xF0F0F0,
//map: new THREE.TextureLoader().load('/assets/interactiveMap/textures/universe-gradient.jpg'),
//map: new THREE.TextureLoader().load('/assets/interactiveMap/textures/temp3.webp'),
//map: gradientTexture,
roughness: 1,
metalness: 0.1,
side: THREE.DoubleSide
}
);
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
        while (clickedObject.parent && !clickedObject.userData.description) {
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

            activityBridge.handleOnCheckPointClick(nodeData.nodeId);

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