package com.leaderboard.util;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * A utility class to enable resizing and dragging of borderless (StageStyle.TRANSPARENT) stages.
 */
public class ResizeHelper {

    public static void addResizeListener(Stage stage) {
        addResizeListener(stage, 150, 150, Double.MAX_VALUE, Double.MAX_VALUE);
    }

    public static void addResizeListener(Stage stage, double minWidth, double minHeight, double maxWidth, double maxHeight) {
        ResizeListener resizeListener = new ResizeListener(stage, minWidth, minHeight, maxWidth, maxHeight);
        Scene scene = stage.getScene();
        scene.addEventHandler(MouseEvent.MOUSE_MOVED, resizeListener);
        scene.addEventHandler(MouseEvent.MOUSE_PRESSED, resizeListener);
        scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, resizeListener);
        scene.addEventHandler(MouseEvent.MOUSE_EXITED, resizeListener);
        scene.addEventHandler(MouseEvent.MOUSE_RELEASED, resizeListener);
    }

    private static class ResizeListener implements EventHandler<MouseEvent> {
        private final Stage stage;
        private final double minWidth;
        private final double minHeight;
        private final double maxWidth;
        private final double maxHeight;

        private Cursor cursorEvent = Cursor.DEFAULT;
        private final int border = 8;

        private double startX = 0;
        private double startY = 0;
        private double startScreenX = 0;
        private double startScreenY = 0;
        private double xOffset = 0;
        private double yOffset = 0;
        private boolean isResizing = false;

        public ResizeListener(Stage stage, double minWidth, double minHeight, double maxWidth, double maxHeight) {
            this.stage = stage;
            this.minWidth = minWidth;
            this.minHeight = minHeight;
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
        }

        @Override
        public void handle(MouseEvent mouseEvent) {
            EventType<? extends MouseEvent> mouseEventType = mouseEvent.getEventType();
            Scene scene = stage.getScene();

            double mouseEventX = mouseEvent.getX();
            double mouseEventY = mouseEvent.getY();
            double sceneWidth = scene.getWidth();
            double sceneHeight = scene.getHeight();

            if (MouseEvent.MOUSE_MOVED.equals(mouseEventType)) {
                if (mouseEventX < border && mouseEventY < border) {
                    cursorEvent = Cursor.NW_RESIZE;
                } else if (mouseEventX < border && mouseEventY > sceneHeight - border) {
                    cursorEvent = Cursor.SW_RESIZE;
                } else if (mouseEventX > sceneWidth - border && mouseEventY < border) {
                    cursorEvent = Cursor.NE_RESIZE;
                } else if (mouseEventX > sceneWidth - border && mouseEventY > sceneHeight - border) {
                    cursorEvent = Cursor.SE_RESIZE;
                } else if (mouseEventX < border) {
                    cursorEvent = Cursor.W_RESIZE;
                } else if (mouseEventX > sceneWidth - border) {
                    cursorEvent = Cursor.E_RESIZE;
                } else if (mouseEventY < border) {
                    cursorEvent = Cursor.N_RESIZE;
                } else if (mouseEventY > sceneHeight - border) {
                    cursorEvent = Cursor.S_RESIZE;
                } else {
                    cursorEvent = Cursor.DEFAULT;
                }
                scene.setCursor(cursorEvent);
            } else if (MouseEvent.MOUSE_EXITED.equals(mouseEventType) || MouseEvent.MOUSE_RELEASED.equals(mouseEventType)) {
                if (MouseEvent.MOUSE_RELEASED.equals(mouseEventType)) {
                    isResizing = false;
                }
                scene.setCursor(Cursor.DEFAULT);
            } else if (MouseEvent.MOUSE_PRESSED.equals(mouseEventType)) {
                startX = stage.getWidth();
                startY = stage.getHeight();
                startScreenX = mouseEvent.getScreenX();
                startScreenY = mouseEvent.getScreenY();
                xOffset = mouseEvent.getSceneX();
                yOffset = mouseEvent.getSceneY();
                isResizing = !Cursor.DEFAULT.equals(cursorEvent);
            } else if (MouseEvent.MOUSE_DRAGGED.equals(mouseEventType)) {
                if (isResizing) {
                    // Handle vertical resize
                    if (!Cursor.W_RESIZE.equals(cursorEvent) && !Cursor.E_RESIZE.equals(cursorEvent)) {
                        double deltaY = mouseEvent.getScreenY() - startScreenY;
                        if (Cursor.NW_RESIZE.equals(cursorEvent) || Cursor.N_RESIZE.equals(cursorEvent) || Cursor.NE_RESIZE.equals(cursorEvent)) {
                            double newHeight = startY - deltaY;
                            if (newHeight >= minHeight && newHeight <= maxHeight) {
                                stage.setHeight(newHeight);
                                stage.setY(mouseEvent.getScreenY() - mouseEvent.getY());
                            }
                        } else {
                            double newHeight = startY + deltaY;
                            if (newHeight >= minHeight && newHeight <= maxHeight) {
                                stage.setHeight(newHeight);
                            }
                        }
                    }

                    // Handle horizontal resize
                    if (!Cursor.N_RESIZE.equals(cursorEvent) && !Cursor.S_RESIZE.equals(cursorEvent)) {
                        double deltaX = mouseEvent.getScreenX() - startScreenX;
                        if (Cursor.NW_RESIZE.equals(cursorEvent) || Cursor.W_RESIZE.equals(cursorEvent) || Cursor.SW_RESIZE.equals(cursorEvent)) {
                            double newWidth = startX - deltaX;
                            if (newWidth >= minWidth && newWidth <= maxWidth) {
                                stage.setWidth(newWidth);
                                stage.setX(mouseEvent.getScreenX() - mouseEvent.getX());
                            }
                        } else {
                            double newWidth = startX + deltaX;
                            if (newWidth >= minWidth && newWidth <= maxWidth) {
                                stage.setWidth(newWidth);
                            }
                        }
                    }
                } else {
                    // Handle move/drag of the window
                    stage.setX(mouseEvent.getScreenX() - xOffset);
                    stage.setY(mouseEvent.getScreenY() - yOffset);
                }
            }
        }
    }
}
