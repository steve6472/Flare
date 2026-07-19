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