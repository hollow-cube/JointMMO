package unnamed.mmo.blocks.ore.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import unnamed.mmo.item.ItemComponent;

public record Pickaxe(
        int miningSpeed
) implements ItemComponent {

    public static final Codec<Pickaxe> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("miningSpeed").forGetter(Pickaxe::miningSpeed)
    ).apply(i, Pickaxe::new));

}
