package adudecalledleo.aftbg.text.animate;

import adudecalledleo.aftbg.face.Face;
import adudecalledleo.aftbg.face.FacePool;

public sealed abstract class AnimationCommand {
    /**
     * Draw a new frame.
     */
    public static final class DrawFrame extends AnimationCommand {
        private static final DrawFrame INSTANCE = new DrawFrame();

        private DrawFrame() { }
    }

    /**
     * The end of the textbox has been reached - advance to the next one (if there is a next one).
     */
    public static final class EndOfTextbox extends AnimationCommand {
        private static final EndOfTextbox INSTANCE = new EndOfTextbox();

        private EndOfTextbox() { }
    }

    /**
     * Repeat the last frame N times to add a delay.
     */
    public static final class AddDelay extends AnimationCommand {
        private final int length;

        public AddDelay(int length) {
            this.length = length;
        }

        public int getLength() {
            return length;
        }
    }

    /**
     * Set the text speed - every N frames, the next character will be appended.
     */
    public static final class SetSpeed extends AnimationCommand {
        private final int newSpeed;

        public SetSpeed(int newSpeed) {
            this.newSpeed = newSpeed;
        }

        public int getNewSpeed() {
            return newSpeed;
        }
    }

    // TODO error handling
    /**
     * Set the face image and draw a new frame.
     */
    public static final class SetFace extends AnimationCommand {
        private final String facePath;

        public SetFace(String facePath) {
            this.facePath = facePath;
        }

        public String getFacePath() {
            return facePath;
        }

        public Face getFace(FacePool from) {
            return from.getByPath(facePath);
        }
    }

    public static DrawFrame drawFrame() {
        return DrawFrame.INSTANCE;
    }

    public static AnimationCommand endOfTextbox() {
        return EndOfTextbox.INSTANCE;
    }
}
