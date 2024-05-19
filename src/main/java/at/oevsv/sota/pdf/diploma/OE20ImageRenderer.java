/*
 * Copyright (C) 2024 David Schwingenschlögl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.oevsv.sota.pdf.diploma;

import at.oevsv.sota.pdf.ImageRenderer;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;

final class OE20ImageRenderer implements ImageRenderer {

    private final Locale locale;
    private final boolean debugLayout;

    public OE20ImageRenderer(Locale locale, boolean debugLayout) {
        this.locale = Objects.requireNonNull(locale);
        this.debugLayout = debugLayout;
    }

    @Override
    public void render(Graphics2D target) throws IOException {
        // Background Size: 3508 x 2480 px (A4, 300 dpi)
        final var bounds = target.getClipBounds();
        try (final InputStream is = this.getClass().getResourceAsStream(backgroundResource())) {
            if (is != null) {
                final var background = ImageIO.read(is);

                if (debugLayout) {
                    final var graphics = background.createGraphics();
                    try {
                        renderGrid(graphics, background.getWidth(), background.getHeight());
                    } finally {
                        graphics.dispose();
                    }
                }

                target.drawImage(background, 0, 0, (int) bounds.getWidth(), (int) bounds.getHeight(), null);
            } else {
                throw new IOException("Could not load background.");
            }
        }
    }

    @Nonnull
    private String backgroundResource() {
        return "/pdf/images/background/oe20sota_" + locale.getLanguage().toLowerCase(Locale.ROOT) + ".png";
    }

    private static void renderGrid(Graphics2D target, int width, int height) {
        for (int loopX = 0; loopX < width; loopX += 100) {
            target.drawLine(loopX, 0, loopX, height);
        }
        for (int loopY = 0; loopY < height; loopY += 100) {
            target.drawLine(0, loopY, width, loopY);
        }
    }
}
