/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.modifier;

import com.google.common.collect.Lists;
import com.google.gson.*;
import hellfirepvp.modularmachinery.common.machine.MachineLoader;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import hellfirepvp.modularmachinery.common.util.IBlockStateDescriptor;
import hellfirepvp.modularmachinery.common.util.nbt.NBTJsonDeserializer;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import java.lang.reflect.Type;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ModifierReplacement
 * Created by HellFirePvP
 * Date: 30.03.2018 / 16:35
 */
public class SingleBlockModifierReplacement extends AbstractModifierReplacement {

    private final BlockArray.BlockInformation info;
    private BlockPos pos = null;

    public SingleBlockModifierReplacement(BlockArray.BlockInformation info, List<RecipeModifier> modifier, String description) {
        super(modifier, description, info.getDescriptiveStack(0));
        this.info = info;
    }

    public BlockArray.BlockInformation getBlockInformation() {
        return info;
    }

    public BlockPos getPos() {
        return pos;
    }

    public SingleBlockModifierReplacement setPos(final BlockPos pos) {
        this.pos = pos;
        return this;
    }

    public static class Deserializer implements JsonDeserializer<SingleBlockModifierReplacement> {

        @Override
        public SingleBlockModifierReplacement deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            NBTTagCompound match = null;
            JsonObject part = json.getAsJsonObject();
            if (part.has("nbt")) {
                JsonElement je = part.get("nbt");
                if (!je.isJsonObject()) {
                    throw new JsonParseException("The ComponentType 'nbt' expects a json compound that defines the NBT tag to match the tileentity's nbt against!");
                }
                String jsonStr = je.toString();
                try {
                    match = NBTJsonDeserializer.deserialize(jsonStr);
                } catch (NBTException exc) {
                    throw new JsonParseException("Error trying to parse NBTTag! Rethrowing exception...", exc);
                }
            }
            if (!part.has("elements")) {
                throw new JsonParseException("Modifier-tag contained no element!");
            }
            BlockArray.BlockInformation blockInfo;
            JsonElement partElement = part.get("elements");
            if (partElement.isJsonPrimitive() && partElement.getAsJsonPrimitive().isString()) {
                String strDesc = partElement.getAsString();
                blockInfo = MachineLoader.VARIABLE_CONTEXT.get(strDesc);
                if (blockInfo == null) {
                    blockInfo = new BlockArray.BlockInformation(Lists.newArrayList(BlockArray.BlockInformation.getDescriptor(partElement.getAsString())));
                } else {
                    blockInfo = blockInfo.copy(); //Avoid NBT-definitions bleed into variable context
                }
                if (match != null) {
                    blockInfo.setMatchingTag(match);
                }
            } else if (partElement.isJsonArray()) {
                JsonArray elementArray = partElement.getAsJsonArray();
                List<IBlockStateDescriptor> descriptors = Lists.newArrayList();
                for (int xx = 0; xx < elementArray.size(); xx++) {
                    JsonElement p = elementArray.get(xx);
                    if (!p.isJsonPrimitive() || !p.getAsJsonPrimitive().isString()) {
                        throw new JsonParseException("Part elements of 'elements' have to be blockstate descriptions!");
                    }
                    String prim = p.getAsString();
                    BlockArray.BlockInformation descr = MachineLoader.VARIABLE_CONTEXT.get(prim);
                    if (descr != null) {
                        descriptors.addAll(descr.copy().matchingStates);
                    } else {
                        descriptors.add(BlockArray.BlockInformation.getDescriptor(prim));
                    }
                }
                if (descriptors.isEmpty()) {
                    throw new JsonParseException("'elements' array didn't contain any blockstate descriptors!");
                }
                blockInfo = new BlockArray.BlockInformation(descriptors);
                if (match != null) {
                    blockInfo.setMatchingTag(match);
                }
            } else {
                throw new JsonParseException("'elements' has to either be a blockstate description, variable or array of blockstate descriptions!");
            }

            if (!part.has("modifier") && !part.has("modifiers")) {
                throw new JsonParseException("'modifiers' getTag not found!");
            }
            JsonElement elementModifiers = part.has("modifier") ? part.get("modifier") : part.get("modifiers");
            List<RecipeModifier> modifiers = Lists.newArrayList();
            if (elementModifiers.isJsonObject()) {
                modifiers.add(context.deserialize(elementModifiers, RecipeModifier.class));

            } else if (elementModifiers.isJsonArray()) {
                JsonArray modifierArray = elementModifiers.getAsJsonArray();
                for (JsonElement modifierElement : modifierArray) {
                    if (!modifierElement.isJsonObject()) {
                        throw new JsonParseException("'modifiers' array needs to consist of json objects, each a modifier object!");
                    }
                    modifiers.add(context.deserialize(modifierElement, RecipeModifier.class));
                }
            } else {
                throw new JsonParseException("'modifiers' getTag needs to be either a single modifier object or an array of modifier objects!");
            }


            String description = part.has("description") ? part.getAsJsonPrimitive("description").getAsString() : "";
            return new SingleBlockModifierReplacement(blockInfo, modifiers, description);
        }

    }

}
