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