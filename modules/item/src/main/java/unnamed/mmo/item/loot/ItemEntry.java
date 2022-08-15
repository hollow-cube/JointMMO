package unnamed.mmo.item.loot;

import com.google.auto.service.AutoService;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import unnamed.mmo.item.Item;
import unnamed.mmo.loot.LootContext;
import unnamed.mmo.loot.LootEntry;
import unnamed.mmo.util.ExtraCodecs;

import java.util.List;

public record ItemEntry(
        @NotNull NamespaceID item
) implements LootEntry<Item> {
    //todo this needs to be improved a lot in the future. Weights, item states, amount, etc, etc.

    public static final Codec<ItemEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
            ExtraCodecs.NAMESPACE_ID.fieldOf("item").forGetter(ItemEntry::item)
    ).apply(i, ItemEntry::new));

    @Override
    public @NotNull List<@NotNull Option<Item>> generate(@NotNull LootContext context) {
        Item item = Item.fromNamespaceId(this.item);
        Check.notNull(item, "Unknown item: " + this.item);
        return List.of(new Option<>(List.of(item), 1));
    }

    @AutoService(LootEntry.Factory.class)
    public static class Factory extends LootEntry.Factory {
        public Factory() {
            super(
                    NamespaceID.from("unnamed:item"),
                    ItemEntry.class,
                    ItemEntry.CODEC
            );
        }
    }
}
