let format;
let codec;
let addTextureEvent;
let onRenderFrameEvent;
let switchProjectEvent;

let renderedObjects = [];

// Used to convert to 0..1 base range
const blockSize = 16;
const FACE_NAMES = ['north', 'east', 'south', 'west', 'up', 'down'];
const FORMAT_ID = 'flare_model';

// TODO: automatically set UV Size for texture to its size

let shapeProperty;
let collisionRadiusProperty;
let collisionHalfHeight;

function isWrongFormat() {
    return Project.format && Project.format.id !== format.id;
}

function setupProperties() {
    shapeProperty = new Property(Locator, 'enum', 'collision_shape', {
        condition: () => !isWrongFormat(),
        default: 'none',
        values: ['none', 'sphere', 'capsule'],
        exposed: true,
        inputs: {
            element_panel: {
                input: {
                    label: 'Shape Type',
                    type: 'select',
                    options: {
                        none: 'None',
                        sphere: 'Sphere',
                        capsule: 'Capsule'
                    }
                }
            }
        }
    });

    collisionRadiusProperty = new Property(Locator, 'number', 'collision_radius', {
        condition: () => !isWrongFormat(),
        default: 1,
        exposed: true,
        inputs: {
            element_panel: {
                input: {
                    label: 'Radius',
                    type: 'num_slider',
                    min: 0.5,
                    step: 0.5
                }
            }
        }
    });

    collisionHalfHeight = new Property(Locator, 'number', 'collision_half_height', {
        condition: () => !isWrongFormat(),
        default: 1,
        exposed: true,
        inputs: {
            element_panel: {
                input: {
                    label: 'Half Height',
                    type: 'num_slider',
                    min: 0.5,
                    step: 0.5
                }
            }
        }
    });
}

function deleteProperties() {
    shapeProperty.delete();
    collisionRadiusProperty.delete();
    collisionHalfHeight.delete();
}

BBPlugin.register('flare_model', {
    title: 'Flare Model',
    author: 'steve6472',
    icon: 'icon',
    description: 'Used by projects using the Flare render engine',
    version: '0.0.1',
    variant: 'both',
    await_loading: true,
    onload() {
        setupFormat();
        addTextureEvent = Blockbench.on(
            'add_texture',
            (texture) => verifyTexture(texture.texture.path, true));
        settings.dialog_save_codec.set(false);
        onRenderFrameEvent = Blockbench.on(
            'render_frame',
            () => render()
        );
        switchProjectEvent = Blockbench.on(
            'select_project',
            () => clearRender()
        );
        setupProperties();

    },
    onunload() {
        format.delete();
        codec.export_action.delete();
        codec.delete();
        addTextureEvent.delete();
        onRenderFrameEvent.delete();
        deleteProperties();

        Project.export_path = '';

        clearRender();
    }
});

function clearRender() {
    renderedObjects.forEach(object => {
        Canvas.scene.remove(object);
    });
    renderedObjects.length = 0;
}

function addGeo(geo, element) {
    geo.translateX(element.position[0]);
    geo.translateY(element.position[1]);
    geo.translateZ(element.position[2]);
    geo.rotateZ(Math.degToRad(element.rotation[0]));
    geo.rotateY(Math.degToRad(element.rotation[1]));
    geo.rotateX(Math.degToRad(element.rotation[2]));
    renderedObjects.push(geo);
    Canvas.scene.add(geo);
}

function render() {
    if (Project.format && Project.format.id !== format.id) {
        return;
    }

    clearRender();

    for (let element of Outliner.root) {
        if (element instanceof Locator) {

            if (element.collision_shape === 'sphere') {
                let geometry = new THREE.SphereGeometry(element.collision_radius / 2);
                let wireframe = new THREE.WireframeGeometry(geometry);
                let geo = new THREE.LineSegments(wireframe);
                addGeo(geo, element);

            } else if (element.collision_shape === 'capsule') {
                let topHemisphere = new THREE.SphereGeometry(element.collision_radius / 2, 8, 3, 0, 2*Math.PI, 0, Math.PI/2);
                let topHemisphereWire = new THREE.WireframeGeometry(topHemisphere);
                let topHemisphereGeo = new THREE.LineSegments(topHemisphereWire);
                topHemisphereGeo.translateY(element.collision_half_height / 2);
                addGeo(topHemisphereGeo, element);

                let bottomHemisphere = new THREE.SphereGeometry(element.collision_radius / 2, 8, 3, 0, 2*Math.PI, Math.PI/2, Math.PI / 2);
                let bottomHemisphereWire = new THREE.WireframeGeometry(bottomHemisphere);
                let bottomHemisphereGeo = new THREE.LineSegments(bottomHemisphereWire);
                bottomHemisphereGeo.translateY(-element.collision_half_height / 2);
                addGeo(bottomHemisphereGeo, element);

                let cylinder = new THREE.CylinderGeometry(element.collision_radius / 2, element.collision_radius / 2, element.collision_half_height);
                let cylinderWire = new THREE.WireframeGeometry(cylinder);
                let cylinderGeo = new THREE.LineSegments(cylinderWire);
                addGeo(cylinderGeo, element);
            }
        }
    }
}
function compileVec3(vec3)
{
    return [vec3[0] / blockSize, vec3[1] / blockSize, vec3[2] / blockSize];
}

