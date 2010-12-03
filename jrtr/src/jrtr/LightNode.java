package jrtr;

public class LightNode extends Leaf {

    private Light light; 
    
    public LightNode() {
        super();
    }
    
    @Override
    public Shape getShape() {
        return null;
    }

    @Override
    public void setShape(Shape shape) {

    }
    
    public Light getLight() {
        return light;
    }
    
    public void setLight(Light light) {
        this.light = light;
    }

}
