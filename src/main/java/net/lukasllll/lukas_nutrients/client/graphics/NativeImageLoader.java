package net.lukasllll.lukas_nutrients.client.graphics;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;

public class NativeImageLoader {
    //currently, the only job of this class is to load a NativeImage Object from a given ResourceLocation.
    //The function is almost exactly copied from net.minecraft.client.renderer.texture.SimpleTexture.TextureImage.load()
    //the main difference is, that the TextureImage.load()-function gets its ResourceManager passed to it as a parameter,
    //when it is called by a chain of functions leading back to net.minecraft.client.renderer.texture.TextureManager which
    //gets its ResourceManager when it is initialized in the Minecraft class. This function instead gets its ResourceManager
    //directly from the Minecraft class.
    //In addition, this function doesn't load Metadata, because it isn't needed.
    public static NativeImage loadFromPath(ResourceLocation location) {
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        try {
            Resource resource = manager.getResourceOrThrow(location);
            InputStream inputstream = resource.open();

            NativeImage nativeimage;
            try {
                nativeimage = NativeImage.read(inputstream);
            } catch (Throwable throwable1) {
                if (inputstream != null) {
                    try {
                        inputstream.close();
                    } catch (Throwable throwable) {
                        throwable1.addSuppressed(throwable);
                    }
                }
                throw throwable1;
            }

            if (inputstream != null) {
                inputstream.close();
            }

            return nativeimage;
        } catch (IOException ioexception) {
            return null;
        }
    }
}