function rotationToRadians(vec3)
{
    return [Math.degToRad(vec3[0]), Math.degToRad(vec3[1]), Math.degToRad(vec3[2])]
}

function exportNode(element) {
    return {
        name: element.name,
        color: element.color,
        locked: element.locked,
        visibility: element.visibility,
        selected: element.selected
    }
}

function compileCube(element) {
    function compileFaces(faces) {
        let ret = {};

        function compileFace(face) {
            if (!face.texture) {
                return {}
            }

            let texture = Project.textures.filter(t => t.uuid === face.texture)[0];
            let textureKey = extractTextureKey(texture.path);
            let ret;

            function normalizeUv(uv) {
                return [uv[0] / texture.uv_width, uv[1] / texture.uv_height, uv[2] / texture.uv_width, uv[3] / texture.uv_height];
            }

            // Texture is saved outside of a module or has other error
            if (!textureKey) {
                ret = {
                    uv: normalizeUv(face.uv),
                    uuid: texture.uuid
                }
            } else {
                ret = {
                    uv: normalizeUv(face.uv),
                    texture: textureKey
                }
            }

            if (face.rotation > 0)
                ret.rotation = face.rotation;

            return ret;
        }

        for (let faceName of FACE_NAMES) {
            if (faces[faceName] && faces[faceName].texture != null) {
                ret[faceName] = compileFace(faces[faceName]);
            }
        }

        return ret;
    }

    return {
        type: element.type,
        uuid: element.uuid,
        meta: {
            ...exportNode(element),
            box_uv: element.box_uv,
            autouv: element.autouv
        },
        from: compileVec3(element.from),
        to: compileVec3(element.to),
        inflate: element.inflate,
        origin: compileVec3(element.origin),
        rotation: rotationToRadians(element.rotation),
        faces: compileFaces(element.faces)
    }
}

function compileGroup(element) {
    let ret = {
        type: element.type,
        uuid: element.uuid,
        meta: {
            ...exportNode(element),
            isOpen: element.isOpen
        },
        origin: compileVec3(element.origin),
        rotation: rotationToRadians(element.rotation),
        elements: []
    };

    for (let el of element.children) {
        ret.elements.push(compileElement(el));
    }
    return ret;
}

function compileElement(element) {
    if (!element.export) return void 0;
    if (element instanceof Cube) {
        return compileCube(element);
    } else if (element instanceof Group) {
        return compileGroup(element);
    }
}

function parseVec3(vec3)
{
    return [vec3[0] * blockSize, vec3[1] * blockSize, vec3[2] * blockSize];
}

function rotationToDegrees(vec3)
{
    return [Math.radToDeg(vec3[0]), Math.radToDeg(vec3[1]), Math.radToDeg(vec3[2])]
}

function parseCube(element, textureMap) {

    let meta = element.meta;
    let cube = new Cube({
        name: meta.name,
        color: Number(meta.color),
        visibility: meta.visibility,

        from: parseVec3(element.from),
        to: parseVec3(element.to),
        origin: parseVec3(element.origin),
        rotation: rotationToDegrees(element.rotation),
        faces: {}
    }, element.uuid);

    for (let faceName of FACE_NAMES) {
        let face = element.faces[faceName];
        if (face) {
            if (face.uuid) {
                cube.faces[faceName].uv = [
                    face.uv[0] * textureMap[face.uuid].uv_width,
                    face.uv[1] * textureMap[face.uuid].uv_height,
                    face.uv[2] * textureMap[face.uuid].uv_width,
                    face.uv[3] * textureMap[face.uuid].uv_height
                ];
                cube.faces[faceName].rotation = face.rotation ?? 0;
                cube.faces[faceName].texture = face.uuid;
            } else if (!face.texture) {
                cube.faces[faceName].uv = [0, 0, 1, 1];
                cube.faces[faceName].rotation = 0;
            } else {
                cube.faces[faceName].uv = [
                    face.uv[0] * textureMap[face.texture].uv_width,
                    face.uv[1] * textureMap[face.texture].uv_height,
                    face.uv[2] * textureMap[face.texture].uv_width,
                    face.uv[3] * textureMap[face.texture].uv_height
                ];
                cube.faces[faceName].rotation = face.rotation ?? 0;
                cube.faces[faceName].texture = textureMap[face.texture].uuid;
            }
        } else {
            cube.faces[faceName].texture = null;
        }
    }

    cube.locked = meta.locked;
    cube.selected = meta.selected;
    return cube;
}

