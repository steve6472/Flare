package steve6472.flare.util;

import steve6472.core.util.ImagePacker;
import steve6472.flare.FlareConstants;
import steve6472.flare.assets.model.blockbench.ErrorModel;

import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * Created by steve6472
 * Date: 10/20/2024
 * Project: Flare <br>
 */
public final class PackerUtil
{
    public static int PADDING = 1;
    public static boolean DUPLICATE_BORDER = true;

    public static ImagePacker pack(int startingImageSize, Map<String, BufferedImage> images, boolean includeError)
    {
        ImagePacker packer;

        l: while (true)
        {
            packer = new ImagePacker(startingImageSize, startingImageSize, PADDING, DUPLICATE_BORDER);

            for (String imgKey : images.keySet())
            {
                BufferedImage bufferedImage = images.get(imgKey);
                try
                {
                    packer.insertImage(imgKey, bufferedImage);
                } catch (RuntimeException ignored)
                {
                    startingImageSize *= 2;
                    continue l;
                }
            }
            if (includeError)
            {
                try
                {
                    packer.insertImage(FlareConstants.ERROR_TEXTURE.toString(), ErrorModel.IMAGE);
                } catch (RuntimeException ignored)
                {
                    startingImageSize *= 2;
                    continue;
                }
            }
            break;
        }

        return packer;
    }
}
