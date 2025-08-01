package steve6472.flare;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.io.File;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;
import static org.lwjgl.assimp.Assimp.*;

public class ObjModelLoader
{
    public static ObjModel loadModel(File file, int flags)
    {
        try (AIScene scene = aiImportFile(file.getAbsolutePath(), flags))
        {
            Logger logger = Logger.getLogger(ObjModelLoader.class.getSimpleName());

            logger.info("Loading model " + file.getPath() + "...");

            if (scene == null || scene.mRootNode() == null)
            {
                throw new RuntimeException("Could not load model: " + aiGetErrorString());
            }

            ObjModel model = new ObjModel();

            long startTime = System.nanoTime();

            processNode(scene.mRootNode(), scene, model);

            logger.info("Model loaded in " + ((System.nanoTime() - startTime) / 1e6) + "ms");

            return model;
        }
    }

    private static void processNode(AINode node, AIScene scene, ObjModel model)
    {
        if (node.mMeshes() != null)
        {
            processNodeMeshes(scene, node, model);
        }

        if (node.mChildren() != null)
        {
            PointerBuffer children = node.mChildren();

            for (int i = 0; i < node.mNumChildren(); i++)
            {
                processNode(AINode.create(children.get(i)), scene, model);
            }
        }
    }

    private static void processNodeMeshes(AIScene scene, AINode node, ObjModel model)
    {
        PointerBuffer pMeshes = scene.mMeshes();
        IntBuffer meshIndices = node.mMeshes();

        for (int i = 0; i < meshIndices.capacity(); i++)
        {
            AIMesh mesh = AIMesh.create(pMeshes.get(meshIndices.get(i)));
            processMesh(scene, mesh, model);
        }
    }

    private static void processMesh(AIScene scene, AIMesh mesh, ObjModel model)
    {
        processPositions(mesh, model.positions);
        processTexCoords(mesh, model.texCoords);

        processIndices(mesh, model.indices);
    }

    private static void processPositions(AIMesh mesh, List<Vector3fc> positions)
    {
        AIVector3D.Buffer vertices = requireNonNull(mesh.mVertices());

        for (int i = 0; i < vertices.capacity(); i++)
        {
            AIVector3D position = vertices.get(i);
            positions.add(new Vector3f(position.x(), position.y(), position.z()));
        }
    }

    private static void processTexCoords(AIMesh mesh, List<Vector2fc> texCoords)
    {
        AIVector3D.Buffer aiTexCoords = requireNonNull(mesh.mTextureCoords(0));

        for (int i = 0; i < aiTexCoords.capacity(); i++)
        {
            final AIVector3D coords = aiTexCoords.get(i);
            texCoords.add(new Vector2f(coords.x(), coords.y()));
        }
    }

    private static void processIndices(AIMesh mesh, List<Integer> indices)
    {
        AIFace.Buffer aiFaces = mesh.mFaces();

        for (int i = 0; i < mesh.mNumFaces(); i++)
        {
            AIFace face = aiFaces.get(i);
            IntBuffer pIndices = face.mIndices();
            for (int j = 0; j < face.mNumIndices(); j++)
            {
                indices.add(pIndices.get(j));
            }
        }
    }

    public static class ObjModel
    {
        public final List<Vector3fc> positions;
        public final List<Vector2fc> texCoords;
        public final List<Integer> indices;

        public ObjModel()
        {
            this.positions = new ArrayList<>();
            this.texCoords = new ArrayList<>();
            this.indices = new ArrayList<>();
        }
    }
}