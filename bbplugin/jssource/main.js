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

//#include properties.js

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

//#include rendering.js
//#include compile.js
//#include parse.js
//#include textures.js

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