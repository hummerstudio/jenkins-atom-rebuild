package atom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jenkinschina.atom.sdk.Json;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * rebuild is a declaration, not code: it puts an entry on the build page and never runs as a step. The old
 * {@code tools/check.sh} asserted that the two JSON files are a declaration the runner can honour; these tests read the
 * same two files and assert the same things.
 */
class RebuildIT {

    private Map<String, Object> task;
    private Map<String, Object> ui;

    @BeforeEach
    void setUp() throws IOException {
        Path module = Path.of(System.getProperty("atom.module", System.getProperty("user.dir")));
        task = Json.parseObject(Files.readString(module.resolve("task.json"), StandardCharsets.UTF_8));
        ui = Json.parseObject(Files.readString(module.resolve("ui.json"), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("task.json declares the atom, an execution target, and the jenkins-1 protocol")
    void taskDeclaration() {
        assertEquals("rebuild", task.get("atomCode"));
        assertTrue(task.get("execution") instanceof Map<?, ?> execution && notBlank(execution.get("target")),
                "task.json needs execution.target (even an atom that only declares a surface)");
        Object protocol = task.get("x-jenkins") instanceof Map<?, ?> jenkins ? jenkins.get("protocol") : "jenkins-1";
        assertEquals("jenkins-1", protocol);
    }

    @Test
    @DisplayName("ui.json has schema 1 and exactly one run surface, the rebuild entry")
    void uiSurface() {
        assertEquals("1", String.valueOf(ui.get("schema")));
        List<?> surfaces = list(ui.get("surfaces"));
        assertEquals(1, surfaces.size(), "expected exactly one surface, got " + surfaces.size());

        Map<?, ?> surface = (Map<?, ?>) surfaces.get(0);
        assertEquals("run", surface.get("where"));
        assertEquals("rebuild", surface.get("id"));
    }

    @Test
    @DisplayName("the surface has exactly one action block that copies the build's parameters")
    void buildAction() {
        Map<?, ?> surface = (Map<?, ?>) list(ui.get("surfaces")).get(0);
        List<Map<?, ?>> actions = new java.util.ArrayList<>();
        for (Object block : list(surface.get("blocks"))) {
            if (block instanceof Map<?, ?> map && "action".equals(map.get("kind"))) {
                actions.add(map);
            }
        }
        assertEquals(1, actions.size(), "expected exactly one action block");

        Map<?, ?> action = (Map<?, ?>) actions.get(0).get("action");
        assertEquals("build", action.get("kind"));
        assertTrue(action.get("params") instanceof Map<?, ?> params && "build".equals(params.get("from")),
                "action must copy the build's parameters (params.from=build)");
    }

    private static List<?> list(Object value) {
        return value instanceof List<?> items ? items : List.of();
    }

    private static boolean notBlank(Object value) {
        return value != null && !String.valueOf(value).isBlank();
    }
}
