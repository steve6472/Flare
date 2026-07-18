let format;
let codec;
let addTextureEvent;

// Used to convert to 0..1 base range
const blockSize = 16;
const FACE_NAMES = ['north', 'east', 'south', 'west', 'up', 'down'];

// TODO: automatically set UV Size for texture to its size

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
    },
    onunload() {
        format.delete();
        codec.export_action.delete();
        codec.delete();
        addTextureEvent.delete();
        Project.export_path = '';
    }
});

function setupFormat() {
    codec = setupCodec();
    format = new ModelFormat('flare_model', {

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
 * Compile
 */

function compileVec3(vec3)
{
    return [vec3[0] / blockSize, vec3[1] / blockSize, vec3[2] / blockSize];
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
            // TODO: Codec will have to include default UV, make the error texture show full in this case
            if (!face.texture) {
                return {}
            }

            let texture = Project.textures.filter(t => t.uuid === face.texture)[0];
            let textureKey = extractTextureKey(texture.path);
            let ret;

            // Texture is saved outside of a module or has other error
            if (!textureKey) {
                ret = {
                    uv: face.uv,
                    uuid: texture.uuid
                }
            } else {
                ret = {
                    uv: face.uv,
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
        origin: compileVec3(element.origin),
        rotation: element.rotation,
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

/*
 * Parse
 */

function parseVec3(vec3)
{
    return [vec3[0] * blockSize, vec3[1] * blockSize, vec3[2] * blockSize];
}

function parseCube(element, textureMap) {

    let meta = element.meta;
    let cube = new Cube({
        name: meta.name,
        color: meta.color,
        visibility: meta.visibility,

        from: parseVec3(element.from),
        to: parseVec3(element.to),
        origin: parseVec3(element.origin),
        rotation: element.rotation,
        faces: {}
    }, element.uuid);

    for (let faceName of FACE_NAMES) {
        let face = element.faces[faceName];
        if (face) {
            if (face.uuid) {
                cube.faces[faceName].uv = face.uv;
                cube.faces[faceName].rotation = face.rotation ?? 0;
                cube.faces[faceName].texture = face.uuid;
            } else if (!face.texture) {
                cube.faces[faceName].uv = [0, 0, 1, 1];
                cube.faces[faceName].rotation = 0;
            } else {
                cube.faces[faceName].uv = face.uv;
                cube.faces[faceName].rotation = face.rotation ?? 0;
                cube.faces[faceName].texture = textureMap[face.texture];
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
        color: meta.color,

        origin: parseVec3(element.origin)
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

/*
 * Texture
 */

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
                        name: texture.name
                    });
                } else {
                    model.textures.push({
                        path: texture.path,
                        uuid: texture.uuid,
                        name: texture.name
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
                textures.push(texture);
                texture.loadContentFromPath(texture.path);
                texture.add(false);
                if (data.key)
                    textureMap[data.key] = texture.uuid;
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
        condition: {formats: ['flare_model']},
        click: function () {
            codec.export();
        }
    });
    MenuBar.addAction(codec.export_action, 'file.export');

    return codec;
}