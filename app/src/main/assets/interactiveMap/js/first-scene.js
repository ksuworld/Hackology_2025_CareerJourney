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
const animationDuration = 500; // 1.5 seconds
let startCameraPos = new THREE.Vector3();
let endCameraPos = new THREE.Vector3();
let startControlsTarget = new THREE.Vector3();
let endControlsTarget = new THREE.Vector3();

// --- DOM Elements ---
const canvas = document.getElementById('journey-canvas');
const infoPanel = document.getElementById('info-panel');
const nodeTitle = document.getElementById('node-title');
const nodeDescription = document.getElementById('node-description'); // Get description element
const closePanelButton = document.getElementById('close-panel'); // NEW: Close button

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

// --- Initialization ---
function init() {
    // 1. Scene
    scene = new THREE.Scene();

    // NEW: Load the sky background model first
    //loadSkyBackgroundModel();

                scene.background = new THREE.Color(0x87CEEB); // Sky blue
    // 2. Camera
    camera = new THREE.PerspectiveCamera(50, window.innerWidth / window.innerHeight, 0.1, 1000);
    camera.position.set(0, 50, 40);
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
    controls.target.set(0, 0, 0); // Look at center of map
    controls.minPolarAngle = Math.PI / 4;
    controls.maxPolarAngle = Math.PI / 2 - 0.1;
    controls.minDistance = 30;
    controls.maxDistance = 100;

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
    createEnvironment();

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

    // NEW: Close panel listener
    closePanelButton.addEventListener('click', () => {
        infoPanel.classList.remove('info-panel-open');
    });

    // 10. Start Animation
    animate();
}

// --- Create Environment (Ground, Road, Trees, Static Buildings) ---
function createEnvironment() {
    // Ground Plane
    loadGroundModel();

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
    const roadWidth = 4;
    const roadSegments = 200;
    const roadGeometry = new THREE.TubeGeometry(pathCurve, roadSegments, roadWidth / 2, 8, false);
    const roadMaterial = new THREE.MeshStandardMaterial({ color: 0xF0F0F0, roughness: 0.7, metalness: 0.1 });
    const road = new THREE.Mesh(roadGeometry, roadMaterial);
    road.receiveShadow = true;
    road.castShadow = true;
    scene.add(road);

    // Create Sparse Trees
    const treePositions = [
        new THREE.Vector3(15, 0, 30), new THREE.Vector3(-15, 0, 30), // Bottom
        new THREE.Vector3(20, 0, 10), new THREE.Vector3(-20, 0, 10), // Mid-bottom
        new THREE.Vector3(25, 0, -5), new THREE.Vector3(-25, 0, -5), // Mid-center
        new THREE.Vector3(15, 0, -25), new THREE.Vector3(-15, 0, -25), // Mid-top
        new THREE.Vector3(20, 0, -45), new THREE.Vector3(-20, 0, -45), // Top
        new THREE.Vector3(30, 0, 0), new THREE.Vector3(-30, 0, 0), // Sides
        new THREE.Vector3(10, 0, 45), new THREE.Vector3(-10, 0, 45) // Further out
    ];
    //treePositions.forEach(pos => createBushes(pos));

    // Create Mountains
    const mountainPositions = [
        new THREE.Vector3(-40, 0, -5), new THREE.Vector3(40, 0, 20),
        new THREE.Vector3(-20, 0, 45), new THREE.Vector3(35, 0, -40),
        new THREE.Vector3(-10, 0, -60), new THREE.Vector3(45, 0, -15)
    ];
    // UPDATED: Call the new loader function
//    mountainPositions.forEach(pos => loadMountainModel(pos));

    // Create Lakes/Rivers
    //createLake(new THREE.Vector3(25, -0.2, 5), 10, 8); // Lake 1
    //createLake(new THREE.Vector3(-30, -0.2, -40), 12, 10); // Lake 2 (top left)

    // River segments
    //createRiverSegment(new THREE.Vector3(15, -0.3, -10), new THREE.Vector3(5, -0.3, -20));
    //createRiverSegment(new THREE.Vector3(5, -0.3, -20), new THREE.Vector3(-5, -0.3, -25));
    //createRiverSegment(new THREE.Vector3(-5, -0.3, -25), new THREE.Vector3(-10, -0.3, -15));
//
    //// Add Windmills
    //loadWindmillModel(new THREE.Vector3(30, 0, -50));
    //loadWindmillModel(new THREE.Vector3(40, 0, -55));
    //loadWindmillModel(new THREE.Vector3(25, 0, -60));
}

