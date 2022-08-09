package unnamed.mmo.item;

import com.google.auto.service.AutoService;
import net.minestom.server.ServerProcess;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unnamed.mmo.server.Facet;

@AutoService(Facet.class)
public class ItemManager implements Facet {
    private static final Logger logger = LoggerFactory.getLogger(ItemManager.class);

    @Override
    public void hook(@NotNull ServerProcess server) {

        // Component handlers
        var eventHandler = server.eventHandler();
        for (ComponentHandler<?> handler : ComponentRegistry.REGISTRY.values()) {

            // Register event nodes
            EventNode<Event> eventNode = handler.eventNode();
            if (eventNode != null) {
                eventHandler.addChild(eventNode);
            }
        }
        logger.info("Loaded {} item components", ComponentRegistry.REGISTRY.size());

    }

}
