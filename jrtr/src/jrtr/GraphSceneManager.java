package jrtr;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.vecmath.Matrix4f;

public class GraphSceneManager implements SceneManagerInterface {

    private Node root;
    private Camera camera;
    private Frustum frustum;
    
    public GraphSceneManager() {
        this.camera = new Camera();
        this.frustum = new Frustum();
    }
    
    @Override
    public Camera getCamera() {
        return this.camera;
    }

    @Override
    public Frustum getFrustum() {
        return this.frustum;
    }

    @Override
    public SceneManagerIterator iterator() {
        return new GraphSceneIterator(this);
    }

    @Override
    public Iterator<Light> lightIterator() {
        // TODO Auto-generated method stub
        return null;
    }
    
    public void setRoot(Node root) {
        this.root = root;
    }
    
    public Node getRoot() {
        return this.root;
    }
    
    private class GraphSceneIterator implements SceneManagerIterator {
        
        private Node root;
        private Stack<RenderItem> stack;
        
        public GraphSceneIterator(GraphSceneManager manager) {
            root = manager.getRoot();
            stack = new Stack<RenderItem>();
            init(root);
        }
        
        private void init(Node node) {
            stack.push(new RenderItem(node.getShape(), node.getTransformationMatrix()));
            
            if (node.getChildren()!=null) {
                List<Node> children = node.getChildren();
                
                for (Node child : children) {
                    Matrix4f t = new Matrix4f(node.getTransformationMatrix());
                    t.mul(child.getTransformationMatrix());
                    child.setTransformationMatrix(t);
                    
                    init(child);
                }
            }
        }
        
        @Override
        public boolean hasNext() {
            return !stack.empty();
        }

        @Override
        public RenderItem next() {
            return stack.pop();
        }
        
    }

}
