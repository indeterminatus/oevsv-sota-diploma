/*
 * Copyright (C) 2023 David Schwingenschlögl
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

package at.oevsv.sota.pdf;

import at.oevsv.sota.data.api.Candidate;
import at.oevsv.sota.data.domain.Summit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Locale;

/**
 * Responsible for rendering all image-related things; this will directly draw into a Graphics2D buffer to allow fine control
 * over placement/blending/...
 *
 * @author schwingenschloegl
 */
final class ImageRenderer {

    /**
     * Specifying the placement of the state graphics, to be able to quickly prototype the desired behaviour.
     */
    public enum StateLayoutMode {

        /**
         * Placing the elements in a straight line. This was the first draft.
         */
        STRAIGHT {
            @Override
            public int heightFor(int currentIndex, int minimumHeight, int maximumHeight) {
                return minimumHeight + (maximumHeight - minimumHeight) / 2;
            }
        },

        /**
         * Placing the elements on a slight curve, like they are hanging on a loose string.
         */
        CURVED {
            @Override
            public int heightFor(int currentIndex, int minimumHeight, int maximumHeight) {
                final double normalizedIndex = currentIndex / (Summit.State.values().length - 1.0);
                final int amplitude = maximumHeight - minimumHeight;
                final double offsetY = Math.sin(normalizedIndex * Math.PI) * amplitude;

                return minimumHeight + (int) offsetY;
            }
        };

        /**
         * Calculate the height of an element at position <code>currentIndex</code>.
         *
         * @param currentIndex  integer between 0 and 9 (number of Austrian regions)
         * @param minimumHeight positive integer, height of the uppermost element
         * @param maximumHeight positive integer, height of the lowest element
         * @return height in pixel <code>y</code>, such that <code>minimumHeight <= y <= maximumHeight</code>
         */
        public abstract int heightFor(int currentIndex, int minimumHeight, int maximumHeight);
    }

    private final Candidate candidate;
    private final boolean debugLayout;
    private final StateLayoutMode stateLayoutMode;

    ImageRenderer(Candidate candidate, boolean debugLayout) {
        this.candidate = candidate;
        this.debugLayout = debugLayout;
        stateLayoutMode = StateLayoutMode.CURVED;
    }

    public void render(Graphics2D target) throws IOException {
        renderBackground(target, target.getClipBounds());
    }

    private void renderBackground(Graphics2D target, Rectangle bounds) throws IOException {
        // Background Size: 3508 x 2480 px (A4, 300 dpi)
        try (final InputStream is = this.getClass().getResourceAsStream(backgroundResource())) {
            if (is != null) {
                final var background = ImageIO.read(is);
                final var graphics = background.createGraphics();
                try {
                    final int stateWidth = 320 + 15; // 320px is the actual image size; add a little extra spacing
                    final int center = background.getWidth() / 2;
                    final int stateStartX = center - (int) ((9 * stateWidth) / 2.0);
                    final int stateStartY = background.getHeight() - 470;
                    renderStates(graphics, stateStartX, stateStartY - 500, stateStartY - 100, stateWidth, candidate);

                    renderResourceAt(graphics, "/pdf/images/banner/" + candidate.rank().toString().toLowerCase(Locale.ROOT) + ".png", center - 864 / 2, 675);

                    if (debugLayout) {
                        renderGrid(graphics, background.getWidth(), background.getHeight(), 100, 100);
                    }
                } finally {
                    graphics.dispose();
                }

                target.drawImage(background, 0, 0, (int) bounds.getWidth(), (int) bounds.getHeight(), null);
            } else {
                throw new IOException("Could not load background.");
            }
        }
    }

    @Nonnull
    private String backgroundResource() {
        return "/pdf/images/background/" + candidate.category().toString().toLowerCase(Locale.ROOT) + ".png";
    }

    private static void renderGrid(Graphics2D target, int width, int height, int gridSizeX, int gridSizeY) {
        for (int loopX = 0; loopX < width; loopX += gridSizeX) {
            target.drawLine(loopX, 0, loopX, height);
        }
        for (int loopY = 0; loopY < height; loopY += gridSizeY) {
            target.drawLine(0, loopY, width, loopY);
        }
    }

    private void renderStates(Graphics2D target, int startX, int minY, int maxY, int width, Candidate candidate) throws IOException {
        for (int loopIdx = 1; loopIdx <= 9; ++loopIdx) {
            final int y = stateLayoutMode.heightFor(loopIdx - 1, minY, maxY);

            if (isStateSatisfied(candidate, Summit.State.stateForOrdinal(loopIdx))) {
                renderResourceAt(target, "/pdf/images/state/positive/oe" + loopIdx + ".png", startX + (loopIdx - 1) * width, y);
            } else {
                renderResourceAt(target, "/pdf/images/state/negative/oe" + loopIdx + ".png", startX + (loopIdx - 1) * width, y);
            }
        }
    }

    private static boolean isStateSatisfied(Candidate candidate, @Nullable Summit.State state) {
        if (state != null) {
            final var count = candidate.activations().getOrDefault(state, 0L);
            return count > 0L;
        } else {
            return false;
        }
    }

    private void renderResourceAt(Graphics2D target, String resourceName, int startX, int startY) throws IOException {
        try (final InputStream is = this.getClass().getResourceAsStream(resourceName)) {
            if (is != null) {
                final var image = ImageIO.read(is);
                target.drawImage(image, startX, startY, image.getWidth(), image.getHeight(), null);
            } else {
                throw new IOException(MessageFormat.format("Could not load {0}.", resourceName));
            }
        }
    }
}
