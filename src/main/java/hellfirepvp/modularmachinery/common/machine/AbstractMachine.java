package hellfirepvp.modularmachinery.common.machine;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.data.Config;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Objects;

public abstract class AbstractMachine {
    @Nonnull
    protected final ResourceLocation registryName;

    protected String localizedName = "";
    protected String prefix = "";
    protected int definedColor = Config.machineColor;

    protected int maxParallelism = Config.maxMachineParallelism;
    protected int internalParallelism = 0;

    protected int maxThreads = Config.defaultFactoryMaxThread;
    protected boolean requiresBlueprint = false;
    protected boolean parallelizable = Config.machineParallelizeEnabledByDefault;
    protected boolean hasFactory = Config.enableFactoryControllerByDefault;
    protected boolean factoryOnly = false;
    protected RecipeFailureActions failureAction = RecipeFailureActions.getDefaultAction();

    public AbstractMachine(String registryName) {
        this.registryName = new ResourceLocation(ModularMachinery.MODID, registryName);
    }

    public RecipeFailureActions getFailureAction() {
        return failureAction;
    }

    public void setFailureAction(RecipeFailureActions failureAction) {
        this.failureAction = failureAction;
    }

    public void setRequiresBlueprint(boolean requiresBlueprint) {
        this.requiresBlueprint = requiresBlueprint;
    }

    public boolean isRequiresBlueprint() {
        return requiresBlueprint;
    }

    @SideOnly(Side.CLIENT)
    public String getPrefix() {
        String localizationKey = registryName.getNamespace() + "." + registryName.getPath() + ".prefix";
        return I18n.hasKey(localizationKey) ? I18n.format(localizationKey) :
                prefix != null ? prefix : localizationKey;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    @SideOnly(Side.CLIENT)
    public String getLocalizedName() {
        String localizationKey = localizedName != null && !localizedName.isEmpty() ?
                localizedName :
                registryName.getNamespace() + ".multiblock." + registryName.getPath();
        return I18n.format(localizationKey);
    }

    public void setLocalizedName(String localizedName) {
        this.localizedName = localizedName;
    }

    public int getMachineColor() {
        return definedColor;
    }

    public void setDefinedColor(int definedColor) {
        this.definedColor = definedColor;
    }

    @Nonnull
    public ResourceLocation getRegistryName() {
        return registryName;
    }

    public boolean isParallelizable() {
        return parallelizable;
    }

    public void setParallelizable(boolean parallelizable) {
        this.parallelizable = parallelizable;
    }

    public int getMaxParallelism() {
        return maxParallelism;
    }

    public void setMaxParallelism(int maxParallelism) {
        this.maxParallelism = maxParallelism;
    }

    public int getInternalParallelism() {
        return internalParallelism;
    }

    public void setInternalParallelism(final int internalParallelism) {
        if (maxParallelism < internalParallelism) {
            this.maxParallelism = internalParallelism;
        }
        this.internalParallelism = internalParallelism;
    }

    public boolean isHasFactory() {
        return hasFactory;
    }

    public void setHasFactory(boolean hasFactory) {
        this.hasFactory = hasFactory;
    }

    public boolean isFactoryOnly() {
        return factoryOnly;
    }

    public void setFactoryOnly(boolean factoryOnly) {
        this.factoryOnly = factoryOnly;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof final AbstractMachine machine)) return false;
        return machine.registryName.toString().equals(registryName.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(registryName);
    }
}
