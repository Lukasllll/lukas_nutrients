package net.lukasllll.lukas_nutrients.client.graphics;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;


public class CustomTexture extends AbstractTexture {
    //This class is based on the net.minecraft.client.renderer.texture.SimpleTexture-class, though it is even simpler.
    //It just saves a NativeImage given to it in its constructor and uploads it when its load()-function is called, probably
    //by the net.minecraft.client.renderer.texture.TextureManager-class, when it is first registered.;
    private NativeImage image;

    public CustomTexture(NativeImage image) {
        this.image = image;
    }

    //this is an implementation of the abstract load()-function of AbstractTexture. The ResourceManager isn't actually needed at all
    //but the abstract load()-function requires it. The image doesn't need to be loaded by the ResourceManager, because it was already
    //supplied in the constructor. Probably by the NativeImageLoader.
    public void load(ResourceManager manager)  {
        TextureUtil.prepareImage(this.getId(), 0, image.getWidth(), image.getHeight());
        image.upload(0, 0, 0, 0, 0, image.getWidth(), image.getHeight(), false, false, false, true);
    }

}
