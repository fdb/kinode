package kinode;


import nodebox.node.*;
import org.openkinect.processing.Kinect;

public abstract class KinectNode extends Node {

    public static final int KINECT_IMAGE_WIDTH = 640;
    public static final int KINECT_IMAGE_HEIGHT = 480;

    protected Kinect kinect;
    public final FloatPort pTilt = new FloatPort(this, "tilt", Port.Direction.INPUT, 15f);

    @Override
    public void initialize() {
        kinect = new Kinect(getScene().getApplet());
        kinect.start();
    }

    @Override
    public void execute(Context context, float dt) {
        float tilt = pTilt.get();
        tilt = tilt < 0 ? 0 : (tilt > 25 ? 25 : tilt);
        kinect.tilt(tilt);
    }

    @Override
    public void destroy() {
        kinect.quit();
        kinect = null;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (kinect != null) {
            kinect.quit();
            kinect = null;
        }
    }

}
