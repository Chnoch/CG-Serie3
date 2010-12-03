package jrtr;

import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Matrix4f;

public abstract class Group implements Node {

    protected List<Node> children;
    
    public Group() {
        super();
        children = new LinkedList<Node>();
    }
    
    @Override
    public List<Node> getChildren() {
        return this.children;
    }

    @Override
    public Shape getShape() {
        return null;
    }

    @Override
    public void setShape(Shape shape) {
    }
    
    public Light getLight() {
        return null;
    }
    
    public void setLight(Light light){
    }

    @Override
    public Matrix4f getTransformationMatrix() {
        return null;
    }
    
    public void addChild(Node child) {
        this.children.add(child);
    }
    
    public void removeChild(Node child) {
        this.children.remove(child);
    }

}
