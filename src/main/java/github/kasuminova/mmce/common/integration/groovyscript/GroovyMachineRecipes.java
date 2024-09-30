package github.kasuminova.mmce.common.integration.groovyscript;

import com.cleanroommc.groovyscript.api.IScriptReloadable;
import com.cleanroommc.groovyscript.helper.Alias;
import com.cleanroommc.groovyscript.registry.NamedRegistry;
import com.google.common.base.CaseFormat;
import net.minecraft.util.ResourceLocation;

public class GroovyMachineRecipes extends NamedRegistry implements IScriptReloadable {

    private final ResourceLocation name;

    public GroovyMachineRecipes(ResourceLocation name) {
        super(Alias.generateOf(name.getPath(), CaseFormat.LOWER_UNDERSCORE));
        this.name = name;
    }

    @Override
    public void onReload() {
    }

    @Override
    public void afterScriptLoad() {
    }

    public GroovyRecipe recipeBuilder(String name) {
        return new GroovyRecipe(this.name);
    }
}
