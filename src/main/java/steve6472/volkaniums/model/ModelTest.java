package steve6472.volkaniums.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import steve6472.volkaniums.Registries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Volkaniums <br>
 */
public class ModelTest
{
    private static final String PATH = "C:\\Users\\Steve\\Desktop\\model.bbmodel";

    public static void main(String[] args) throws FileNotFoundException
    {
        System.setProperty("joml.format", "false");
        Registries.createContents();

        File file = new File(PATH);

        BufferedReader reader = new BufferedReader(new FileReader(file));
        JsonElement jsonElement = JsonParser.parseReader(reader);
//        System.out.println(jsonElement);

        DataResult<Pair<LoadedModel, JsonElement>> decode = LoadedModel.CODEC.decode(JsonOps.INSTANCE, jsonElement);
        System.out.println(decode.getOrThrow().getFirst());
    }
}
