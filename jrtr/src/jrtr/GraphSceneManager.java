

package jrtr;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

public class GraphSceneManager implements SceneManagerInterface {

    private Node root;
    private Camera camera;
    private Frustum frustum;
    private LinkedList<Light> lights;

    public GraphSceneManager() {
        this.camera = new Camera();
        this.frustum = new Frustum();
        lights = new LinkedList<Light>();
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
        return new GraphSceneIterator(this, true);
    }

    @Override
    public Iterator<Light> lightIterator() {
        return new GraphSceneIterator(this, false).getLights().iterator();
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
        private List<Light> lights;

        public GraphSceneIterator(GraphSceneManager manager, boolean shape) {
            root = manager.getRoot();
            stack = new Stack<RenderItem>();
            lights = new LinkedList<Light>();
            init(root, shape);
        }

        public List<Light> getLights() {
            return lights;
        }

        private void init(Node node, boolean shape) {
            if (shape) {
                if (node.getShape() != null) {
                    stack.push(new RenderItem(node.getShape(), node
                            .getTransformationMatrix()));
                }
            } else {
                if (node instanceof LightNode) {
                    Vector3f pos = node.getLight().position;
                    Matrix3f trans = new Matrix3f();
                    Matrix4f oldTrans = node.getTransformationMatrix();
                    trans
                            .setColumn(0, oldTrans.m00, oldTrans.m10,
                                    oldTrans.m20);
                    trans
                            .setColumn(1, oldTrans.m01, oldTrans.m11,
                                    oldTrans.m21);
                    trans
                            .setColumn(2, oldTrans.m02, oldTrans.m12,
                                    oldTrans.m22);

                    trans.transform(pos);
                    node.getLight().position = pos;
                    lights.add(node.getLight());
                }
            }

            if (node.getChildren() != null) {
                List<Node> children = node.getChildren();

                for (Node child : children) {
                    Matrix4f t = new Matrix4f(child.getTransformationMatrix());
                    t.mul(node.getTransformationMatrix());
                    child.setTransformationMatrix(t);

                    init(child, shape);
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
