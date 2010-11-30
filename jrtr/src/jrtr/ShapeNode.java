package jrtr;

import javax.vecmath.Matrix4f;

public class ShapeNode extends Leaf {

    private Shape shape;
    
    public ShapeNode() {
        super();
    }
    
    public void setShape(Shape shape) {
        this.shape = shape;
    }
    
    public Shape getShape() {
        return this.shape;
    }
}
