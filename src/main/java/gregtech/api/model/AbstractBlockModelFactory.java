package gregtech.api.model;

import gregtech.api.GT_Values;
import gregtech.api.util.FileUtility;
import net.minecraft.block.Block;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractBlockModelFactory implements ResourcePackHook.IResourcePackFileHook {

    private final ResourceLocation sampleResourceLocation;
    private final String blockNamePrefix;

    public AbstractBlockModelFactory(String sampleName, String blockNamePrefix) {
        this.sampleResourceLocation = sampleName == null ? null : new ResourceLocation(GT_Values.MODID, "blockstates/autogenerated/" + sampleName + ".json");
        this.blockNamePrefix = blockNamePrefix;
    }

    private String blockStateSample;

    protected abstract String fillSample(Block block, String blockStateSample);

    @Override
    public void onResourceManagerReload(SimpleReloadableResourceManager resourceManager) {
        if(sampleResourceLocation != null) {
            try {
                blockStateSample = FileUtility.readInputStream(resourceManager.getResource(sampleResourceLocation).getInputStream());
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }
    }

    @Override
    public boolean resourceExists(ResourceLocation location) {
        return location.getResourceDomain().equals(GT_Values.MODID) && location.getResourcePath().startsWith("blockstates/" + blockNamePrefix);
    }

    @Override
    public InputStream getInputStream(ResourceLocation location) throws IOException {
        String resourcePath = location.getResourcePath(); // blockstates/compressed_block_01.json
        resourcePath = resourcePath.substring(0, resourcePath.length() - 5); //remove .json
        resourcePath = resourcePath.substring(12); //remove blockstates/
        if(resourcePath.startsWith(blockNamePrefix)) {
            Block block = Block.REGISTRY.getObject(new ResourceLocation(location.getResourceDomain(), resourcePath));
            if(block != null) {
                return FileUtility.writeInputStream(fillSample(block, blockStateSample));
            }
            throw new IllegalArgumentException("Block not found: " + resourcePath);
        }
        throw new FileNotFoundException(location.toString());
    }

}
