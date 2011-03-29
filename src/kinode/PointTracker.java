package kinode;

import nodebox.node.*;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

@Category("Hardware")
@Description("Tracks an average point inside of the depth threshold.")
@Drawable
@EvaluatedNode
public class PointTracker extends KinectNode {

    public final IntPort pThreshold = new IntPort(this, "threshold", Port.Direction.INPUT, 745);
    public final FloatPort pSmoothness = new FloatPort(this, "smoothness", Port.Direction.INPUT, 0f);

    public final BooleanPort pShowDepthImage = new BooleanPort(this, "showDepthImage", Port.Direction.INPUT, true);
    public final BooleanPort pShowPoint = new BooleanPort(this, "showPoint", Port.Direction.INPUT, true);
    public final FloatPort pX = new FloatPort(this, "x", Port.Direction.OUTPUT, -1);
    public final FloatPort pY = new FloatPort(this, "y", Port.Direction.OUTPUT, -1);

    private float smoothX = -1;
    private float smoothY = -1;

    private PImage coloredImage;
    private int[] depthValues;

    @Override
    public void initialize() {
        super.initialize();
        kinect.enableDepth(true);
        // TODO: Should be configurable.
        kinect.processDepthImage(true);
    }

    @Override
    public void execute(Context context, float dt) {
        super.execute(context, dt);
        depthValues = kinect.getRawDepth();
        trackDepthValues(depthValues);
    }

    private void trackDepthValues(int[] depthValues) {
        long sumX = 0;
        long sumY = 0;
        int count = 0;
        int threshold = pThreshold.get();
        for (int x = 0; x < KINECT_IMAGE_WIDTH; x++) {
            for (int y = 0; y < KINECT_IMAGE_HEIGHT; y++) {
                int offset = KINECT_IMAGE_WIDTH - x - 1 + y * KINECT_IMAGE_WIDTH;
                int rawDepth = depthValues[offset];
                if (rawDepth < threshold) {
                    sumX += x;
                    sumY += y;
                    count++;
                }
            }
        }

        if (count > 0) {
            float x = sumX / (float) count;
            float y = sumY / (float) count;
            if (pSmoothness.get() > 0) {
                float amount = PApplet.constrain(pSmoothness.get() / 100f, 0, 0.99f);
                x = smoothX = PApplet.lerp(x, smoothX, amount);
                y = smoothY = PApplet.lerp(y, smoothX, amount);
            }
            pX.set(x);
            pY.set(y);
        } else {
            pX.set(-1);
            pY.set(-1);
        }
    }

    @Override
    public void draw(processing.core.PGraphics g, Context context, float dt) {
        if (pShowDepthImage.get()) {
            PImage depthImage = kinect.getDepthImage();
            PApplet applet = context.getApplet();
            colorPixelsByThreshold(applet, depthImage, depthValues, pThreshold.get());
            g.image(coloredImage, 0, 0);
        }
        if (pShowPoint.get()) {
            g.fill(100, 250, 50, 200);
            g.noStroke();
            g.ellipse(pX.get(), pY.get(), 20, 20);
        }
    }

    private void colorPixelsByThreshold(PApplet applet, PImage image, int[] depthValues, int threshold) {
        if (coloredImage == null) {
            coloredImage = applet.createImage(KINECT_IMAGE_WIDTH, KINECT_IMAGE_HEIGHT, PConstants.RGB);
        }
        int thresholdColor = applet.color(150, 50, 50);
        coloredImage.loadPixels();
        for (int x = 0; x < KINECT_IMAGE_WIDTH; x++) {
            for (int y = 0; y < KINECT_IMAGE_HEIGHT; y++) {
                // mirroring image
                int offset = KINECT_IMAGE_WIDTH - x - 1 + y * KINECT_IMAGE_WIDTH;
                // Raw depth
                int rawDepth = depthValues[offset];

                int pix = x + y * KINECT_IMAGE_WIDTH;
                if (rawDepth < threshold) {
                    // A red color instead
                    coloredImage.pixels[pix] = thresholdColor;
                } else {
                    coloredImage.pixels[pix] = image.pixels[offset];
                }
            }
        }
        coloredImage.updatePixels();
    }


}
