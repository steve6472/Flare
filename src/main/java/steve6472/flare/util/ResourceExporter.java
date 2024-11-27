package steve6472.flare.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by steve6472
 * Date: 11/24/2024
 * Project: Flare <br>
 */
public final class ResourceExporter
{
    public static void exportFile(String resourcePath, String destinationPath)
    {
        // Get the file as a resource stream
        try (InputStream inputStream = ResourceExporter.class.getResourceAsStream(resourcePath); FileOutputStream outputStream = new FileOutputStream(destinationPath))
        {
            if (inputStream == null)
            {
                throw new FileNotFoundException("Resource not found: " + resourcePath);
            }

            // Create buffer and transfer data
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1)
            {
                outputStream.write(buffer, 0, bytesRead);
            }

            System.out.println("File exported to: " + destinationPath);

        } catch (IOException e)
        {
            e.printStackTrace();
            System.err.println("Failed to export file: " + e.getMessage());
        }
    }
}