function parseGroup(element, textureMap) {
    let meta = element.meta;
    let group = new Group({
        name: meta.name,
        selected: meta.selected,
        visibility: meta.visibility,
        color: Number(meta.color),

        origin: parseVec3(element.origin),
        rotation: rotationToDegrees(element.rotation)
    });

    for (let child of element.elements) {
        group.children.push(parseElement(child, textureMap));
    }

    group.uuid = element.uuid;

    return group;
}

function parseElement(element, textureMap) {
    if (element.type === 'cube') {
        return parseCube(element, textureMap);
    } else if (element.type === 'group') {
        return parseGroup(element, textureMap);
    }
    console.error("Tried to parse an unknown element type")
}
function extractTextureKey(path) {
    if (!verifyTexture(path, false))
        return false;
    let pathArr = path.split(osfs);

    function findModulesRoot() {
        let pattern = ['modules', null, null, 'textures', 'model'];
        if (pathArr.length < pattern)
            return false;

        mainLoop: for (let i = 0; i < pathArr.length - pattern.length; i++) {
            for (let j = 0; j < pattern.length; j++) {
                if (pattern[j] != null && pathArr[i + j] !== pattern[j]) {
                    continue mainLoop;
                }
            }
            return i;
        }
    }

    let rootIndex = findModulesRoot();
    if (!rootIndex)
        return false;

    let filename = pathArr[pathArr.length - 1].split('.')[0];
    let namespace = pathArr[rootIndex + 2];
    let inModulePath = '';
    for (let i = rootIndex + 5; i < pathArr.length - 1; i++) {
        inModulePath += pathArr[i] + '/';
    }
    return namespace + ":" + inModulePath + filename;
}

function verifyTexture(path, showDialogs) {
    if (Project.format.id !== format.id || !path)
        return true;

    if (!path.endsWith('.png')) {
        if (showDialogs) Blockbench.showMessageBox({
            buttons: ['Acknowledge'],
            icon: 'folder_open',
            confirm: 0,
            title: 'Invalid Texture Extension',
            message: 'Textures have to end with .png and be of PNG type'
        });
        return false;
    }

    if (!(path.includes(osfs+'modules'+osfs) && path.includes(osfs+'textures'+osfs))) {
        if (showDialogs) Blockbench.showMessageBox({
            buttons: ['Acknowledge'],
            icon: 'folder_open',
            confirm: 0,
            title: 'Invalid Texture',
            message: 'The imported texture is not contained in a module.\n' +
                'Flare can only load textures inside the texture folder of a module.'
        });
        return false;
    }

    if (!path.includes(osfs+'model'+osfs)) {
        if (showDialogs) Blockbench.showMessageBox({
            buttons: ['Acknowledge'],
            icon: 'folder_open',
            confirm: 0,
            title: 'Invalid Model Texture',
            message: 'Model textures have to be under the textures'+osfs+'model directory.<br><br>' +
                '(Otherwise they will not be included within the model atlas and won\'t be rendered)'
        });
        return false;
    }
    return true;
}

