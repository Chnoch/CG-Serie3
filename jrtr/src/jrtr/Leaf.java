package jrtr;

import java.util.List;

import javax.vecmath.Matrix4f;

public abstract class Leaf implements Node {

    protected Matrix4f transformationMatrix;
    
    @Override
    public List<Node> getChildren() {
        return null;
    }
    
    public void addChild(Node child) {
    }

    @Override
    public Matrix4f getTransformationMatrix() {
        return this.transformationMatrix;
    }
    
    @Override
    public void setTransformationMatrix(Matrix4f t) {
        this.transformationMatrix = t;
    }
        
}
