package game.logic;

import javafx.scene.canvas.GraphicsContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RenderableTest {

    @Test
    void render_isInvokedWithProvidedGraphicsContext() {
        CountingRenderable renderable = new CountingRenderable();

        renderable.render(null);

        assertEquals(1, renderable.renderCalls);
        assertNull(renderable.lastGraphicsContext);
    }

    private static final class CountingRenderable implements Renderable {
        private int renderCalls;
        private GraphicsContext lastGraphicsContext;

        @Override
        public void render(GraphicsContext gc) {
            renderCalls++;
            lastGraphicsContext = gc;
        }
    }
}
