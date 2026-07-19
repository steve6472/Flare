
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