function setupFormat() {
    codec = setupCodec();
    format = new ModelFormat(FORMAT_ID, {

        // FormatOptions
        name: 'Flare Model',
        description: 'Model for the Flare render engine',
        show_on_start_screen: true,
        target: ['Flare', 'Orbiter'],
        codec: codec,

        // FormatFeatures
        box_uv: false,
        optional_box_uv: true,
        single_texture: false,
        per_group_texture: false,
        per_texture_uv_size: true,
        texture_wrap_default: 'limited',
        model_identifier: false,
        parent_model_id: false,
        animated_textures: true,
        bone_rig: true,
        armature_rig: false,
        centered_grid: true,
        block_size: blockSize,
        forward_direction: '-z',
        rotate_cubes: true,
        stretch_cubes: true,
        integer_size: false,
        meshes: true,
        // TODO: try to support splines
        splines: false,
        texture_meshes: false,
        // TODO: support for billboards :)
        billboards: false,
        bounding_boxes: true,
        locators: true,
        pbr: false,
        rotation_limit: false,
        rotation_snap: false,
        euler_order: 'ZYX',
        // I just wanna see what this is lol
        uv_rotation: true,
        java_face_properties: false,
        select_texture_for_particles: false,
        texture_mcmeta: false,
        // What is this
        bone_binding_expression: true,
        // also what is this
        animation_files: false,
        // Allows folder + namespace in texture settings
        texture_folder: false,
        // This somewhy removes the outliner...
        //image_editor: true,
        edit_mode: true,
        paint_mode: true,
        // MC - display options such as slot, ground, fixed...
        display_mode: false,
        animation_mode: true,
        pose_mode: false,
        animation_controllers: true,
        animation_loop_wrapping: true,
        // what
        quaternion_interpolation: true,
        // idk what this does
        per_animator_rotation_interpolation: false,
        // should prob. be false
        box_uv_float_size: false,
        java_cube_shading_properties: false,
        cullfaces: false,
        node_name_regex: false,
        render_sides: 'front'
    });
    codec.format = format;
}

/*
 * Codec
 */

function setupCodec() {
    let codec = new Codec('flare_model', {
        name: 'Flare Model',
        extension: 'fm',
        remember: true,
        support_partial_export: false,
        load_filter: {
            type: 'json',
            extensions: ['fm']
        },

        load(model, file, args = {}) {
            if (args.import_to_current_project) {
                return;
            }

            setupProject(format);

            this.parse(model, file.path, args);

            if (file.path && isApp && this.remember && !file.no_file) {
                Project.export_path = file.path;
                let pathSplit = file.path.split(osfs);
                Project.name = pathSplit[pathSplit.length - 1].split(".")[0];
                addRecentProject({
                    name: Project.name + ".fm",
                    path: Project.export_path,
                    icon: Format.icon
                });
                let project = Project;
                setTimeout(() => {
                    if (Project === project) { // noinspection JSIgnoredPromiseFromCall
                        updateRecentProjectThumbnail();
                    }
                }, 500);
            }

            Settings.updateSettingsInProfiles();
        },
        compile(options = {}) {
            let model = {
                elements: [],
                textures: []
            }

            for (let texture of Project.textures) {
                let key = extractTextureKey(texture.path);
                if (key) {
                    model.textures.push({
                        key: key,
                        path: texture.path,
                        uuid: texture.uuid,
                        name: texture.name,
                        uv_width: texture.uv_width,
                        uv_height: texture.uv_height
                    });
                } else {
                    model.textures.push({
                        path: texture.path,
                        uuid: texture.uuid,
                        name: texture.name,
                        uv_width: texture.uv_width,
                        uv_height: texture.uv_height
                    })
                }
            }

            for (let element of Outliner.root) {
                let compiled = compileElement(element);
                if (compiled) model.elements.push(compiled);
            }

            return compileJSON(model, {
                indentation: "    ",
                final_newline: false
            });
        },
        parse(model, path, args = {}) {
            let elements = [];
            let textures = [];

            let textureMap = {};
            for (let data of model.textures) {
                let texture = new Texture({
                    path: data.path,
                    uuid: data.uuid,
                    name: data.name
                }, data.uuid);
                texture.uv_width = data.uv_width;
                texture.uv_height = data.uv_height;
                textures.push(texture);
                texture.loadContentFromPath(texture.path);
                texture.add(false);
                if (data.key)
                    textureMap[data.key] = texture;
                else if (data.uuid)
                    textureMap[data.uuid] = texture;
            }

            for (let element of model.elements) {
                elements.push(parseElement(element, textureMap));
            }

            function add(element, parent) {
                element.addTo(parent).init();

                if (element instanceof Group) {
                    for (let child of element.children) {
                        add(child, element);
                    }
                }
            }

            for (let element of elements) {
                add(element, 'root');
            }

            return {elements, textures};
        },

        async export(options) {
            if (Object.keys(this.export_options).length) {
                let result = await this.promptExportOptions();
                if (result === null) return;
            }

            Blockbench.export({
                resource_id: 'model',
                type: this.name,
                extensions: [this.extension],
                name: this.fileName(),
                startpath: this.startPath(),
                content: this.compile(options)
            }, (path) => {
                this.afterDownload(path);
                Project.export_path = path;
            });
        }
    });

    codec.export_action = new Action('export_flare_model', {
        name: 'Export Flare Model',
        description: 'Export an fm file',
        icon: 'error',
        category: 'file',
        click: function () {
            codec.export();
        }
    });
    MenuBar.addAction(codec.export_action, 'file.export');

    return codec;
}