// --- Create a procedural normal map for water ---
function createWaterNormalMap() {
    const canvas = document.createElement('canvas');
    canvas.width = 128;
    canvas.height = 128;
    const ctx = canvas.getContext('2d');
    ctx.fillStyle = '#8080ff'; // Mid-blue (neutral normal)
    ctx.fillRect(0, 0, 128, 128);

    ctx.fillStyle = '#a0a0ff'; // Lighter blue (top-left light source)
    for (let i = 0; i < 200; i++) {
        const x = Math.random() * 128;
        const y = Math.random() * 128;
        const r = Math.random() * 2 + 1;
        ctx.beginPath();
        ctx.arc(x, y, r, 0, Math.PI * 2);
        ctx.fill();
    }

    ctx.fillStyle = '#6060ff'; // Darker blue (bottom-right shadow)
    for (let i = 0; i < 100; i++) {
        const x = Math.random() * 128;
        const y = Math.random() * 128;
        const r = Math.random() * 1.5 + 1;
        ctx.beginPath();
        ctx.arc(x, y, r, 0, Math.PI * 2);
        ctx.fill();
    }

    const texture = new THREE.CanvasTexture(canvas);
    texture.wrapS = THREE.RepeatWrapping;
    texture.wrapT = THREE.RepeatWrapping;
    texture.repeat.set(4, 4); // Tile the texture
    return texture;
}

// --- Create a stylized tree (matches reference image) ---
function createStylizedTree(position) {
    const treeGroup = new THREE.Group();

    // Trunk (simple cylinder, dark brown)
    const trunkHeight = 1.5;
    const trunkRadius = 0.4;
    const trunkGeometry = new THREE.CylinderGeometry(trunkRadius, trunkRadius, trunkHeight, 6);
    const trunkMaterial = new THREE.MeshStandardMaterial({ color: 0x8B4513, roughness: 0.9 }); // Brown
    const trunk = new THREE.Mesh(trunkGeometry, trunkMaterial);
    trunk.position.set(position.x, position.y + trunkHeight / 2, position.z);
    trunk.castShadow = true;
    trunk.receiveShadow = true;
    treeGroup.add(trunk);

    // Leaves (simplified spheres, rounded, more vibrant green)
    const leavesGeometry = new THREE.SphereGeometry(1.5, 12, 12);
    const leavesMaterial = new THREE.MeshStandardMaterial({ color: 0x2E8B57, roughness: 0.7 }); // Sea Green
    const leaves = new THREE.Mesh(leavesGeometry, leavesMaterial);
    leaves.position.set(position.x, position.y + trunkHeight + 1.0, position.z); // Position above trunk
    leaves.castShadow = true;
    leaves.receiveShadow = true;
    treeGroup.add(leaves);

    scene.add(treeGroup);
}

function createBushes(position) {
// Instantiate the loader
    const loader = new GLTFLoader();

    const modelPath = '/assets/interactiveMap/models/bush/celandine_01_2k.gltf';

    loader.load(
        modelPath,
        // onLoad callback
        function (gltf) {
            const model = gltf.scene;

            // Set position
            model.position.copy(position);

            // Set scale
            // The old procedural mountains were roughly 16 units wide
            // (from new THREE.DodecahedronGeometry(8, 0))
            // and 12 units high (base + cap).
            // --- You MUST adjust this scale to match your model's original size ---
            model.scale.set(8, 8, 8); // Example scale, ADJUST AS NEEDED

            // Enable shadows for all meshes in the model
            model.traverse(function (node) {
                if (node.isMesh) {
                    node.castShadow = true;
                    node.receiveShadow = true;
                }
            });

            // Add the loaded model to the scene
            scene.add(model);
        },
        // onProgress callback (optional)
        function (xhr) {
            // You can use this to show a loading percentage
            // console.log((xhr.loaded / xhr.total * 100) + '% loaded');
        },
        // onError callback (important)
        function (error) {
            console.error('An error happened while loading the mountain model:', error);
            // As a fallback, you could log the error or add a placeholder
        }
    );
}

