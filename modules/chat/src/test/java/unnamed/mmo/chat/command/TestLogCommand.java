package unnamed.mmo.chat.command;

import net.minestom.server.command.builder.CommandDispatcher;
import net.minestom.server.command.builder.CommandResult;
import net.minestom.server.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import unnamed.mmo.chat.ChatQuery;
import unnamed.mmo.test.TestUtil;
import unnamed.mmo.chat.storage.MockChatStorage;

import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;

public class TestLogCommand {
    private final CommandDispatcher dispatcher = new CommandDispatcher();
    // Fine to use headless player since this command does not affect the world or anything
    private final Player player = TestUtil.headlessPlayer();
    private final MockChatStorage storage = new MockChatStorage();

    @BeforeEach
    public void setup() {
        dispatcher.register(new LogCommand(storage));
    }

    private static Stream<Arguments> commandToQueryMappings() {
        return Stream.of(
                Arguments.of("log in global", ChatQuery.builder().context("global").build()),
                Arguments.of("log context global", ChatQuery.builder().context("global").build()),
                Arguments.of("log in global in local", ChatQuery.builder().context("global", "local").build()),
                Arguments.of("log on build", ChatQuery.builder().serverId("build").build()),
                Arguments.of("log on build in global", ChatQuery.builder().context("global").serverId("build").build())
        );
    }

    @ParameterizedTest
    @MethodSource("commandToQueryMappings")
    public void basicExecution(String command, ChatQuery expectedQuery) {
        CommandResult result = dispatcher.execute(player, command);

        assertThat(result.getType()).isEqualTo(CommandResult.Type.SUCCESS);
        assertThat(storage.queries()).containsExactly(expectedQuery);
    }
}
