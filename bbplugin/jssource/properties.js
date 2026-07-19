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