function loadMountainModel(position) {
    const loader = new GLTFLoader();

    //const modelPath = '/assets/interactiveMap/models/rock/namaqualand_boulder_04_2k.gltf';
    const modelPath = '/assets/interactiveMap/models/rock.glb';
    //const modelPath = '/assets/interactiveMap/models/mountain.glb';

    loader.load(
        modelPath,
        // onLoad callback
        function (gltf) {
            const model = gltf.scene;

            // Set position
            model.position.copy(position);

            // Set scale
            // The old procedural mountains were roughly 16 units wide
            // (from new THREE.DodecahedronGeometry(8, 0))
            // and 12 units high (base + cap).
            // --- You MUST adjust this scale to match your model's original size ---
            model.scale.set(6, 6, 6)

            // Enable shadows for all meshes in the model
            model.traverse(function (node) {
                if (node.isMesh) {
                    node.castShadow = true;
                    node.receiveShadow = true;
                }
            });

            // Add the loaded model to the scene
            scene.add(model);
        },
        // onProgress callback (optional)
        function (xhr) {
            // You can use this to show a loading percentage
            // console.log((xhr.loaded / xhr.total * 100) + '% loaded');
        },
        // onError callback (important)
        function (error) {
            console.error('An error happened while loading the mountain model:', error);
            // As a fallback, you could log the error or add a placeholder
        }
    );
}


    // --- NEW: Function to load a GLB/GLTF ground/grass model ---
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

function createLake(position, width, depth) {
    const geometry = new THREE.PlaneGeometry(width, depth, 1, 1);
    const waterNormalMap = createWaterNormalMap(); // Get the procedural normal map

    const material = new THREE.MeshStandardMaterial({
        color: 0x4A90E2, // Blue
        metalness: 0.8,  // More reflective
        roughness: 0.2,  // Smoother surface
        opacity: 0.9,    // Slightly transparent
        transparent: true,
        side: THREE.DoubleSide, // Render both sides
        normalMap: waterNormalMap,
        normalScale: new THREE.Vector2(0.1, 0.1) // Adjust ripple intensity
    });
    const lake = new THREE.Mesh(geometry, material);
    lake.rotation.x = -Math.PI / 2;
    lake.position.copy(position);
    lake.receiveShadow = true;
    scene.add(lake);
}

// --- Create a river segment ---
function createRiverSegment(start, end) {
    const riverWidth = 2; // Width of the river segment
    const midPoint = new THREE.Vector3().lerpVectors(start, end, 0.5);
    const length = start.distanceTo(end);
    const waterNormalMap = createWaterNormalMap(); // Get the procedural normal map

    const geometry = new THREE.PlaneGeometry(riverWidth, length, 1, 1);
    const material = new THREE.MeshStandardMaterial({
        color: 0x4A90E2, // Blue
        metalness: 0.8,  // More reflective
        roughness: 0.2,  // Smoother surface
        opacity: 0.9,    // Slightly transparent
        transparent: true,
        side: THREE.DoubleSide,
        normalMap: waterNormalMap,
        normalScale: new THREE.Vector2(0.1, 0.1) // Adjust ripple intensity
    });
    const river = new THREE.Mesh(geometry, material);

    river.position.copy(midPoint);
    river.position.y = -0.3; // Slightly below ground level

    // Orient the plane to face along the segment
    river.lookAt(new THREE.Vector3(end.x, -0.3, end.z));
    river.rotateX(Math.PI / 2); // Make it horizontal

    river.receiveShadow = true;
    scene.add(river);
}


    // --- NEW: Function to load a GLB/GLTF windmill model ---
    function loadWindmillModel(position) {
        // Instantiate the loader
        const loader = new GLTFLoader();

        const modelPath = '/assets/interactiveMap/models/windmill.glb';

        loader.load(
            modelPath,
            // onLoad callback
            function (gltf) {
                const model = gltf.scene;

                // Set position
                model.position.copy(position);

                // NEW: Lift the model
                // The 'position' vector has y=0 (ground level).
                // If your model's pivot is in its center, this will put it
                // halfway into the ground. Add to 'y' to lift it up.
                // --- ADJUST THIS VALUE to make your model sit on the ground ---
                model.position.y += 2.5; // Example: lifting it by 2.5 units

                // Set scale
                // The old procedural windmill was ~6-10 units high.
                // --- You MUST adjust this scale to match your model's original size ---
                model.scale.set(0.5, 0.5, 0.5); // Using 2 as requested

                // Enable shadows for all meshes in the model
                model.traverse(function (node) {
                    if (node.isMesh) {
                        node.castShadow = true;
                        node.receiveShadow = true;
                    }
                });

                // --- Animation Handling ---
                // Check if the model has any animations
                if (gltf.animations && gltf.animations.length) {
                    // Create an animation mixer
                    const mixer = new THREE.AnimationMixer(model);

                    // Play the first animation clip
                    const action = mixer.clipAction(gltf.animations[0]);
                    action.play();

                    // Add the mixer to our array of mixers to be updated
                    mixers.push(mixer);
                }
                // --- End Animation Handling ---

                // Add the loaded model to the scene
                scene.add(model);
            },
            // onProgress callback (optional)
            function (xhr) {
                // console.log((xhr.loaded / xhr.total * 100) + '% loaded');
            },
            // onError callback (important)
            function (error) {
                console.error('An error happened while loading the windmill model:', error);
            }
        );
    }

