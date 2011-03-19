package kinode;


import nodebox.node.*;
import org.openkinect.processing.Kinect;

@Description("Draw the Kinect depth image.")
@Drawable
@Category("Hardware")
public class KinectNode extends Node {

    private Kinect kinect;
    public final FloatPort pTilt = new FloatPort(this, "tilt", Port.Direction.INPUT, 15f);

    @Override
    public void initialize() {
        kinect = new Kinect(getScene().getApplet());
        kinect.start();
        kinect.enableDepth(true);
        kinect.enableRGB(true);
        kinect.processDepthImage(true);
    }

    @Override
    public void execute(Context context, float dt) {
        float tilt = pTilt.get();
        tilt = tilt < 0 ? 0 : (tilt > 25 ? 25 : tilt);
        kinect.tilt(tilt);
    }

    @Override
    public void draw(processing.core.PGraphics g, Context context, float dt) {
        g.image(kinect.getDepthImage(), 0, 0);
    }

    @Override
    public void destroy() {
        kinect.quit();
        kinect = null;
    }

    @Override
    protected void finalize() throws Throwable {
        if (kinect != null) {
            kinect.quit();
            kinect = null;
        }
    }
}
