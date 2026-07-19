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