function loadSkyBackgroundModel() {

                scene.background = new THREE.Color(0x87CEEB); // Sky blue
        // Instantiate the loader
        const loader = new GLTFLoader();

        const modelPath = '/assets/interactiveMap/models/clouds.glb';

        loader.load(
            modelPath,
            // onLoad callback
            function (gltf) {
                const model = gltf.scene;

                // Set position to center
                model.position.set(0, 0, 0);

                // Set scale
                // This should be large enough to surround the entire scene
                // --- You MUST adjust this scale to match your model's original size ---
                model.scale.set(200, 200, 200); // Example scale, ADJUST AS NEEDED

                // Disable shadows
                model.traverse(function (node) {
                    if (node.isMesh) {
                        node.castShadow = false;
                        node.receiveShadow = false;
                    }
                });

                // Add the loaded model to the scene
                scene.add(model);
            },
            // onProgress callback (optional)
            function (xhr) {
                // console.log('Sky: ' + (xhr.loaded / xhr.total * 100) + '% loaded');
            },
            // onError callback (important)
            function (error) {
                console.error('An error happened while loading the sky model:', error);
                // As a fallback, you could set a simple color background
                scene.background = new THREE.Color(0x87CEEB); // Sky blue
            }
        );
    }

// --- Create a stylized windmill ---
function createWindmill(position) {
    const windmillGroup = new THREE.Group();

    // Base tower
    const towerGeometry = new THREE.CylinderGeometry(1.5, 2.5, 6, 8);
    const towerMaterial = new THREE.MeshStandardMaterial({ color: 0xD2B48C, roughness: 0.7 }); // Tan
    const tower = new THREE.Mesh(towerGeometry, towerMaterial);
    tower.position.y = 3;
    tower.castShadow = true;
    tower.receiveShadow = true;
    windmillGroup.add(tower);

    // Roof cap
    const capGeometry = new THREE.ConeGeometry(1.8, 2, 8);
    const capMaterial = new THREE.MeshStandardMaterial({ color: 0x8B4513, roughness: 0.7 }); // Brown
    const cap = new THREE.Mesh(capGeometry, capMaterial);
    cap.position.y = 7;
    cap.castShadow = true;
    cap.receiveShadow = true;
    windmillGroup.add(cap);

    // Blades
    const bladeMaterial = new THREE.MeshStandardMaterial({ color: 0xDDDDDD, roughness: 0.6 });
    const bladeGeometry = new THREE.PlaneGeometry(2, 7);

    const bladesPivot = new THREE.Group(); // Group to rotate blades
    bladesPivot.position.set(0, 6, 2.6); // Position in FRONT of tower
    bladesPivot.rotation.x = -Math.PI / 2; // Rotate pivot to face forward
    windmillGroup.add(bladesPivot);

    for (let i = 0; i < 3; i++) { // Three blades
        const blade = new THREE.Mesh(bladeGeometry, bladeMaterial);
        blade.position.set(0, 0, 0); // Position relative to pivot
        blade.rotation.z = (Math.PI * 2 / 3) * i; // Distribute evenly
        blade.castShadow = true;
        blade.receiveShadow = true;
        bladesPivot.add(blade);
    }

    // Store pivot for animation
    windmillGroup.userData.bladesPivot = bladesPivot;

    windmillGroup.position.copy(position);
    scene.add(windmillGroup);
    return windmillGroup;
}

// --- Create the 3D Journey Nodes (Custom Models) ---
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
