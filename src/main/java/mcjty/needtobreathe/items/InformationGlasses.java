package mcjty.needtobreathe.items;

import mcjty.needtobreathe.NeedToBreathe;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class InformationGlasses extends ItemArmor {

    public InformationGlasses() {
        super(ArmorMaterial.LEATHER, 0, EntityEquipmentSlot.HEAD);
        setRegistryName("informationglasses");
        setUnlocalizedName(NeedToBreathe.MODID + ".informationglasses");
        setCreativeTab(NeedToBreathe.setup.getTab());
    }

    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag advancedToolTip) {
        super.addInformation(itemStack, player, list, advancedToolTip);
        list.add("If you wear these glasses you can see,");
        list.add("where there is clean air");
    }

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return NeedToBreathe.MODID + ":textures/armor/glasses.png";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default) {
        return InformationGlassesModel.getModel(entityLiving, itemStack);
    }


}
